package test;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Getter(AccessLevel.PACKAGE)
public class Node {
    private ProxyRule rule;
    private Map<String, Node> children;

    @Override
    public String toString() {
        return "rule=" + rule + ", children=" + (children == null ? null : children.keySet());
    }

    public void insert(List<ProxyRule> rules) {
        rules.forEach(this::insertRule);
    }

    private void insertRule(ProxyRule rule) {
        String prefix = rule.getPrefix();
        URI uri = rule.getTarget();
        String[] elements = prefix.split("/");
        Node node = this;
        for (int i = 0; ; ) {
            String element = elements[i++];
            if (!"".equals(element)) {
                node = node.createIfAbsent(element);
            }
            if (i >= elements.length) {
                if (node.rule != null) {
                    throw new IllegalStateException("Duplicate " + prefix + ": " + node.rule.getTarget() + " & " + uri);
                }
                node.rule = rule;
                return;
            }
        }
    }

    public ProxyRule findRule(RequestPath path) {
        return get(path.elements()
                .stream()
                .map(PathContainer.Element::value));
    }

    ProxyRule get(Stream<String> path) {
        Iterator<String> iterator = path.iterator();
        Node node = this;
        ProxyRule rule = null;
        for (;;) {
            String element = iterator.next();
            if (!"/".equals(element)) {
                node = node.find(element);
                if (node == null) {
                    return rule;
                }
            }
            if (node.rule != null) {
                rule = node.rule;
            }
            if (!iterator.hasNext()) {
                return rule;
            }
        }
    }

    private Node createIfAbsent(String key) {
        if (children == null) {
            children = new HashMap<>();
        }
        return children.computeIfAbsent(key, k -> new Node());
    }

    private Node find(String key) {
        return children == null ? null : children.get(key);
    }
}
