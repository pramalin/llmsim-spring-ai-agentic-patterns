package com.example.agentic;

import com.example.agentic.EvaluatorOptimizer.RefinedResponse;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = Application.class,
        properties = "agentic.demo.run=false"
)
@ActiveProfiles("llmsim")
@EnabledIfEnvironmentVariable(named = "LLMSIM_BASE_URL", matches = ".+")
class EvaluatorOptimizerLlmsimTest {

    private static final String LLMSIM_BASE_URL =
            System.getenv("LLMSIM_BASE_URL");

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    private LlmsimClient llmsim;

    @BeforeEach
    void resetSimulator() {
        this.llmsim = new LlmsimClient(LLMSIM_BASE_URL);
        this.llmsim.reset();
    }

    @Test
    void rejectsFirstGenerationAndAcceptsImprovedGeneration() {
        RefinedResponse result = new EvaluatorOptimizer(
                this.chatClientBuilder.build()
        ).loop(Application.TASK);

        assertThat(result.chainOfThought()).hasSize(2);
        assertThat(result.chainOfThought().get(0).response())
                .contains("values.stream().min");

        assertThat(result.solution())
                .contains("Deque<Integer> minima")
                .contains("this.minima")
                .contains("public int getMin()")
                .contains("constant-time minimum lookup");

        JsonNode calls = this.llmsim.calls();
        assertThat(calls.size()).isEqualTo(4);

        for (int index = 0; index < 4; index++) {
            JsonNode call = calls.get(index);
            assertThat(call.path("sequence").asInt()).isEqualTo(index + 1);
            assertThat(call.path("stepIndex").asInt()).isEqualTo(index);
            assertThat(call.path("provider").asText()).isEqualTo("openai");
            assertThat(call.path("model").asText()).isEqualTo("llmsim-demo");
            assertThat(call.has("streamed"))
                    .as("journal entry should expose streamed")
                    .isTrue();
            assertThat(call.path("streamed").asBoolean()).isFalse();
            assertThat(call.path("outcome").path("type").asText())
                    .isEqualTo("responded");
        }

        assertThat(LlmsimClient.messagesText(calls.get(0)))
                .contains("All operations should be O(1)");

        assertThat(LlmsimClient.messagesText(calls.get(1)))
                .contains("values.stream().min");

        assertThat(LlmsimClient.messagesText(calls.get(2)))
                .contains("getMin() is O(n)")
                .contains("second stack")
                .contains("Previous attempts");

        assertThat(LlmsimClient.messagesText(calls.get(3)))
                .contains("Deque<Integer> minima")
                .contains("this.minima");
    }
}
