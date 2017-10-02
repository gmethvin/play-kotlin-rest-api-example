package v1.post

import com.palominolabs.http.url.UrlBuilder
import play.libs.concurrent.HttpExecutionContext
import play.mvc.Http

import javax.inject.Inject
import java.nio.charset.CharacterCodingException
import java.util.Optional
import java.util.concurrent.CompletionStage
import java.util.stream.Stream
import java.util.function.Function as JFunction

/**
 * Handles presentation of Post resources, which map to JSON.
 */
class PostResourceHandler @Inject
constructor(private val repository: PostRepository, private val ec: HttpExecutionContext) {

    fun find(): CompletionStage<Stream<PostResource>> {
        return repository.list().thenApplyAsync(JFunction { postDataStream ->
            postDataStream.map { data -> PostResource(data, link(data)) }
        }, ec.current())
    }

    fun create(resource: PostResource): CompletionStage<PostResource> {
        val data = PostData(resource.title, resource.body)
        return repository.create(data).thenApplyAsync(JFunction { savedData ->
            PostResource(savedData, link(savedData))
        }, ec.current())
    }

    fun lookup(id: String): CompletionStage<Optional<PostResource>> {
        return repository.get(java.lang.Long.parseLong(id)).thenApplyAsync(JFunction { optionalData ->
            optionalData.map { data -> PostResource(data, link(data)) }
        }, ec.current())
    }

    fun update(id: String, resource: PostResource): CompletionStage<Optional<PostResource>> {
        val data = PostData(resource.title, resource.body)
        return repository.update(java.lang.Long.parseLong(id), data).thenApplyAsync(JFunction { optionalData ->
            optionalData.map { op -> PostResource(op, link(op)) }
        }, ec.current())
    }

    private fun link(data: PostData): String {
        // Make a point of using request context here, even if it's a bit strange
        val request = Http.Context.current().request()
        val hostPort = request.host().split(":")
        val host = hostPort[0]
        val port = if (hostPort.size == 2) Integer.parseInt(hostPort[1]) else -1
        val scheme = if (request.secure()) "https" else "http"
        try {
            return UrlBuilder.forHost(scheme, host, port)
                    .pathSegments("v1", "posts", data.id!!.toString())
                    .toUrlString()
        } catch (e: CharacterCodingException) {
            throw IllegalStateException(e)
        }

    }
}
