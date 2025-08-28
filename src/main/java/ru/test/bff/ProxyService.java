package ru.test.bff;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProxyService {
    private final WebClient webClient;
    private final ProxyConfig proxyConfig;

    public ProxyService(WebClient webClient, ProxyConfig proxyConfig) {
        this.webClient = webClient;
        this.proxyConfig = proxyConfig;
    }

    public Mono<ClientResponse> proxyRequest(ServerWebExchange exchange,
                                             Flux<DataBuffer> body) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        ProxyConfig.ProxyRule rule = findRule(path);
        String targetUrl = buildTargetUrl(path, rule);

        return webClient.method(request.getMethod())
                .uri(targetUrl)
                .headers(h -> h.addAll(request.getHeaders()))
                .body(body, DataBuffer.class)
                .exchangeToMono(Mono::just);
    }

    private ProxyConfig.ProxyRule findRule(String path) {
        return proxyConfig.getRules()
                .stream()
                .filter(rule -> path.startsWith(rule.getPrefix()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching rule found"));
    }

    private String buildTargetUrl(String path, ProxyConfig.ProxyRule rule) {
        return rule.getTarget() + path.substring(rule.getPrefix().length());
    }
}