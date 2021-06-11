package com.example.handlers;

import java.util.Optional;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import org.springframework.cloud.function.adapter.azure.FunctionInvoker;

public class UppercaseHandler extends FunctionInvoker<String, String> {

    @FunctionName("uppercase")
    // @formatter:off
    public HttpResponseMessage execute(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.GET, HttpMethod.POST }, 
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
    // @formatter:on

        context.getLogger().info("Java HTTP trigger processed a request.");

        final String word = request
            .getBody()
            .orElse(request
                .getQueryParameters()
                .get("word"));

        if (word == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Please pass a word on the query string or in the request body").build();
        } else {
            String converted = this.handleRequest(word, context);
            return request.createResponseBuilder(HttpStatus.OK).body(converted).build();
        }
    }
}
