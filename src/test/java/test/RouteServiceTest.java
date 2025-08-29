package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import test.junit.UidExtension;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static test.junit.UidExtension.uid;
import static test.junit.UidExtension.uidLeftPad;
import static test.junit.UidExtension.uidS;

@ExtendWith({
        MockitoExtension.class,
        UidExtension.class
})
@SuppressWarnings("unchecked")
class RouteServiceTest {
    @Spy
    @InjectMocks
    RouteService subj;
    @Spy
    ProxyConfig proxyConfig;

    @Test
    void postConstruct() {
        assertThat(subj.root).isNull();
        List<ProxyRule> rules = mock(List.class);
        proxyConfig.setRules(rules);
        var root = mock(Node.class);
        doReturn(root).when(subj).createNode();

        subj.postConstruct();

        verify(root).insert(rules);
        verifyNoMoreInteractions(rules, root);
    }

    @Test
    void createNode() {
        Node actual = subj.createNode();

        assertSoftly(s -> {
            s.assertThat(actual.getRule()).as("rule").isNull();
            s.assertThat(actual.getChildren()).as("children").isNull();
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void getTargetUrl(boolean found) {
        var request = mock(ServerHttpRequest.class);
        var path = mock(RequestPath.class);
        doReturn(path).when(request).getPath();
        var root = subj.root = mock(Node.class);
        var rule = newProxyRule();
        String prefix = "prefix" + uid();
        String suffix = "suffix" + uid();
        lenient().doReturn(uidLeftPad(prefix.length()) + suffix).when(path).value();
        rule.setPrefix(prefix);
        URI target = URI.create("https://host" + uid() + "/path" + uid());
        rule.setTarget(target);
        doReturn(found ? rule : null).when(root).findRule(path);

        String actual = subj.getTargetUrl(request);

        assertThat(actual).isEqualTo(found ? target + suffix : null);
    }

    ProxyRule newProxyRule() {
        var r = new ProxyRule();
        r.setPrefix(uidS());
        return r;
    }
}