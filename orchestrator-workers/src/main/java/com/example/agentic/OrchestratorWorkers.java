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

    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.util.Assert;

    /**
     * Dynamically decomposes a task and delegates each generated subtask to a worker.
     */
    public final class OrchestratorWorkers {

        public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
                Analyze this task and break it down into 2-3 distinct approaches:

                Task: {task}

                Return your response in this JSON format:
                \\{
                  "analysis": "Explain your understanding of the task and why the approaches are valuable.",
                  "tasks": [
                    \\{
                      "type": "formal",
                      "description": "Write a precise, technical version."
                    \\},
                    \\{
                      "type": "conversational",
                      "description": "Write an engaging, friendly version."
                    \\}
                  ]
                \\}
                """;

        public static final String DEFAULT_WORKER_PROMPT = """
                Generate content based on:
                Task: {original_task}
                Style: {task_type}
                Guidelines: {task_description}
                """;

        public record Task(String type, String description) {
        }

        public record OrchestratorResponse(
                String analysis,
                List<Task> tasks
        ) {
        }

        public record FinalResponse(
                String analysis,
                List<String> workerResponses
        ) {
        }

        private final ChatClient chatClient;
        private final String orchestratorPrompt;
        private final String workerPrompt;

        public OrchestratorWorkers(ChatClient chatClient) {
            this(
                    chatClient,
                    DEFAULT_ORCHESTRATOR_PROMPT,
                    DEFAULT_WORKER_PROMPT
            );
        }

        public OrchestratorWorkers(
                ChatClient chatClient,
                String orchestratorPrompt,
                String workerPrompt
        ) {
            Assert.notNull(chatClient, "ChatClient must not be null");
            Assert.hasText(
                    orchestratorPrompt,
                    "Orchestrator prompt must not be empty"
            );
            Assert.hasText(workerPrompt, "Worker prompt must not be empty");

            this.chatClient = chatClient;
            this.orchestratorPrompt = orchestratorPrompt;
            this.workerPrompt = workerPrompt;
        }

        @SuppressWarnings("null")
        public FinalResponse process(String taskDescription) {
            Assert.hasText(
                    taskDescription,
                    "Task description must not be empty"
            );

            OrchestratorResponse orchestratorResponse = this.chatClient
                    .prompt()
                    .user(user -> user
                            .text(this.orchestratorPrompt)
                            .param("task", taskDescription))
                    .call()
                    .entity(OrchestratorResponse.class);

            System.out.printf(
                    "%n=== ORCHESTRATOR OUTPUT ===%nANALYSIS: %s%n%nTASKS: %s%n",
                    orchestratorResponse.analysis(),
                    orchestratorResponse.tasks()
            );

            List<String> workerResponses = orchestratorResponse.tasks()
                    .stream()
                    .map(task -> this.chatClient
                            .prompt()
                            .user(user -> user
                                    .text(this.workerPrompt)
                                    .param("original_task", taskDescription)
                                    .param("task_type", task.type())
                                    .param(
                                            "task_description",
                                            task.description()
                                    ))
                            .call()
                            .content())
                    .toList();

            System.out.println(
                    "\n=== WORKER OUTPUT ===\n" + workerResponses
            );

            return new FinalResponse(
                    orchestratorResponse.analysis(),
                    workerResponses
            );
        }
    }
