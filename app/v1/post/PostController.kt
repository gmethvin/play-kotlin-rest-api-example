package v1.post

import play.libs.Json
import play.libs.concurrent.HttpExecutionContext
import play.mvc.Controller
import play.mvc.Result
import play.mvc.With
import java.util.concurrent.CompletionStage
import java.util.stream.Collectors
import javax.inject.Inject
import java.util.function.Function as JFunction

@With(PostAction::class)
class PostController @Inject
constructor(private val ec: HttpExecutionContext, private val handler: PostResourceHandler) : Controller() {

    fun list(): CompletionStage<Result> {
        return handler.find().thenApplyAsync(JFunction { posts ->
            val postList = posts.collect(Collectors.toList())
            ok(Json.toJson(postList))
        }, ec.current())
    }

    fun show(id: String): CompletionStage<Result> {
        return handler.lookup(id).thenApplyAsync(JFunction { optionalResource ->
            optionalResource.map({ resource -> ok(Json.toJson(resource)) }).orElseGet { notFound() }
        }, ec.current())
    }

    fun update(id: String): CompletionStage<Result> {
        val json = request().body().asJson()
        val resource = Json.fromJson(json, PostResource::class.java)
        return handler.update(id, resource).thenApplyAsync(JFunction { optionalResource ->
            optionalResource.map({ r -> ok(Json.toJson(r)) }).orElseGet({ notFound() })
        }, ec.current())
    }

    fun create(): CompletionStage<Result> {
        val json = request().body().asJson()
        val resource = Json.fromJson(json, PostResource::class.java)
        return handler.create(resource).thenApplyAsync(JFunction { savedResource ->
            created(Json.toJson(savedResource))
        }, ec.current())
    }
}
