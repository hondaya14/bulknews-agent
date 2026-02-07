package co.hondaya.notifier

import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.charset.StandardCharsets

class FileNotifier(
    private val outputPath: String = resolveOutputPath()
) : Notifier {
    override fun notify(
        context: RunContext,
        summaries: List<TopicSummary>
    ) {
        val payload = NotificationPayload(
            text = NotificationMessageFormatter.build(context, summaries)
        )
        val json = Json.encodeToString(payload)
        val file = File(outputPath)
        file.parentFile?.mkdirs()
        file.writeText(json, StandardCharsets.UTF_8)
    }

    companion object {
        private fun resolveOutputPath(): String {
            return System.getenv("BULKNEWS_NOTIFY_OUTPUT")?.takeIf { it.isNotBlank() }
                ?: error("Environment variable BULKNEWS_NOTIFY_OUTPUT is required.")
        }
    }
}

@Serializable
private data class NotificationPayload(
    val text: String
)
