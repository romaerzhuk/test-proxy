package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import test.junit.UidExtension;

import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static test.junit.UidExtension.uid;
import static test.junit.UidExtension.uidS;

@ExtendWith({
        MockitoExtension.class,
        UidExtension.class
})
class NodeTest {
    @Spy
    Node subj;

    @Test
    void insert() {
        String[] prefix = {"/abc/def", "/abc/ghi/", "abc/jk/lm"};
        URI[] uri = Stream.generate(() -> URI.create("https://" + uid())).limit(prefix.length).toArray(URI[]::new);
        ProxyRule[] rule = IntStream.range(0, uri.length)
                .mapToObj(i -> {
                    var r = new ProxyRule();
                    r.setPrefix(prefix[i]);
                    r.setTarget(uri[i]);
                    return r;
                }).toArray(ProxyRule[]::new);

        subj.insert(List.of(rule));

        assertSoftly(s -> {
            s.assertThat(subj.getRule()).as("rule").isNull();
            s.assertThat(subj.getChildren().keySet()).as("children").containsExactly("abc");
            s.assertThat(subj.getChildren().get("abc").getRule()).as("abc.rule").isNull();
            s.assertThat(subj.getChildren().get("abc").getChildren().keySet()).as("abc.children").containsExactlyInAnyOrder("def", "ghi", "jk");
            s.assertThat(subj.getChildren().get("abc").getChildren().get("def").getRule()).as("abc.def.rule").isSameAs(rule[0]);
            s.assertThat(subj.getChildren().get("abc").getChildren().get("ghi").getRule()).as("abc.ghi.rule").isSameAs(rule[1]);
            s.assertThat(subj.getChildren().get("abc").getChildren().get("jk").getRule()).as("abc.ghi.rule").isNull();
            s.assertThat(subj.getChildren().get("abc").getChildren().get("jk").getChildren().keySet()).as("abc.jk.children").containsExactly("lm");
            s.assertThat(subj.getChildren().get("abc").getChildren().get("jk").getChildren().get("lm").getRule()).as("abc.jk.lm.rule").isSameAs(rule[2]);
        });
    }

    @Test
    void insert_duplicate() {
        String[] prefix = {"/abc/def", "/abc/ghi/", "abc/def"};
        URI[] uri = Stream.generate(() -> URI.create("https://" + uid())).limit(prefix.length).toArray(URI[]::new);
        ProxyRule[] rule = IntStream.range(0, uri.length)
                .mapToObj(i -> {
                    var r = new ProxyRule();
                    r.setPrefix(prefix[i]);
                    r.setTarget(uri[i]);
                    return r;
                }).toArray(ProxyRule[]::new);

        var ex = assertThrowsExactly(IllegalStateException.class, () -> subj.insert(List.of(rule)));

        assertThat(ex.getMessage()).isEqualTo("Duplicate abc/def: " + rule[0].getTarget() + " & " + rule[2].getTarget());
    }

    @Test
    void findRule() {
        String[] values = Stream.generate(UidExtension::uidS).limit(3 + uid(2)).toArray(String[]::new);
        List<PathContainer.Element> elements = Stream.of(values)
                .map(value -> {
                    var element = mock(PathContainer.Element.class, "element" + uid());
                    doReturn(value).when(element).value();
                    return element;
                }).toList();
        var request = mock(RequestPath.class);
        doReturn(elements).when(request).elements();
        var expected = new ProxyRule();
        expected.setPrefix(uidS());
        doAnswer(inv -> {
            Stream<String> path = inv.getArgument(0);
            assertThat(path).containsExactly(values);
            return expected;
        }).when(subj).get(any());

        ProxyRule actual = subj.findRule(request);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void get(boolean found) {
        String[] prefix = {"/abc/def", "abc/ghi", "abc/jk/lm"};
        URI[] uri = Stream.generate(() -> URI.create("https://" + uid())).limit(prefix.length).toArray(URI[]::new);
        ProxyRule[] rule = IntStream.range(0, uri.length)
                .mapToObj(i -> {
                    var r = new ProxyRule();
                    r.setPrefix(prefix[i]);
                    r.setTarget(uri[i]);
                    return r;
                }).toArray(ProxyRule[]::new);
        subj.insert(List.of(rule));
        int index = uid(prefix.length);
        List<String> path = Stream.of(prefix[index].split("/"))
                .filter(s -> !s.isEmpty())
                .toList();

        ProxyRule actual = subj.get(found ? path.stream() : Stream.of("abc", "def", uidS()));

        assertThat(actual).isSameAs(found ? rule[index] : null);
    }
}