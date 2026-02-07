# bulknews-agent

Batch app that reads a run context JSON and sends topic summaries through notifier messages.

## Usage

```bash
./gradlew :app:run
```

Input/output paths are resolved via environment variables:

- `BULKNEWS_INPUT`
- `BULKNEWS_WEBHOOK_URL`
- `BULKNEWS_NOTIFY_OUTPUT` (only when `FileNotifier` is wired)

External dependencies:

- `OPENAI_API_KEY`
- `GOOGLE_API_KEY` - Optional, only required if you wire `GeminiResearcher`

## System Architecture

```mermaid
flowchart LR
  RC[run_context.json] --> APP[AgentApp]
  APP --> ORCH[orchestrator.Orchestrator]
  ORCH --> DR[researcher.GPTResearcher]
  ORCH --> SUM[summarizer.SimpleSummarizer]
  ORCH --> NOTI[notifier.WebhookNotifier]
```

## Batch Flow

```mermaid
sequenceDiagram
  participant App as AgentApp
  participant Orchestrator as Orchestrator
  participant Researcher as GPTResearcher
  participant Summarizer as SimpleSummarizer
  participant Notifier as WebhookNotifier

  App->>Orchestrator: run(context)
  Orchestrator->>Researcher: research(topic, timeWindow)
  Orchestrator->>Summarizer: summarize(items)
  Summarizer-->>Orchestrator: article summaries
  Orchestrator->>Notifier: notify(context, summaries)
  Notifier-->>Orchestrator: notification result
```

## Input (RunContext)

```json
{
  "topics": ["Kotlin 2.x compiler"],
  "time_window": "past 7 days",
  "max_items_per_topic": 3
}
```
