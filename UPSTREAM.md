# Upstream source

The workflow implementations in this repository were adapted on July 22, 2026
from:

- Repository: `spring-projects/spring-ai-examples`
- Branch: `main`
- Directory: `agentic-patterns`
- Modules:
  - `chain-workflow`
  - `evaluator-optimizer`

Upstream project:

- https://github.com/spring-projects/spring-ai-examples

llmsim project:

- https://github.com/pramalin/llmsim

## Changes in this companion project

- Selected only the prompt-chain and evaluator-optimizer examples.
- Added `@ConditionalOnProperty` around each sample `CommandLineRunner`.
- Added llmsim-specific Spring profiles.
- Added deterministic llmsim Scala scripts.
- Added captured-call journal assertions.
- Added Docker Compose orchestration.
- Added a one-command demonstration script.
- Added GitHub Actions integration testing.
- Tightened a few raw generic types in the evaluator-optimizer example.

The upstream source files retain the original Apache License 2.0 header.
