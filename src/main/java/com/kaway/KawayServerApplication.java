package com.kaway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = {"com.kaway.service", "com.kaway"})
public class KawayServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KawayServerApplication.class, args);
    }
}