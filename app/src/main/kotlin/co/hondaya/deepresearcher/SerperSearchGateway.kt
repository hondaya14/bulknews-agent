package co.hondaya.deepresearcher

import co.hondaya.model.JsonSupport
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class SerperSearchGateway(
    private val apiKey: String,
    private val endpoint: String = "https://google.serper.dev/search",
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
) : SearchGateway {
    override fun search(query: String, limit: Int): List<SearchResult> {
        val payload = JsonSupport.instance.encodeToString(
            SerperRequest.serializer(),
            SerperRequest(query, limit)
        )

        val request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("X-API-KEY", apiKey)
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(15))
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                emptyList()
            } else {
                val parsed = JsonSupport.instance.decodeFromString(
                    SerperResponse.serializer(),
                    response.body()
                )
                parsed.organic.orEmpty().mapNotNull { item ->
                    val url = item.link?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    SearchResult(url = url, title = item.title, snippet = item.snippet)
                }
            }
        } catch (ex: Exception) {
            emptyList()
        }
    }
}

@Serializable
private data class SerperRequest(
    val q: String,
    val num: Int
)

@Serializable
private data class SerperResponse(
    val organic: List<SerperOrganic>? = null
)

@Serializable
private data class SerperOrganic(
    val title: String? = null,
    val link: String? = null,
    val snippet: String? = null
)
