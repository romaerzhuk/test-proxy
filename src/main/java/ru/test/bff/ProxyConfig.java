package ru.test.bff;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
    private List<ProxyRule> rules = new ArrayList<>();

    public static class ProxyRule {
        private String prefix;
        private String target;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    public List<ProxyRule> getRules() {
        return rules;
    }

    public void setRules(List<ProxyRule> rules) {
        this.rules = rules;
    }
}
