package co.hondaya.collector

import org.jsoup.Jsoup
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class HttpCollector(
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(10))
        .build()
) : Collector {
    override fun collect(urls: List<String>): List<CollectedArticle> {
        return urls.mapNotNull { url ->
            fetch(url)?.let { html ->
                extract(url, html)
            }
        }
    }

    private fun fetch(url: String): String? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "bulknews-agent/0.1")
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() in 200..299) {
                response.body()
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
    }

    private fun extract(url: String, html: String): CollectedArticle {
        val document = Jsoup.parse(html)
        val title = document.selectFirst("meta[property=og:title]")?.attr("content")
            ?.takeIf { it.isNotBlank() }
            ?: document.title().takeIf { it.isNotBlank() }

        val description = document.selectFirst("meta[property=og:description]")?.attr("content")
            ?.takeIf { it.isNotBlank() }
            ?: document.selectFirst("meta[name=description]")?.attr("content")?.takeIf { it.isNotBlank() }

        val firstParagraph = document.select("p")
            .firstOrNull()
            ?.text()
            ?.takeIf { it.isNotBlank() }

        return CollectedArticle(url, title, description, firstParagraph)
    }
}
