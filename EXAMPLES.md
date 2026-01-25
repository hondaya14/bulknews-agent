# Example: Running the BulkNews Agent

This document provides practical examples of running the BulkNews Agent.

## Basic Usage

### 1. Set Environment Variables

```bash
# Required: Your Gemini API key
export GEMINI_API_KEY="your-gemini-api-key-here"

# Optional: Deployment target (default: local)
export DEPLOYMENT_TARGET="local"
```

### 2. Run the Agent

```bash
# Using Gradle
./gradlew run

# Or using the JAR directly
java -jar build/libs/bulknews-agent-1.0.0.jar
```

## Advanced Examples

### Example 1: Local Deployment (Default)

Generate news summaries and save them locally:

```bash
export GEMINI_API_KEY="your-key"
./gradlew run
```

Output: `news-summary-<timestamp>.md` in the current directory

### Example 2: S3 Deployment

Deploy generated markdown files to AWS S3:

```bash
# Set up AWS credentials first
export AWS_ACCESS_KEY_ID="your-aws-key"
export AWS_SECRET_ACCESS_KEY="your-aws-secret"

# Configure the agent
export GEMINI_API_KEY="your-key"
export DEPLOYMENT_TARGET="s3"
export S3_BUCKET="my-news-bucket"
export S3_REGION="us-east-1"

# Run the agent
./gradlew run
```

### Example 3: Custom Topics

Edit `src/main/resources/application.conf` to customize topics:

```hocon
app {
  news {
    topics = [
      "Kotlin Programming Language",
      "Spring Boot Framework",
      "Docker and Containerization",
      "Microservices Architecture",
      "GraphQL APIs"
    ]
  }
}
```

Then run:
```bash
export GEMINI_API_KEY="your-key"
./gradlew run
```

### Example 4: Scheduled Execution

Run the agent periodically using cron:

```bash
# Edit crontab
crontab -e

# Add this line to run daily at 8 AM
0 8 * * * cd /path/to/bulknews-agent && GEMINI_API_KEY="your-key" ./gradlew run --no-daemon
```

### Example 5: Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/bulknews-agent-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t bulknews-agent .
docker run -e GEMINI_API_KEY="your-key" bulknews-agent
```

## Sample Output

When you run the agent, you'll see output like:

```
13:00:00.123 [main] INFO  co.hondaya.agent.MainKt - Starting BulkNews Agent
13:00:00.456 [main] INFO  co.hondaya.agent.MainKt - Configuration loaded successfully
13:00:00.789 [main] INFO  co.hondaya.agent.MainKt - Topics to collect: AI, Cloud Computing, Security
13:00:01.234 [main] INFO  co.hondaya.agent.service.NewsCollectionService - Starting news collection for 3 topics
13:00:05.678 [main] INFO  co.hondaya.agent.service.NewsCollectionService - Collected news for 3 topics
13:00:05.901 [main] INFO  co.hondaya.agent.output.MarkdownGenerator - Generating markdown for 3 news collections
13:00:05.999 [main] INFO  co.hondaya.agent.MainKt - Markdown saved to: /path/to/news-summary-1674656400000.md
13:00:06.123 [main] INFO  co.hondaya.agent.MainKt - Deployment completed: file:///path/to/news-summary-1674656400000.md
13:00:06.234 [main] INFO  co.hondaya.agent.MainKt - BulkNews Agent completed successfully!
```

## Troubleshooting

### Issue: "API key not found"

**Solution:** Make sure the `GEMINI_API_KEY` environment variable is set:
```bash
echo $GEMINI_API_KEY
```

### Issue: "S3 deployment failed"

**Solution:** Verify your AWS credentials and permissions:
```bash
aws s3 ls s3://$S3_BUCKET
```

### Issue: Build errors

**Solution:** Clean and rebuild:
```bash
./gradlew clean build --no-daemon
```

## Integration Examples

### CI/CD Pipeline (GitHub Actions)

```yaml
name: Generate News Summary
on:
  schedule:
    - cron: '0 8 * * *'  # Daily at 8 AM
  workflow_dispatch:

jobs:
  generate-news:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run BulkNews Agent
        env:
          GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
        run: ./gradlew run --no-daemon
      - name: Upload artifact
        uses: actions/upload-artifact@v3
        with:
          name: news-summary
          path: news-summary-*.md
```

### Kubernetes CronJob

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: bulknews-agent
spec:
  schedule: "0 8 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: bulknews-agent
            image: bulknews-agent:latest
            env:
            - name: GEMINI_API_KEY
              valueFrom:
                secretKeyRef:
                  name: bulknews-secrets
                  key: gemini-api-key
          restartPolicy: OnFailure
```

## Next Steps

- Customize the markdown template in `MarkdownGenerator.kt`
- Add more deployment targets (Cloudflare Pages, Azure Blob Storage)
- Implement email notifications
- Add RSS feed generation
- Create a web dashboard
