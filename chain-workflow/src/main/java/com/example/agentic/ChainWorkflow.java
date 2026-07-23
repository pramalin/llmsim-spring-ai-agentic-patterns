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

    import java.util.Objects;

    import org.springframework.ai.chat.client.ChatClient;

    /**
     * Implements a four-stage prompt-chaining workflow.
     *
     * <p>Each response becomes the user input for the next model call.</p>
     */
    public final class ChainWorkflow {

        private static final String[] DEFAULT_SYSTEM_PROMPTS = {
                """
                Extract only the numerical values and their associated metrics from the text.
                Format each as 'value: metric' on a new line.
                Example:
                92: customer satisfaction
                45%: revenue growth
                """,
                """
                Convert all numerical values to percentages where possible.
                If a value is neither a percentage nor points, retain its original unit.
                Keep one number per line.
                """,
                """
                Sort all lines in descending order by numerical value.
                Keep the format 'value: metric' on each line.
                """,
                """
                Format the sorted data as a markdown table with columns:
                | Metric | Value |
                |:--|--:|
                """
        };

        private final ChatClient chatClient;
        private final String[] systemPrompts;

        public ChainWorkflow(ChatClient chatClient) {
            this(chatClient, DEFAULT_SYSTEM_PROMPTS);
        }

        public ChainWorkflow(ChatClient chatClient, String[] systemPrompts) {
            this.chatClient = Objects.requireNonNull(chatClient, "chatClient must not be null");
            this.systemPrompts = Objects.requireNonNull(
                    systemPrompts,
                    "systemPrompts must not be null"
            ).clone();
        }

        public String chain(String userInput) {
            String response = Objects.requireNonNull(userInput, "userInput must not be null");

            int step = 0;
            System.out.printf("%nSTEP %d:%n%s%n", step++, response);

            for (String systemPrompt : this.systemPrompts) {
                String input = systemPrompt + System.lineSeparator() + response;
                response = this.chatClient.prompt(input).call().content();
                System.out.printf("%nSTEP %d:%n%s%n", step++, response);
            }

            return response;
        }
    }
