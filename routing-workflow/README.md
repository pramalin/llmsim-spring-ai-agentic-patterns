# Routing workflow

Adapted from the Spring AI routing example.

The first LLM call returns a structured route selection. The second call uses
the selected technical-support prompt. The test verifies both the structured
decision and the specialized follow-up request through the llmsim journal.
