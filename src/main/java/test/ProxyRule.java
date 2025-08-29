package test;

import lombok.Data;

import java.net.URI;

@Data
public class ProxyRule {
    private String prefix;
    private URI target;
}
