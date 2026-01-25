package com.bulknews.agent.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

data class AppConfig(
    val geminiApiKey: String,
    val newsTopics: List<String>,
    val outputFormat: String,
    val deploymentTarget: String,
    val s3Bucket: String?,
    val s3Region: String?,
    val githubToken: String?,
    val githubRepo: String?,
    val githubBranch: String?
) {
    companion object {
        fun load(): AppConfig {
            val config = ConfigFactory.load()
            return AppConfig(
                geminiApiKey = getEnvOrConfig(config, "GEMINI_API_KEY", "app.gemini.apiKey"),
                newsTopics = config.getStringList("app.news.topics"),
                outputFormat = config.getString("app.output.format"),
                deploymentTarget = config.getString("app.deployment.target"),
                s3Bucket = getOptionalEnvOrConfig(config, "S3_BUCKET", "app.deployment.s3.bucket"),
                s3Region = getOptionalEnvOrConfig(config, "S3_REGION", "app.deployment.s3.region"),
                githubToken = getOptionalEnvOrConfig(config, "GITHUB_TOKEN", "app.deployment.github.token"),
                githubRepo = getOptionalEnvOrConfig(config, "GITHUB_REPO", "app.deployment.github.repo"),
                githubBranch = getOptionalEnvOrConfig(config, "GITHUB_BRANCH", "app.deployment.github.branch")
            )
        }

        private fun getEnvOrConfig(config: Config, envVar: String, configPath: String): String {
            return System.getenv(envVar) ?: config.getString(configPath)
        }

        private fun getOptionalEnvOrConfig(config: Config, envVar: String, configPath: String): String? {
            return System.getenv(envVar) ?: if (config.hasPath(configPath)) config.getString(configPath) else null
        }
    }
}
