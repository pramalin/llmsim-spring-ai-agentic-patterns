# Parallelization workflow

Adapted from the Spring AI parallelization example.

Four independent stakeholder prompts are submitted concurrently through the
Anthropic-shaped Spring AI client. The llmsim script intentionally returns the
same deterministic response for every call, so thread scheduling cannot associate
a stakeholder with the wrong scripted response. The journal assertions verify
that all four distinct inputs reached llmsim.
