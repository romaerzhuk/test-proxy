package test;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
    private List<ProxyRule> rules = new ArrayList<>();
}
