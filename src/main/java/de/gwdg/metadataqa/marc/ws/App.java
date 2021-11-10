package de.gwdg.metadataqa.marc.ws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * QA Catalogue web service
 */
@SpringBootApplication
@RestController
public class App extends SpringBootServletInitializer {

    @Value("${spring.application.name}")
    String appName;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @GetMapping("/hello2")
    public String sayHello(
        @RequestParam(value = "myName", defaultValue = "World") String name
    ) {
        return String.format("Hello %s!", name);
    }

}
