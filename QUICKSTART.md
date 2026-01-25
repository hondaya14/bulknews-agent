# Quick Start Guide

## 1. Prerequisites
- Java 17 or higher installed
- Gradle (or use included wrapper)
- Google Gemini API key

## 2. Get Your Gemini API Key
1. Visit https://makersuite.google.com/app/apikey
2. Sign in with your Google account
3. Create a new API key
4. Copy the key

## 3. Set Up the Project

### Option A: Using Environment Variables (Recommended)
```bash
export GEMINI_API_KEY="your-api-key-here"
```

### Option B: Using Configuration File
Edit `src/main/resources/application.conf` and replace:
```
apiKey = "YOUR_GEMINI_API_KEY_HERE"
```
with your actual API key.

## 4. Run the Agent

### Quick Run (No Build Required)
```bash
./gradlew run
```

### Build and Run JAR
```bash
./gradlew build
java -jar build/libs/bulknews-agent-1.0.0.jar
```

## 5. Check the Output
The agent will generate a file named `news-summary-<timestamp>.md` in the current directory.

## 6. Customize Topics (Optional)
Edit `src/main/resources/application.conf`:
```hocon
news {
  topics = [
    "Your Topic 1",
    "Your Topic 2",
    "Your Topic 3"
  ]
}
```

## 7. Deploy to S3 (Optional)
```bash
export DEPLOYMENT_TARGET="s3"
export S3_BUCKET="your-bucket-name"
export S3_REGION="us-east-1"
./gradlew run
```

## Troubleshooting

### "API key not found" error
Make sure your `GEMINI_API_KEY` environment variable is set correctly.

### "Java version" error
Ensure you have Java 17 or higher installed:
```bash
java -version
```

### Gradle build errors
Try cleaning and rebuilding:
```bash
./gradlew clean build
```

## What's Next?
- Schedule the agent to run periodically (cron job)
- Customize the markdown template
- Add more deployment targets
- Integrate with your CI/CD pipeline
