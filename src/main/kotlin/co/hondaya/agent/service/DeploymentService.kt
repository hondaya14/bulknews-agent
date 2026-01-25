package co.hondaya.agent.service

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

interface DeploymentService {
    suspend fun deploy(filePath: String): String
}

class S3DeploymentService(
    private val bucketName: String,
    private val region: String
) : DeploymentService {

    override suspend fun deploy(filePath: String): String {
        logger.info { "Deploying to S3 bucket: $bucketName in region: $region" }
        
        val file = File(filePath)
        val fileName = file.name
        
        S3Client { this.region = region }.use { s3Client ->
            val request = PutObjectRequest {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(file.readBytes())
                contentType = "text/markdown"
            }
            
            s3Client.putObject(request)
            
            val url = "https://$bucketName.s3.$region.amazonaws.com/$fileName"
            logger.info { "Successfully deployed to S3: $url" }
            return url
        }
    }
}

class LocalDeploymentService : DeploymentService {
    override suspend fun deploy(filePath: String): String {
        logger.info { "File saved locally: $filePath" }
        return "file://$filePath"
    }
}

class GitHubPagesDeploymentService(
    private val token: String?,
    private val repo: String?,
    private val branch: String?
) : DeploymentService {
    
    override suspend fun deploy(filePath: String): String {
        logger.info { "GitHub Pages deployment - Manual setup required" }
        logger.info { "File ready at: $filePath" }
        logger.info { "To deploy to GitHub Pages:" }
        logger.info { "1. Commit the file to your repository" }
        logger.info { "2. Push to the '$branch' branch" }
        logger.info { "3. Enable GitHub Pages in repository settings" }
        
        // Note: Actual GitHub API integration would require additional libraries
        // For now, we'll just save locally and provide instructions
        return "file://$filePath (ready for GitHub Pages deployment)"
    }
}

object DeploymentServiceFactory {
    fun create(
        target: String,
        s3Bucket: String? = null,
        s3Region: String? = null,
        githubToken: String? = null,
        githubRepo: String? = null,
        githubBranch: String? = null
    ): DeploymentService {
        return when (target.lowercase()) {
            "s3" -> {
                require(s3Bucket != null && s3Region != null) {
                    "S3 bucket and region are required for S3 deployment"
                }
                S3DeploymentService(s3Bucket, s3Region)
            }
            "github" -> {
                GitHubPagesDeploymentService(githubToken, githubRepo, githubBranch)
            }
            "local" -> LocalDeploymentService()
            else -> {
                logger.warn { "Unknown deployment target: $target, using local deployment" }
                LocalDeploymentService()
            }
        }
    }
}
