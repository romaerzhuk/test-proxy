package test;

import java.net.URI;

public class ProxyRule {
    private String prefix;
    private URI target;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public URI getTarget() {
        return target;
    }

    public void setTarget(URI target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return prefix + ": " + target;
    }
}
