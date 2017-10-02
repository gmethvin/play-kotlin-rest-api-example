package v1.post

import java.util.*
import java.util.concurrent.CompletionStage
import java.util.stream.Stream

interface PostRepository {

    fun list(): CompletionStage<Stream<PostData>>

    fun create(postData: PostData): CompletionStage<PostData>

    operator fun get(id: Long?): CompletionStage<Optional<PostData>>

    fun update(id: Long?, postData: PostData): CompletionStage<Optional<PostData>>
}

