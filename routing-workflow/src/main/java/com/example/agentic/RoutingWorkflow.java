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

    import java.util.Map;

    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.util.Assert;

    /**
     * Classifies an input and forwards it to a specialized prompt.
     */
    public final class RoutingWorkflow {

        private final ChatClient chatClient;

        public RoutingWorkflow(ChatClient chatClient) {
            this.chatClient = chatClient;
        }

        public String route(String input, Map<String, String> routes) {
            Assert.notNull(input, "Input text cannot be null");
            Assert.notEmpty(routes, "Routes map cannot be null or empty");

            String routeKey = determineRoute(input, routes.keySet());
            String selectedPrompt = routes.get(routeKey);

            if (selectedPrompt == null) {
                throw new IllegalArgumentException(
                        "Selected route '" + routeKey + "' not found in routes map"
                );
            }

            return this.chatClient
                    .prompt(selectedPrompt + "\nInput: " + input)
                    .call()
                    .content();
        }

        @SuppressWarnings("null")
        private String determineRoute(
                String input,
                Iterable<String> availableRoutes
        ) {
            System.out.println("\nAvailable routes: " + availableRoutes);

            String selectorPrompt = String.format("""
                    Analyze the input and select the most appropriate support team
                    from these options: %s

                    First explain your reasoning, then provide your selection in this
                    JSON format:
                    \\{
                      "reasoning": "Brief explanation of why this ticket should be routed.",
                      "selection": "The chosen team name"
                    \\}

                    Input: %s
                    """, availableRoutes, input);

            RoutingResponse response = this.chatClient
                    .prompt(selectorPrompt)
                    .call()
                    .entity(RoutingResponse.class);

            System.out.printf(
                    "Routing analysis: %s%nSelected route: %s%n",
                    response.reasoning(),
                    response.selection()
            );

            return response.selection();
        }
    }
