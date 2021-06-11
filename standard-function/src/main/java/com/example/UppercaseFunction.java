package com.example;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class UppercaseFunction {
    @FunctionName("uppercase")
    // @formatter:off
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
    // @formatter:on

        context.getLogger().info("Java HTTP trigger processed a request.");

        final String word = request
            .getBody()
            .orElse(request
                .getQueryParameters()
                .get("word"));

        if (word == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Please pass a message on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(word.toUpperCase()).build();
        }
    }
}
