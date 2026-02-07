# bulknews-agent

Batch app that reads a run context JSON and emits topic summaries as JSON.

## Usage

```bash
./gradlew :app:run
```

Input/output paths are resolved via environment variables:

- `BULKNEWS_INPUT`
- `BULKNEWS_OUTPUT`
- `BULKNEWS_NOTIFY_OUTPUT`

External dependencies:

- `OPENAI_API_KEY`
- `GOOGLE_API_KEY` - Required for GeminiResearcher to perform web research using Google's Gemini API

## System Architecture

```mermaid
flowchart LR
  RC[run_context.json] --> APP[AgentApp]
  APP --> ORCH[orchestrator.Orchestrator]
  ORCH --> DR[researcher.GeminiResearcher]
  ORCH --> SUM[summarizer.SimpleSummarizer]
  ORCH --> NOTI[notifier.WebhookNotifier]
  APP --> OUTJSON[topic_summaries.json]
```

## Batch Flow

```mermaid
sequenceDiagram
  participant App as AgentApp
  participant Orchestrator as Orchestrator
  participant Researcher as GeminiResearcher
  participant Summarizer as SimpleSummarizer
  participant Notifier as WebhookNotifier

  App->>Orchestrator: run(context)
  Orchestrator->>Researcher: research(topic, timeWindow)
  Orchestrator->>Summarizer: summarize(items)
  Summarizer-->>Orchestrator: article summaries
  Orchestrator->>Notifier: notify(summaries)
  Notifier-->>Orchestrator: notification result
  App->>App: write topic_summaries.json
```

## Input (RunContext)

```json
{
  "topics": ["Kotlin 2.x compiler"],
  "time_window": "past 7 days",
  "max_items_per_topic": 3
}
```
