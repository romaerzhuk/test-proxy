package test;

import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    Map<String, Node> children;
    ProxyRule rule;

    public void insert(ProxyRule rule) {
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
        List<PathContainer.Element> elements = path.elements();
        Node node = this;
        for (int i = 0; ; ) {
            PathContainer.Element element = elements.get(i++);
            if (element instanceof PathContainer.PathSegment) {
                node = node.find(element.value());
                if (node == null) {
                    return null;
                }
            }
            if (node.rule != null || i >= elements.size()) {
                return node.rule;
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

    @Override
    public String toString() {
        return "Node(rule=" + rule + ", children=" + children + ')';
    }
}
