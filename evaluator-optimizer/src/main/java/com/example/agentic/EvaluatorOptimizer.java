/*
 * Copyright 2024 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

    package com.example.agentic;

    import java.util.ArrayList;
    import java.util.List;

    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.util.Assert;

    /**
     * Implements the evaluator-optimizer workflow pattern.
     */
    public final class EvaluatorOptimizer {

        public static final String DEFAULT_GENERATOR_PROMPT = """
                Your goal is to complete the task based on the input.
                If feedback from previous generations is present, reflect on it and improve
                the solution.

                CRITICAL: Respond with one line of valid JSON:
                {"thoughts":"Brief description","response":"Escaped response content"}

                Escape line breaks inside response as \\n and quotes as \\".
                """;

        public static final String DEFAULT_EVALUATOR_PROMPT = """
                Evaluate the implementation for correctness, time complexity, and best
                practices. Ensure the code has proper Javadoc documentation.

                Respond with one line of valid JSON:
                {"evaluation":"PASS, NEEDS_IMPROVEMENT, or FAIL","feedback":"Feedback"}

                Use PASS only when all criteria are met.
                """;

        public record Generation(String thoughts, String response) {
        }

        public record EvaluationResponse(Evaluation evaluation, String feedback) {
            public enum Evaluation {
                PASS,
                NEEDS_IMPROVEMENT,
                FAIL
            }
        }

        public record RefinedResponse(
                String solution,
                List<Generation> chainOfThought
        ) {
        }

        private final ChatClient chatClient;
        private final String generatorPrompt;
        private final String evaluatorPrompt;

        public EvaluatorOptimizer(ChatClient chatClient) {
            this(chatClient, DEFAULT_GENERATOR_PROMPT, DEFAULT_EVALUATOR_PROMPT);
        }

        public EvaluatorOptimizer(
                ChatClient chatClient,
                String generatorPrompt,
                String evaluatorPrompt
        ) {
            Assert.notNull(chatClient, "ChatClient must not be null");
            Assert.hasText(generatorPrompt, "Generator prompt must not be empty");
            Assert.hasText(evaluatorPrompt, "Evaluator prompt must not be empty");

            this.chatClient = chatClient;
            this.generatorPrompt = generatorPrompt;
            this.evaluatorPrompt = evaluatorPrompt;
        }

        public RefinedResponse loop(String task) {
            List<String> memory = new ArrayList<>();
            List<Generation> chainOfThought = new ArrayList<>();
            return loop(task, "", memory, chainOfThought);
        }

        private RefinedResponse loop(
                String task,
                String context,
                List<String> memory,
                List<Generation> chainOfThought
        ) {
            Generation generation = generate(task, context);
            memory.add(generation.response());
            chainOfThought.add(generation);

            EvaluationResponse evaluation = evaluate(generation.response(), task);

            if (evaluation.evaluation() == EvaluationResponse.Evaluation.PASS) {
                return new RefinedResponse(
                        generation.response(),
                        List.copyOf(chainOfThought)
                );
            }

            StringBuilder newContext = new StringBuilder("Previous attempts:");
            for (String previous : memory) {
                newContext.append("\n- ").append(previous);
            }
            newContext.append("\nFeedback: ").append(evaluation.feedback());

            return loop(task, newContext.toString(), memory, chainOfThought);
        }

        private Generation generate(String task, String context) {
            Generation generation = this.chatClient.prompt()
                    .user(user -> user.text("{prompt}\n{context}\nTask: {task}")
                            .param("prompt", this.generatorPrompt)
                            .param("context", context)
                            .param("task", task))
                    .call()
                    .entity(Generation.class);

            System.out.printf(
                    "%n=== GENERATOR OUTPUT ===%nTHOUGHTS: %s%n%nRESPONSE:%n%s%n",
                    generation.thoughts(),
                    generation.response()
            );

            return generation;
        }

        private EvaluationResponse evaluate(String content, String task) {
            EvaluationResponse evaluation = this.chatClient.prompt()
                    .user(user -> user.text(
                                    "{prompt}\nOriginal task: {task}\nContent to evaluate: {content}"
                            )
                            .param("prompt", this.evaluatorPrompt)
                            .param("task", task)
                            .param("content", content))
                    .call()
                    .entity(EvaluationResponse.class);

            System.out.printf(
                    "%n=== EVALUATOR OUTPUT ===%nEVALUATION: %s%n%nFEEDBACK: %s%n",
                    evaluation.evaluation(),
                    evaluation.feedback()
            );

            return evaluation;
        }
    }
