package test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
public class Controller {
    private final WebClient webClient;
    private final RouteService routeService;

    @RequestMapping("/api/**")
    public Mono<ResponseEntity<Flux<DataBuffer>>> proxy(ServerWebExchange exchange, @RequestBody(required = false) Flux<DataBuffer> body) {
        ServerHttpRequest request = exchange.getRequest();
        String url = routeService.getTargetUrl(request);
        if (url == null) {
            log.debug("path: {}: Not found", request.getPath());
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
        }
        HttpHeaders headers = request.getHeaders();
        log.debug("{} {} => {}", request.getMethod(), request.getPath(), url);

        return webClient.method(request.getMethod())
                .uri(url)
                .headers(h -> h.addAll(headers))
                .body(body, DataBuffer.class)
                .retrieve()
                .toEntityFlux(DataBuffer.class);
    }
}