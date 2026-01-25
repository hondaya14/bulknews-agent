package co.hondaya.agent

import co.hondaya.agent.client.GeminiClient
import co.hondaya.agent.config.AppConfig
import co.hondaya.agent.output.MarkdownGenerator
import co.hondaya.agent.service.DeploymentServiceFactory
import co.hondaya.agent.service.NewsCollectionService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * BulkNews Agent - AI-powered news aggregation and summarization agent
 * 
 * This application:
 * 1. Collects news from various sources using Gemini API
 * 2. Summarizes news articles with source information
 * 3. Generates markdown files with formatted news
 * 4. Deploys to S3, GitHub Pages, or local storage
 */
fun main() = runBlocking {
    logger.info { "Starting BulkNews Agent" }
    
    try {
        // Load configuration
        val config = AppConfig.load()
        logger.info { "Configuration loaded successfully" }
        logger.info { "Topics to collect: ${config.newsTopics.joinToString(", ")}" }
        logger.info { "Deployment target: ${config.deploymentTarget}" }
        
        // Initialize services
        val geminiClient = GeminiClient(config.geminiApiKey)
        val newsCollectionService = NewsCollectionService(geminiClient)
        val markdownGenerator = MarkdownGenerator()
        val deploymentService = DeploymentServiceFactory.create(
            target = config.deploymentTarget,
            s3Bucket = config.s3Bucket,
            s3Region = config.s3Region,
            githubToken = config.githubToken,
            githubRepo = config.githubRepo,
            githubBranch = config.githubBranch
        )
        
        // Step 1: Collect news for all topics
        logger.info { "Step 1: Collecting news..." }
        val newsCollections = newsCollectionService.collectNews(config.newsTopics)
        logger.info { "Collected news for ${newsCollections.size} topics" }
        
        // Step 2: Generate markdown
        logger.info { "Step 2: Generating markdown..." }
        val markdown = markdownGenerator.generateMarkdown(newsCollections)
        
        // Step 3: Save to file
        logger.info { "Step 3: Saving markdown to file..." }
        val fileName = "news-summary-${System.currentTimeMillis()}.md"
        val filePath = markdownGenerator.saveToFile(markdown, fileName)
        logger.info { "Markdown saved to: $filePath" }
        
        // Step 4: Deploy
        logger.info { "Step 4: Deploying..." }
        val deploymentUrl = deploymentService.deploy(filePath)
        logger.info { "Deployment completed: $deploymentUrl" }
        
        // Cleanup
        geminiClient.close()
        
        logger.info { "BulkNews Agent completed successfully!" }
        logger.info { "Summary available at: $deploymentUrl" }
        
    } catch (e: Exception) {
        logger.error(e) { "Error running BulkNews Agent" }
        throw e
    }
}
