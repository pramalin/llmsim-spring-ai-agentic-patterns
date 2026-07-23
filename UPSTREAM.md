# Upstream source

The workflow implementations in this repository were adapted on July 23, 2026
from:

- Repository: `spring-projects/spring-ai-examples`
- Branch: `main`
- Directory: `agentic-patterns`
- Modules:
  - `chain-workflow`
  - `parallelization-workflow`
  - `routing-workflow`
  - `orchestrator-workers`
  - `evaluator-optimizer`

Upstream project:

- https://github.com/spring-projects/spring-ai-examples

llmsim project:

- https://github.com/pramalin/llmsim

## Changes in this companion project

- Included all five Spring AI workflow-pattern examples.
- Added `@ConditionalOnProperty` around each sample `CommandLineRunner`.
- Added llmsim-specific Spring profiles.
- Added deterministic llmsim Scala scripts.
- Added captured-call journal assertions.
- Used identical parallel responses to avoid scheduler-dependent association.
- Added Docker Compose orchestration.
- Added a one-command demonstration script.
- Added GitHub Actions integration testing.
- Tightened raw generic types and minor formatting in adapted examples.

The upstream source files retain the original Apache License 2.0 header.
