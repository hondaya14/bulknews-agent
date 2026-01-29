package co.hondaya.notifier

import co.hondaya.model.JsonSupport
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.publisher.PublishResult
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.charset.StandardCharsets

class FileNotifier(
    private val outputPath: String = resolveOutputPath()
) : Notifier {
    override fun notify(
        context: RunContext,
        summaries: List<TopicSummary>,
        publishResult: PublishResult
    ): NotificationResult {
        val payload = NotificationPayload(
            topicCount = summaries.size,
            timeWindow = context.timeWindow,
            outputDir = publishResult.outputDir,
            jsonPath = publishResult.jsonPath,
            markdownPath = publishResult.markdownPath
        )
        val json = JsonSupport.instance.encodeToString(NotificationPayload.serializer(), payload)
        val file = File(outputPath)
        file.parentFile?.mkdirs()
        file.writeText(json, StandardCharsets.UTF_8)
        return NotificationResult(file.path)
    }

    companion object {
        private fun resolveOutputPath(): String {
            return System.getenv("BULKNEWS_NOTIFY_OUTPUT")?.takeIf { it.isNotBlank() }
                ?: "build/output/notification.json"
        }
    }
}

@Serializable
private data class NotificationPayload(
    val topicCount: Int,
    val timeWindow: String,
    val outputDir: String,
    val jsonPath: String,
    val markdownPath: String
)
