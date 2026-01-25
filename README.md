# BulkNews Agent

An AI-powered news aggregation and summarization agent built with Kotlin and Gemini API.

## Features

- 🤖 **AI-Powered News Collection**: Uses Google's Gemini API to collect and curate news articles
- 📝 **Automated Summarization**: Generates concise summaries of news articles with source attribution
- 📄 **Markdown Generation**: Creates beautifully formatted markdown files with news summaries
- ☁️ **Multiple Deployment Options**: Deploy to AWS S3, GitHub Pages, or save locally
- 🔧 **Configurable Topics**: Easily customize news topics of interest
- 🚀 **Built with Kotlin**: Leverages Kotlin's coroutines for efficient async operations

## Architecture

The BulkNews Agent is structured as follows:

- **GeminiClient**: Handles communication with Google's Gemini API
- **NewsCollectionService**: Orchestrates news collection across multiple topics
- **MarkdownGenerator**: Converts news collections into formatted markdown
- **DeploymentService**: Manages deployment to various platforms (S3, GitHub Pages, local)
- **AppConfig**: Manages application configuration from environment variables and config files

## Prerequisites

- Java 17 or higher
- Gradle 8.x (or use the included wrapper)
- Google Gemini API key (get one at [Google AI Studio](https://makersuite.google.com/app/apikey))
- (Optional) AWS credentials for S3 deployment
- (Optional) GitHub token for GitHub Pages deployment

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/hondaya14/bulknews-agent.git
   cd bulknews-agent
   ```

2. **Set up your Gemini API key**
   
   Set the environment variable:
   ```bash
   export GEMINI_API_KEY="your-api-key-here"
   ```
   
   Or edit `src/main/resources/application.conf` and replace `YOUR_GEMINI_API_KEY_HERE` with your actual key.

3. **Configure news topics** (Optional)
   
   Edit `src/main/resources/application.conf` to customize the news topics:
   ```hocon
   news {
     topics = [
       "Artificial Intelligence",
       "Kubernetes",
       "Kotlin Programming"
     ]
   }
   ```

4. **Configure deployment target** (Optional)
   
   Choose your deployment target in `application.conf`:
   - `local`: Save files locally (default)
   - `s3`: Deploy to AWS S3
   - `github`: Prepare for GitHub Pages deployment

## Usage

### Build the project

```bash
./gradlew build
```

### Run the agent

```bash
./gradlew run
```

Or build and run the JAR:

```bash
./gradlew jar
java -jar build/libs/bulknews-agent-1.0.0.jar
```

### Run with custom configuration

```bash
export GEMINI_API_KEY="your-key"
export DEPLOYMENT_TARGET="local"
./gradlew run
```

## Deployment Options

### Local Deployment (Default)

Files are saved to the current directory with a timestamp:
```
news-summary-1674656400000.md
```

### AWS S3 Deployment

1. Configure AWS credentials (using AWS CLI or environment variables)
2. Set environment variables:
   ```bash
   export DEPLOYMENT_TARGET="s3"
   export S3_BUCKET="your-bucket-name"
   export S3_REGION="us-east-1"
   ```
3. Run the agent

### GitHub Pages Deployment

1. Set environment variables:
   ```bash
   export DEPLOYMENT_TARGET="github"
   export GITHUB_REPO="username/repo"
   export GITHUB_BRANCH="gh-pages"
   ```
2. Run the agent
3. Commit and push the generated file to your GitHub repository
4. Enable GitHub Pages in your repository settings

## Configuration

Configuration can be set via environment variables or `application.conf`:

| Environment Variable | Config Path | Description | Default |
|---------------------|-------------|-------------|---------|
| `GEMINI_API_KEY` | `app.gemini.apiKey` | Google Gemini API key | Required |
| `DEPLOYMENT_TARGET` | `app.deployment.target` | Deployment target | `local` |
| `S3_BUCKET` | `app.deployment.s3.bucket` | S3 bucket name | - |
| `S3_REGION` | `app.deployment.s3.region` | S3 region | `us-east-1` |
| `GITHUB_REPO` | `app.deployment.github.repo` | GitHub repository | - |
| `GITHUB_BRANCH` | `app.deployment.github.branch` | GitHub branch | `gh-pages` |

## Example Output

The agent generates markdown files with the following structure:

```markdown
# Daily News Summary

*Generated on 2026-01-25 13:00:00*

## Table of Contents
1. [Artificial Intelligence and Machine Learning](#artificial-intelligence-and-machine-learning)
2. [Cloud Computing and DevOps](#cloud-computing-and-devops)

## Artificial Intelligence and Machine Learning

### 1. Latest Advances in Large Language Models
**Summary:** Recent breakthroughs in LLM technology show...
**Source:** Tech News Daily
**Published:** 2026-01-25
**Link:** [Read more](https://example.com/article1)
```

## Project Structure

```
bulknews-agent/
├── src/
│   ├── main/
│   │   ├── kotlin/com/bulknews/agent/
│   │   │   ├── Main.kt                    # Application entry point
│   │   │   ├── client/
│   │   │   │   └── GeminiClient.kt        # Gemini API client
│   │   │   ├── config/
│   │   │   │   └── AppConfig.kt           # Configuration management
│   │   │   ├── model/
│   │   │   │   └── NewsArticle.kt         # Data models
│   │   │   ├── output/
│   │   │   │   └── MarkdownGenerator.kt   # Markdown generation
│   │   │   └── service/
│   │   │       ├── NewsCollectionService.kt # News collection logic
│   │   │       └── DeploymentService.kt     # Deployment handling
│   │   └── resources/
│   │       ├── application.conf           # Application configuration
│   │       └── logback.xml               # Logging configuration
│   └── test/
│       └── kotlin/                        # Test files
├── build.gradle.kts                       # Gradle build configuration
├── settings.gradle.kts                    # Gradle settings
└── README.md                              # This file
```

## Technologies Used

- **Kotlin 1.9.21**: Modern JVM language
- **Ktor Client**: HTTP client for API communication
- **Kotlinx Serialization**: JSON serialization/deserialization
- **AWS SDK for Kotlin**: S3 integration
- **Kotlin Coroutines**: Asynchronous programming
- **Typesafe Config**: Configuration management
- **Logback**: Logging framework

## Development

### Running tests

```bash
./gradlew test
```

### Code formatting

```bash
./gradlew ktlintFormat
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with [Kotlin](https://kotlinlang.org/)
- Powered by [Google Gemini API](https://ai.google.dev/)
- Inspired by the need for automated news aggregation

## Support

For issues, questions, or contributions, please open an issue on GitHub.