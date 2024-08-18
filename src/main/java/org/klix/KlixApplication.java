package org.klix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KlixApplication {

    public static void main(String[] args) {
        SpringApplication.run(new Class[]{KlixApplication.class}, args);
    }
}