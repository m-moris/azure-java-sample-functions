# Azure Functions Samples for Java

**[日本語 (Japanese)](./README.ja.md)**

There are several ways to implement Azure Functions in Java. This repository is a sample of them.

- Standard Functions.
- Function based on Azure adapter for Spring Cloud Function
- Functions based on Spring Cloud Functions with custom handlers

## Standard Functions

A standard Azure Functions sample, the project can be created with the following commands

```sh
mvn archetype:generate -DarchetypeGroupId=com.microsoft.azure -DarchetypeArtifactId=azure-functions-archetype -DjavaVersion=11
```

- Required settings are set by MAVEN（`pom.xml` ,`host.json` 、 `local.settings.json` etc...)
- Each function is qualified on an annotation basis. From there, `function.json`, the function definition, is automatically generated.
- Because it is a pure Java application, frameworks such as DI cannot be used.

## Azure Spring Cloud Function

This is a sample of Azure Functions using the Azure adapter for Spring Cloud Functions, with the following differences from the standard

- The `host.json` is different. If placed in the root folder, it will be packaged by the Azure Functions Maven Plugin.
- `functions.json` is generated automatically by annotation.
-  Specify `<start-class>` as a property of `pom.xml`. Embedded in `MANIFEST.MF`.
- Since it is not run by `spring-boot:run`, `spring-boot-maven-plugin` is not needed. (If it is in a strange position, it will be `repackaged` and slightly buggy.)

- Dependency libraries are copied to `target/azure-functions/{applicationName}/lib` at runtime (as specified by the Azure Functions Maven plugin)
- Functions Called via Java Worker (app does not listen for HTTP)

The following is a reference, but the description is a bit outdated

+ https://docs.spring.io/spring-cloud-function/docs/current/reference/html/azure.html

## Spring Cloud Function + Native

Native imaging of Spring Cloud Function applications as well as custom handlers for Azure Functions to run in Azure Functions.

To create a Native Image, the JVM must switch to GraalVM.


```sh
export JAVA_HOME=/usr/local/graalvm-ce-java11-21.1.0
export PATH=/usr/local/graalvm-ce-java11-21.1.0/bin:$PATH
```


- The application itself can run as a Spring Cloud Function app (explained later)
- API prefix is changed in `spring.cloud.function.web.path` in `application.properties`.
- Execution on Azure Functions uses a custom handler to execute the Native Image.
- Define `host.json` , `functions.json` manually. The copy to `target/azure-functions` is configured in `pom.xml`.
- Creating a Native Image and running it locally is described below.
- Set the port number of Native Image by `server.port=${FUNCTIONS_CUSTOMHANDLER_PORT:8080}` in `application.properties`.

### Run as local Spring Boot

Execute the following command.

```sh
mvn clean package spring-boot:run -DskipTests
```

The `@Bean` will be exposed as an API by Spring Cloud Function.

```sh
curl http://localhost:8080/api/uppercase -d "hello world" -H "Content-Type: text/plain"
```

Access the following URL to obtain the results.

```sh
curl http://localhost:8080/apu/uppercase/HOGEHOGE
```

### Run as local Azure Functions

Run locally as Azure Functions with the following command. (Since it is configured for Native, it cannot be run from a Java Worker.)

```sh
mvn clean package -Pnative 
mvn azure-functions:run 
```

Creating a Native Image takes a few minutes. The created binary file can be executed by itself.

```sh
./target/azure-functions/spring-cloud-function-native/com.example.DemoApplication
```

Access URLs published as Azure Functions, which are accessed via the Functions Host to Native Spring Boot apps.

```sh
curl http://localhost:7071/api/uppercase -d "hello world" -H "Content-Type: text/plain"
```

### Running on Azure

Cannot deploy using the Azure Functions Maven Plugin as is.

The presence and format of the `jar` file is checked in the target directory. (Even if you put a file with 0 bytes, it is NG)

Manually, you can copy the `jar` file to perform the deployment task.

```sh
cp target/spring-cloud-function-native-0.0.1-SNAPSHOT.jar target/azure-functions/spring-cloud-function-native
```

You get a warning, but you ignore it.

```log
[WARNING] The POM for com.microsoft.azure.applicationinsights.v2015_05_01:azure-mgmt-insights:jar:1.0.0-beta is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details
[WARNING] App setting `FUNCTIONS_WORKER_RUNTIME` doesn't meet the requirement of Azure Java Functions, the value should be `java`.
```

It can be accessed at the deployed URL.
