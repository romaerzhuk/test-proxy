package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class Controller {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Node root = new Node();
    private final WebClient webClient;

    public Controller(WebClient webClient, ProxyConfig proxyConfig) {
        this.webClient = webClient;
        proxyConfig.getRules()
                .forEach(root::insert);
    }

    @RequestMapping("/api/**")
    public Mono<ResponseEntity<Flux<DataBuffer>>> proxy(ServerWebExchange exchange, @RequestBody(required = false) Flux<DataBuffer> body) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        ProxyRule rule = root.findRule(path);
        if (rule == null) {
            log.debug("path: {}: Not found", path);
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
        }
        HttpHeaders headers = request.getHeaders();
        String url = buildTargetUri(path, rule);
        log.debug("{} {} => {}", request.getMethod(), path, url);

        return webClient.method(request.getMethod())
                .uri(url)
                .headers(h -> h.addAll(headers))
                .body(body, DataBuffer.class)
                .retrieve()
                .toEntityFlux(DataBuffer.class);
    }

    private String buildTargetUri(RequestPath path, ProxyRule rule) {
        return rule.getTarget() + path.value()
                .substring(rule.getPrefix().length());
    }
}