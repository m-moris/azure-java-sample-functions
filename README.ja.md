# Azure Functions Samples for Java

### 更新

- 全体的にAzure Functionsのバージョンを更新
- Azure Spring Cloud Function
  - Spring Boot のバージョンを更新 (2.7.9)
  - Azure Cloud Adapter のバージョンを更新 (4.0.1)
- Spring Cloud Function + Native
  - Spring Boot のバージョンを Native Imageがサポートされている 3.1.0-M に更新
  - Azure Cloud Adapter のバージョンを更新 (4.0.1)
  - Graalvm のバージョンを更新


## 概要

Azure Functions for Java のサンプルで、いくつか実装方法があるため自己理解をまとめたものである。

+ 標準的な関数
+ Spring Cloud Function の Azure アダプタをベースとした関数
+ Spring Cloud Function をベースとして、カスタムハンドラを使った関数

## 標準的な関数

標準的な Azure Functions のサンプルで、以下のコマンドでプロジェクトを作成できる。

```sh
mvn archetype:generate -DarchetypeGroupId=com.microsoft.azure -DarchetypeArtifactId=azure-functions-archetype -DjavaVersion=11
```

+ 必要な設定は、上記生成で設定される（`pom.xml` や `host.json` 、 `local.settings.json` など）
+ 各関数は、アノテーションベースで修飾する。そこから 関数の定義である `function.json` が自動で生成される。
+ 素のJavaアプリケーションなため、DIなどのフレームワークは使えない。

### 実行方法

以下でローカル実行できる。

```sh
mvn clean package azure-functions:run -DskipTests
```

## Azure Spring Cloud Function

Spring Cloud Functions の Azure アダプタを利用した Azure Functions のサンプルであり、標準的なものと以下の違いがある。

+ `host.json` が異なる。（ルートに置いておけば、Azure Functions Maven Pluginによってパッケージされる）
+ `functions.json` はアノテーションによる自動生成
+ `pom.xml` のプロパティに `<start-class>` を指定する。`MANIFEST.MF` に埋め込まれる。
+ `spring-boot:run` する訳ではないので、`spring-boot-maven-plugin` は不要 （変な位置にあると`repackage`されて微妙にバグることに）
+ 実行時に依存関係するライブラリは、`target/azure-functions/{applicationName}/lib` にコピーされる（Azure Functions Maven plugin の仕様として）
+ Functions Java Worker 経由で呼び出される（アプリがHTTPをリッスンするわけではない）

以下にリファレンスがあり、現時点で `4.0.1`

+ https://docs.spring.io/spring-cloud-function/docs/current/reference/html/azure.html

### 実行方法

以下でローカル実行できる。

```sh
mvn clean package azure-functions:run -DskipTests
```

## Spring Cloud Function + Native

素のSpring Cloud Function アプリケーションをNative Image化するとともに、Azure Functions のカスタムハンドラーを利用して、Azure Functionsで実行する。

Native Image を作成するために、JVM は GraalVM に切り替える必要がある。

```sh
export JAVA_HOME=/usr/local/graalvm-ce-java17-22.2.1
export PATH=/usr/local/graalvm-ce-java17-22.3.1/bin:$PATH
```

+ アプリ自身は、素の Spring Cloud Function アプリ（後述の方法で実行できる）
+ `application.properties` の `spring.cloud.function.web.path` で APIのプレフィックスを変更している
+ Azure Functions 上での実行は、Native Imageを実行するようカスタムハンドラーを使う
+ そのときのHTTPリクエストは `enableForwardingHttpRequest` を有効にしてのままフォワードする
+ `host.json` , `functions.json` の定義は手動。`target/azure-functions` へのコピーは、`pom.xml` で構成する
+ Native Image の作成とローカルで実行は後述のとおり
+ Native Imageのポート番号は、`application.properties` の `server.port=${FUNCTIONS_CUSTOMHANDLER_PORT:8080}` により設定する

### ローカルの Spring Boot として実行

以下のコマンドで、ローカル実行する。

```sh
mvn clean package spring-boot:run -DskipTests
```

`@Bean` が Spring Cloud Function により APIとして公開される。

```sh
curl http://localhost:8080/api/uppercase -d "hello world" -H "Content-Type: text/plain"
```

GETでも引数をURLパスに含めると、引数として渡すことができるが、Azure Functions上ではできない。

```sh
curl http://localhost:8080/apu/uppercase/HOGEHOGE
```

### ローカルの Azure Functionsとして実行

以下のコマンドで、Azure Functionsとしてローカルに実行する。（Native用に構成しているので、Java Workerからの実行はできない）

```sh
mvn -Pnative native:compile 
mvn azure-functions:run 
```

Native Imageの作成には、数分かかる。作成されたバイナリファイルは単独でも実行可能である。

```sh
./target/azure-functions/spring-cloud-function-native/spring-cloud-function-native
```

Azure Functions として公開されたURLにアクセスする。Functions Host を経由して、Native の Spring Boot アプリにアクセスされる。

```sh
curl http://localhost:7071/api/uppercase -d "hello world" -H "Content-Type: text/plain"
```

### Azure 上での実行

そのままでは、Azure Functions Maven Plugin を使ってデプロイできない。対象ディレクトリに`jar` ファイルの有無と形式をチェックしている。（0バイトのファイルを置いてもNG）

手動で、`jar` ファイルをコピーすると、デプロイタスクが実行できる。（`pom.xml` でやればいいけど）

```sh
cp target/spring-cloud-function-native-0.0.2-SNAPSHOT.jar target/azure-functions/spring-cloud-function-native
```

警告がでるけど無視する。

```log
[WARNING] The POM for com.microsoft.azure.applicationinsights.v2015_05_01:azure-mgmt-insights:jar:1.0.0-beta is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details
[WARNING] App setting `FUNCTIONS_WORKER_RUNTIME` doesn't meet the requirement of Azure Java Functions, the value should be `java`.
```

デプロイしたURLでアクセスできるはず。
