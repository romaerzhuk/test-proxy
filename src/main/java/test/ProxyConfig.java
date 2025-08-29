package test;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
    private List<ProxyRule> rules = new ArrayList<>();

    public List<ProxyRule> getRules() {
        return rules;
    }

    public void setRules(List<ProxyRule> rules) {
        this.rules = rules;
    }
}
