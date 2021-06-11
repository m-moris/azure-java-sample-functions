package com.example;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class Functions {

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

}
