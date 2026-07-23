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

    import java.util.List;
    import java.util.concurrent.CompletableFuture;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.stream.Collectors;

    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.util.Assert;

    /**
     * Runs independent LLM requests concurrently and returns results in input order.
     *
     * <p>The class name preserves the spelling used by the upstream Spring AI
     * example.</p>
     */
    public final class ParallelizationlWorkflow {

        private final ChatClient chatClient;

        public ParallelizationlWorkflow(ChatClient chatClient) {
            this.chatClient = chatClient;
        }

        public List<String> parallel(
                String prompt,
                List<String> inputs,
                int nWorkers
        ) {
            Assert.notNull(prompt, "Prompt cannot be null");
            Assert.notEmpty(inputs, "Inputs list cannot be empty");
            Assert.isTrue(
                    nWorkers > 0,
                    "Number of workers must be greater than 0"
            );

            ExecutorService executor = Executors.newFixedThreadPool(nWorkers);

            try {
                List<CompletableFuture<String>> futures = inputs.stream()
                        .map(input -> CompletableFuture.supplyAsync(() -> {
                            try {
                                return this.chatClient
                                        .prompt(prompt + "\nInput: " + input)
                                        .call()
                                        .content();
                            }
                            catch (Exception exception) {
                                throw new RuntimeException(
                                        "Failed to process input: " + input,
                                        exception
                                );
                            }
                        }, executor))
                        .collect(Collectors.toList());

                CompletableFuture.allOf(
                        futures.toArray(CompletableFuture[]::new)
                ).join();

                return futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
            }
            finally {
                executor.shutdown();
            }
        }
    }
