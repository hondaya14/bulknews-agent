package co.hondaya.notifier

import co.hondaya.model.ArticleSummary
import co.hondaya.model.KeyPoint
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.publisher.PublishResult
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WebhookNotifierTest {
    @Test
    fun `posts slack-style payload to configured url`() {
        val client = CapturingHttpClient()
        val url = "https://example.com/webhook"
        val notifier = WebhookNotifier(client = client, url = url)

        val context = RunContext(
            topics = listOf("Kotlin", "AI"),
            timeWindow = "past 7 days",
            maxItemsPerTopic = 3
        )
        val summaries = listOf(
            TopicSummary(
                topic = "AI",
                timeWindow = "past 7 days",
                articles = listOf(
                    ArticleSummary(
                        title = "A",
                        tldr = listOf("t1"),
                        keyPoints = listOf(KeyPoint("k1", listOf("https://example.com/a"))),
                        sources = listOf("https://example.com/a")
                    )
                )
            ),
            TopicSummary(
                topic = "Kotlin",
                timeWindow = "past 7 days",
                articles = listOf(
                    ArticleSummary(
                        title = "B",
                        tldr = listOf("t2"),
                        keyPoints = listOf(KeyPoint("k2", listOf("https://example.com/b"))),
                        sources = listOf("https://example.com/b")
                    ),
                    ArticleSummary(
                        title = "C",
                        tldr = listOf("t3"),
                        keyPoints = listOf(KeyPoint("k3", listOf("https://example.com/c"))),
                        sources = listOf("https://example.com/c")
                    )
                )
            )
        )
        val publishResult = PublishResult(
            outputDir = "build/output",
            jsonPath = "build/output/topic_summaries.json",
            markdownPath = "build/output/topic_summaries.md"
        )

        notifier.notify(context, summaries, publishResult)

        val request = client.lastRequest
        assertNotNull(request)
        assertEquals(URI.create(url), request.uri())
        assertEquals("POST", request.method())
        val headers = request.headers().map()
        val hasJsonHeader = headers.any { (key, values) ->
            key.equals("Content-Type", ignoreCase = true) &&
                values.any { it.contains("application/json") }
        }
        assertTrue(hasJsonHeader)

        val body = client.lastBody ?: ""
        assertTrue(body.contains("\"text\""))
        assertTrue(body.contains("Bulknews run completed."))
        assertTrue(body.contains("Time window: past 7 days"))
        assertTrue(body.contains("Topic breakdown:"))
    }
}

private class CapturingHttpClient : HttpClient() {
    var lastRequest: HttpRequest? = null
        private set
    var lastBody: String? = null
        private set

    override fun <T : Any?> send(
        request: HttpRequest,
        responseBodyHandler: HttpResponse.BodyHandler<T>
    ): HttpResponse<T> {
        lastRequest = request
        request.bodyPublisher().ifPresent { publisher ->
            lastBody = bodyPublisherToString(publisher)
        }
        return SimpleHttpResponse(200)
    }

    override fun <T : Any?> sendAsync(
        request: HttpRequest,
        responseBodyHandler: HttpResponse.BodyHandler<T>
    ): CompletableFuture<HttpResponse<T>> {
        return CompletableFuture.completedFuture(send(request, responseBodyHandler))
    }

    override fun <T : Any?> sendAsync(
        request: HttpRequest,
        responseBodyHandler: HttpResponse.BodyHandler<T>,
        pushPromiseHandler: HttpResponse.PushPromiseHandler<T>
    ): CompletableFuture<HttpResponse<T>> {
        return CompletableFuture.completedFuture(send(request, responseBodyHandler))
    }

    override fun cookieHandler(): Optional<java.net.CookieHandler> = Optional.empty()
    override fun connectTimeout(): Optional<Duration> = Optional.of(Duration.ofSeconds(1))
    override fun followRedirects(): Redirect = Redirect.NEVER
    override fun proxy(): Optional<java.net.ProxySelector> = Optional.empty()
    override fun sslContext(): javax.net.ssl.SSLContext? = null
    override fun sslParameters(): javax.net.ssl.SSLParameters? = null
    override fun authenticator(): Optional<java.net.Authenticator> = Optional.empty()
    override fun version(): Version = Version.HTTP_1_1
    override fun executor(): Optional<Executor> = Optional.empty()
}

private class SimpleHttpResponse<T>(
    private val code: Int
) : HttpResponse<T> {
    override fun statusCode(): Int = code
    override fun request(): HttpRequest? = null
    override fun previousResponse(): Optional<HttpResponse<T>> = Optional.empty()
    override fun headers(): HttpHeaders = HttpHeaders.of(emptyMap()) { _, _ -> true }
    @Suppress("UNCHECKED_CAST")
    override fun body(): T = null as T
    override fun sslSession(): Optional<javax.net.ssl.SSLSession> = Optional.empty()
    override fun uri(): URI = URI.create("https://example.com")
    override fun version(): HttpClient.Version = HttpClient.Version.HTTP_1_1
}

private fun bodyPublisherToString(publisher: HttpRequest.BodyPublisher): String {
    val future = CompletableFuture<String>()
    val builder = StringBuilder()
    publisher.subscribe(object : Flow.Subscriber<ByteBuffer> {
        override fun onSubscribe(subscription: Flow.Subscription) {
            subscription.request(Long.MAX_VALUE)
        }

        override fun onNext(item: ByteBuffer) {
            val bytes = ByteArray(item.remaining())
            item.get(bytes)
            builder.append(String(bytes, StandardCharsets.UTF_8))
        }

        override fun onError(throwable: Throwable) {
            future.completeExceptionally(throwable)
        }

        override fun onComplete() {
            future.complete(builder.toString())
        }
    })
    return future.get(1, TimeUnit.SECONDS)
}
