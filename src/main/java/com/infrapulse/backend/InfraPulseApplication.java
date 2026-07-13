package com.infrapulse.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class InfraPulseApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfraPulseApplication.class, args);
    }
}
