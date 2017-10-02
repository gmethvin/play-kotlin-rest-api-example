package v1.post

/**
 * Resource for the API.  This is a presentation class for frontend work.
 */
data class PostResource(val id: String?, val link: String, val title: String, val body: String) {
    internal constructor(): this(null, "", "", "") // used for Jackson
    constructor(data: PostData, link: String): this(data.id.toString(), link, data.title, data.body)
}
