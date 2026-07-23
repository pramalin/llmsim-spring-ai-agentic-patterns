# llmsim Spring AI Agentic Patterns

This companion project demonstrates deterministic integration testing of all
five workflow examples in Spring AI's `agentic-patterns` project using
[llmsim](https://github.com/pramalin/llmsim).

Included patterns:

- **Prompt chaining** through Spring AI's Anthropic-shaped client.
- **Parallelization** with four concurrent stakeholder-analysis requests.
- **Routing** with structured classification and a specialized follow-up.
- **Orchestrator-workers** with structured task decomposition and two workers.
- **Evaluator-optimizer** through Spring AI's OpenAI-shaped client.

No external model, API key, or token spend is required. The application code
uses real Spring AI `ChatClient` instances; only the model endpoint is replaced
by llmsim.

## What the tests verify

The tests assert both workflow results and the captured llmsim journal:

- Prompt-chain responses propagate through four ordered stages.
- Four distinct parallel inputs reach llmsim without response-order flakiness.
- Routing returns a structured `technical` decision and uses its prompt.
- The orchestrator creates formal and conversational worker tasks.
- Evaluator feedback is included in the improved generation request.
- Provider, model, script step, response outcome, and streaming mode are recorded.

## Requirements

- Java 21
- Maven 3.9 or later
- Docker with Docker Compose
- `curl`
- llmsim 0.3.0 or later

## Run all five demonstrations

```bash
./run-demo.sh
```

The runner switches llmsim to the script for each pattern and executes that
module's integration test.

## Run one scenario manually

Start a script:

```bash
LLMSIM_SCRIPT=com.example.agentic.llmsim.RoutingWorkflowFlow       docker compose up --build
```

In another terminal:

```bash
LLMSIM_BASE_URL=http://localhost:8089       mvn -pl routing-workflow       -Dtest=RoutingWorkflowLlmsimTest       test
```

Available script classes:

```text
com.example.agentic.llmsim.ChainWorkflowFlow
com.example.agentic.llmsim.ParallelizationWorkflowFlow
com.example.agentic.llmsim.RoutingWorkflowFlow
com.example.agentic.llmsim.OrchestratorWorkersFlow
com.example.agentic.llmsim.EvaluatorOptimizerFlow
```

## Inspect calls

```bash
curl -s http://localhost:8089/_llmsim/calls | jq
```

Reset the script and journal:

```bash
curl -s -X POST http://localhost:8089/_llmsim/reset
```

## Project structure

```text
.
├── chain-workflow/
├── parallelization-workflow/
├── routing-workflow/
├── orchestrator-workers/
├── evaluator-optimizer/
├── llmsim/
│   ├── Dockerfile
│   └── src/main/scala/com/example/agentic/llmsim/
├── compose.yaml
├── run-demo.sh
├── UPSTREAM.md
└── .github/workflows/integration-test.yaml
```

## Notes

- `llmsim-build:0.3.0` is pinned in the Dockerfile.
- The workflow implementations are adapted from the Apache-2.0-licensed
  `spring-projects/spring-ai-examples` repository.
- The copied and adapted application classes retain upstream license headers.
- The parallel test intentionally uses one response value for all four calls;
  request arrival order is nondeterministic, while the journal proves all four
  stakeholder inputs were submitted.
- See [UPSTREAM.md](UPSTREAM.md) for provenance and local changes.
