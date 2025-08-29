package test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(Controller.class)
@Import({TestController.class, WebConfig.class})
public class ControllerTest {
    @Autowired
    WebTestClient client;

    @Test
    void get_test() {
        String name = "name" + System.currentTimeMillis() % 1000;
        client.get().uri("/test/" + name)
                .exchange()
                .expectAll(resp -> resp.expectStatus().isOk(),
                        resp -> resp.expectBody(String.class).isEqualTo("Hello " + name));
    }

    @Test
    void get_unknown_NotFound() {
        client.get()
                .uri("/api/unknown")
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
