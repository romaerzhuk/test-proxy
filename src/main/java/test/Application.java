package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Application.class);
        log.info("Version: {}", Application.class.getPackage().getImplementationVersion());
        SpringApplication.run(Application.class, args);
    }
}