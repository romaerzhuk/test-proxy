package test;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {
    private final ProxyConfig proxyConfig;

    Node root;

    @PostConstruct
    void postConstruct() {
        root = createNode();
        root.insert(proxyConfig.getRules());
    }

    Node createNode() {
        return new Node();
    }

    public String getTargetUrl(ServerHttpRequest request) {
        RequestPath path = request.getPath();
        ProxyRule rule = root.findRule(path);
        if (rule == null) {
            log.debug("getTargetUrl {}: null", path);
            return null;
        }
        String url = rule.getTarget() + path.value()
                .substring(rule.getPrefix().length());
        log.debug("getTargetUrl {}: {}", path, url);
        return url;
    }
}
