# bulknews-agent

Batch app that reads a run context JSON and emits topic summaries as JSON.

## Usage

```bash
./gradlew :app:run
```

Input/output paths are resolved via environment variables:

- `BULKNEWS_INPUT` (default: `run_context.json`)
- `BULKNEWS_OUTPUT` (default: `build/output/topic_summaries.json`)
- `BULKNEWS_OUTPUT_DIR` (default: `build/output`)
- `BULKNEWS_NOTIFY_OUTPUT` (default: `build/output/notification.json`)

External dependencies:

- `OPENAI_API_KEY` (Koog deep research planning)
- `SERPER_API_KEY` (search results; Serper-compatible)

## System Architecture

```mermaid
flowchart LR
  RC[run_context.json] --> ORCH[orchestrator.BatchOrchestrator]
  ORCH --> DR[deepresearcher.KoogDeepResearcher]
  DR --> SG[deepresearcher.SearchGateway]
  ORCH --> COL[collector.HttpCollector]
  ORCH --> SUM[summarizer.SimpleSummarizer]
  ORCH --> PUB[publisher.FilePublisher]
  ORCH --> NOTI[notifier.FileNotifier]
  PUB --> OUTJSON[topic_summaries.json]
  PUB --> OUTMD[topic_summaries.md]
  NOTI --> NOTIF[notification.json]
```

## Batch Flow

```mermaid
sequenceDiagram
  participant App as AgentApp
  participant Orchestrator as BatchOrchestrator
  participant Researcher as KoogDeepResearcher
  participant Search as SearchGateway
  participant Collector as HttpCollector
  participant Summarizer as SimpleSummarizer
  participant Publisher as FilePublisher
  participant Notifier as FileNotifier

  App->>Orchestrator: run(context)
  Orchestrator->>Researcher: research(topic, timeWindow)
  Researcher->>Search: search(queries)
  Search-->>Researcher: urls
  Orchestrator->>Collector: collect(urls)
  Collector-->>Orchestrator: collected articles
  Orchestrator->>Summarizer: summarize(articles)
  Summarizer-->>Orchestrator: article summaries
  Orchestrator->>Publisher: publish(summaries)
  Publisher-->>Orchestrator: publish result
  Orchestrator->>Notifier: notify(result)
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
