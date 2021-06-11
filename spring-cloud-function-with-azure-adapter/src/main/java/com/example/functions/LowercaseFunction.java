package com.example.functions;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component("lowercase")
public class LowercaseFunction implements Function<Mono<String>, Mono<String>> {

    @Override
    public Mono<String> apply(Mono<String> mono) {
        return mono.map(word -> word.toLowerCase());
    }
}
