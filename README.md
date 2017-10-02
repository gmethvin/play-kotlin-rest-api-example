[<img src="https://img.shields.io/travis/playframework/play-java-rest-api-example.svg"/>](https://travis-ci.org/playframework/play-java-rest-api-example)


# play-kotlin-rest-api-example

A REST API showing Play with Kotlin and a JPA backend. This is a Kotlin version of the Java example at https://github.com/playframework/play-java-rest-api-example. For a Scala version, please see https://github.com/playframework/play-scala-rest-api-example

## Known issues

This library uses https://github.com/pfn/kotlin-plugin to add Kotlin to the project in SBT. Proper use of twirl templates requires mixed scala+kotlin+java compilation, which is currently not implemented. See https://github.com/pfn/kotlin-plugin/issues/15 for more discussion.

There is also a similar problem for reverse routing, since those classes are generated by Scala code. It should be usable as long as you don't need to depend on the generated routes class inside your Kotlin code.

## Best Practices for Blocking API

If you look at the controller: https://github.com/playframework/play-java-rest-api-example/blob/master/app/v1/post/PostController.kt
then you can see that when calling out to a blocking API like JDBC, you should put it behind an asynchronous boundary -- in practice, this means using the CompletionStage API to make sure that you're not blocking the rendering thread while the database call is going on in the background.

```kotlin
fun list(): CompletionStage<Result> {
    return handler.find().thenApplyAsync(JFunction { posts ->
        ok(Json.toJson(posts.collect(Collectors.toList())))
    }, ec.current())
}
```

There is more detail in https://www.playframework.com/documentation/latest/ThreadPools -- notably, you can always bump up the number of threads in the rendering thread pool rather than do this -- but it gives you an idea of best practices.
