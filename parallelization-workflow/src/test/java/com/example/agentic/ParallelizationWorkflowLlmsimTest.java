package com.example.agentic;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = Application.class,
        properties = "agentic.demo.run=false"
)
@ActiveProfiles("llmsim")
@EnabledIfEnvironmentVariable(
        named = "LLMSIM_BASE_URL",
        matches = ".+"
)
class ParallelizationWorkflowLlmsimTest {

    private static final String LLMSIM_BASE_URL =
            System.getenv("LLMSIM_BASE_URL");

    private static final String EXPECTED_RESPONSE =
            "Stakeholder analysis completed.";

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    private LlmsimClient llmsim;

    @BeforeEach
    void resetSimulator() {
        this.llmsim = new LlmsimClient(LLMSIM_BASE_URL);
        this.llmsim.reset();
    }

    @Test
    void submitsAllStakeholderAnalysesConcurrently() {
        List<String> results = new ParallelizationlWorkflow(
                this.chatClientBuilder.build()
        ).parallel(Application.PROMPT, Application.INPUTS, 4);

        assertThat(results)
                .hasSize(4)
                .allMatch(EXPECTED_RESPONSE::equals);

        JsonNode calls = this.llmsim.calls();
        assertThat(calls.size()).isEqualTo(4);

        List<String> messages = new ArrayList<>();
        List<Integer> stepIndexes = new ArrayList<>();

        for (int index = 0; index < calls.size(); index++) {
            JsonNode call = calls.get(index);

            assertThat(call.path("sequence").asInt())
                    .isEqualTo(index + 1);
            stepIndexes.add(call.path("stepIndex").asInt());
            assertThat(call.path("provider").asText())
                    .isEqualTo("anthropic");
            assertThat(call.path("model").asText())
                    .isEqualTo("llmsim-demo");
            assertThat(call.has("streamed")).isTrue();
            assertThat(call.path("streamed").asBoolean()).isFalse();
            assertThat(call.path("outcome").path("type").asText())
                    .isEqualTo("responded");

            messages.add(LlmsimClient.messagesText(call));
        }

        assertThat(stepIndexes)
                .containsExactlyInAnyOrder(0, 1, 2, 3);

        assertThat(messages)
                .anySatisfy(message -> assertThat(message)
                        .contains("Customers:")
                        .contains("Price sensitive"))
                .anySatisfy(message -> assertThat(message)
                        .contains("Employees:")
                        .contains("Need new skills"))
                .anySatisfy(message -> assertThat(message)
                        .contains("Investors:")
                        .contains("Expect growth"))
                .anySatisfy(message -> assertThat(message)
                        .contains("Suppliers:")
                        .contains("Capacity constraints"));

        assertThat(messages).allSatisfy(message ->
                assertThat(message).contains(
                        "Analyze how market changes will impact"
                )
        );
    }
}
