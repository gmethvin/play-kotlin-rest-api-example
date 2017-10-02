package it

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import play.Application
import play.inject.guice.GuiceApplicationBuilder
import play.libs.Json
import play.mvc.Http
import play.test.Helpers.*
import play.test.WithApplication
import v1.post.PostData
import v1.post.PostRepository
import v1.post.PostResource

class IntegrationTest : WithApplication() {

    override fun provideApplication(): Application {
        return GuiceApplicationBuilder().build()
    }

    @Test
    fun testList() {
        val repository = app.injector().instanceOf(PostRepository::class.java)
        repository.create(PostData("title", "body"))

        val request = Http.RequestBuilder()
                .method(GET)
                .uri("/v1/posts")

        val result = route(app, request)
        val body = contentAsString(result)
        assertThat(body, containsString("body"))
    }

    @Test
    fun testTimeoutOnUpdate() {
        val repository = app.injector().instanceOf(PostRepository::class.java)
        repository.create(PostData("title", "body"))

        val json = Json.toJson(PostResource("1", "http://localhost:9000/v1/posts/1", "some title", "somebody"))

        val request = Http.RequestBuilder()
                .method(POST)
                .bodyJson(json)
                .uri("/v1/posts/1")

        val result = route(app, request)
        assertThat(result.status(), equalTo(Http.Status.GATEWAY_TIMEOUT))
    }

    @Test
    fun testCircuitBreakerOnShow() {
        val repository = app.injector().instanceOf(PostRepository::class.java)
        repository.create(PostData("title", "body"))

        val request = Http.RequestBuilder()
                .method(GET)
                .uri("/v1/posts/1")

        val result = route(app, request)
        assertThat(result.status(), equalTo(Http.Status.SERVICE_UNAVAILABLE))
    }


}
