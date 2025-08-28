package ru.test.bff;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ProxyController {
    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @RequestMapping("/**")
    public Mono<ResponseEntity<Flux<DataBuffer>>> proxy(
            ServerWebExchange exchange,
            @RequestBody(required = false) Flux<DataBuffer> body) {
        
        return proxyService.proxyRequest(exchange, body)
                .map(this::convertResponse);
    }

    private ResponseEntity<Flux<DataBuffer>> convertResponse(ClientResponse response) {
        return ResponseEntity.status(response.statusCode())
                .headers(headers -> headers.addAll(response.headers().asHttpHeaders()))
                .body(response.bodyToFlux(DataBuffer.class));
    }
}