package com.example.agentic;

import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

final class LlmsimClient {

    private final RestClient client;

    LlmsimClient(String baseUrl) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    void reset() {
        this.client.post()
                .uri("/_llmsim/reset")
                .retrieve()
                .toBodilessEntity();
    }

    JsonNode calls() {
        JsonNode body = this.client.get()
                .uri("/_llmsim/calls")
                .retrieve()
                .body(JsonNode.class);

        if (body == null || !body.isArray()) {
            throw new IllegalStateException(
                    "llmsim returned no call array"
            );
        }

        return body;
    }

    static String messagesText(JsonNode call) {
        return call.path("messages").toString();
    }
}
