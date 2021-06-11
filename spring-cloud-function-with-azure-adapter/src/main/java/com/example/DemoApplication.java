package com.example;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // You can also use @Bean instead of @Component.

    /*
    @Bean
    public Function<Mono<String>, Mono<String>> uppercase() {
        return mono -> mono.map(value -> {
            return value.toUpperCase();
        });
    }
    
    @Bean
    public Function<Mono<String>, Mono<String>> lowercase() {
        return mono -> mono.map(value -> {
            return value.toLowerCase();
        });
    }
    */

}
