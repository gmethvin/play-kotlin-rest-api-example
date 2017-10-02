package controllers

import play.mvc.*

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
class HomeController : Controller() {

    private val html = """<!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="utf-8">
        <title>Play REST API</title>
        </head>

        <body>
        <h1>Play REST API</h1>

        <p>
        This is a placeholder page to show you the REST API.  Use <a href="https://httpie.org/">httpie</a> to post JSON to the application.
        </p>

        <p>
        To see all posts, you can do a GET:
        </p>


        <pre>
        <code>http GET localhost:9000/v1/posts</code>
        </pre>

        <p>
        To create new posts, do a post
        <p>

        <pre>
        <code>http POST localhost:9000/v1/posts title="Some title" body="Some Body"</code>
        </pre>

        <p>
        You can always look at the API directly: <a href="/v1/posts">/v1/posts</a>
        </p>

        </body>
        </html>
    """.trimIndent()

    fun index(): Result {
        return ok(html).`as`("text/html")
    }
}

