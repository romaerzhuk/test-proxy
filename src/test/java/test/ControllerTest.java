package test;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import test.junit.UidExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static test.junit.UidExtension.uid;

@ExtendWith(UidExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerTest {
    @Autowired
    WebTestClient client;

    @MockitoSpyBean
    RouteService routeService;

    @Test
    void get_test() {
        String name = "name" + uid();
        String path = "/api/test/" + name;
        doAnswer(inv -> {
            ServerHttpRequest request = inv.getArgument(0);
            assertThat(request.getPath().value()).isEqualTo(path);
            return StringUtils.substring(request.getURI().toString(), 0, -path.length()) + "/test/" + name;
        }).when(routeService).getTargetUrl(any());

        client.get()
                .uri("/api/test/" + name)
                .exchange()
                .expectAll(resp -> resp.expectStatus().isOk(),
                        resp -> resp.expectBody(String.class).isEqualTo("Hello " + name));
        verify(routeService).getTargetUrl(any());
    }

    @Test
    void get_unknown_NotFound() {
        String name = "name" + uid();
        String path = "/api/test/" + name;
        doAnswer(inv -> {
            ServerHttpRequest request = inv.getArgument(0);
            assertThat(request.getPath().value()).isEqualTo(path);
            return null;
        }).when(routeService).getTargetUrl(any());

        client.get()
                .uri(path)
                .exchange()
                .expectStatus()
                .isNotFound();
        verify(routeService).getTargetUrl(any());
    }
}
