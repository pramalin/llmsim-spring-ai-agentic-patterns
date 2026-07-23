# llmsim Spring AI Agentic Patterns

This companion project demonstrates deterministic integration testing of selected
Spring AI agentic workflow examples using
[llmsim](https://github.com/pramalin/llmsim).

It includes two examples:

- **Prompt chaining** through Spring AI's Anthropic-shaped client.
- **Evaluator-optimizer** through Spring AI's OpenAI-shaped client.

No external model, API key, or token spend is required. The application code uses
real Spring AI `ChatClient` instances; only the model endpoint is replaced by
llmsim.

## What the tests verify

The tests assert both the final workflow result and the captured llmsim journal:

- The prompt chain makes exactly four calls in sequence.
- Each chain stage receives the previous stage's response.
- The evaluator initially rejects an O(n) implementation.
- The generator receives the evaluator's feedback.
- The refined implementation is evaluated and accepted.
- Spring AI uses the expected provider protocol and model name.
- Journal entries identify whether the request used streaming.

## Requirements

- Java 21
- Maven 3.9 or later
- Docker with Docker Compose
- `curl`
- llmsim 0.3.0 or later

## Run both demonstrations

```bash
./run-demo.sh
```

The script builds the custom llmsim image, runs the chain-workflow test, switches
llmsim to the evaluator-optimizer script, and runs the second test.

## Run one scenario manually

Start the chain script:

```bash
LLMSIM_SCRIPT=com.example.agentic.llmsim.ChainWorkflowFlow \
  docker compose up --build
```

In a second terminal:

```bash
LLMSIM_BASE_URL=http://localhost:8089 \
  mvn -pl chain-workflow \
  -Dtest=ChainWorkflowLlmsimTest \
  test
```

For evaluator-optimizer, replace the script and module:

```bash
LLMSIM_SCRIPT=com.example.agentic.llmsim.EvaluatorOptimizerFlow \
  docker compose up --build
```

```bash
LLMSIM_BASE_URL=http://localhost:8089 \
  mvn -pl evaluator-optimizer \
  -Dtest=EvaluatorOptimizerLlmsimTest \
  test
```

## Inspect calls

While llmsim is running:

```bash
curl -s http://localhost:8089/_llmsim/calls
```

Reset the script and journal:

```bash
curl -s -X POST http://localhost:8089/_llmsim/reset
```

## Project structure

```text
.
├── chain-workflow/
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
- The copied application classes retain their upstream license headers.
- See [UPSTREAM.md](UPSTREAM.md) for provenance and local changes.
