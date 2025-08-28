package ru.test.bff;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(ProxyConfig.class)
public class AppConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}

