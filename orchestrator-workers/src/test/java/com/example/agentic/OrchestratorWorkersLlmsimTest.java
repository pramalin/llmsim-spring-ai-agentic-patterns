package com.example.agentic;

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
class OrchestratorWorkersLlmsimTest {

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
    void decomposesTaskAndRunsTwoWorkers() {
        OrchestratorWorkers.FinalResponse result =
                new OrchestratorWorkers(
                        this.chatClientBuilder.build()
                ).process(Application.TASK);

        assertThat(result.analysis())
                .contains("two audiences")
                .contains("formal")
                .contains("conversational");

        assertThat(result.workerResponses())
                .containsExactly(
                        """
                        Formal Announcement:
                        The company will conduct a three-month pilot of a
                        four-day workweek to evaluate employee well-being
                        and sustained productivity.
                        """.strip(),
                        """
                        Team Announcement:
                        We are trying a four-day workweek for three months
                        to support well-being while keeping productivity
                        strong.
                        """.strip()
                );

        JsonNode calls = this.llmsim.calls();
        assertThat(calls.size()).isEqualTo(3);

        for (int index = 0; index < calls.size(); index++) {
            JsonNode call = calls.get(index);

            assertThat(call.path("sequence").asInt())
                    .isEqualTo(index + 1);
            assertThat(call.path("stepIndex").asInt())
                    .isEqualTo(index);
            assertThat(call.path("provider").asText())
                    .isEqualTo("anthropic");
            assertThat(call.path("model").asText())
                    .isEqualTo("llmsim-demo");
            assertThat(call.has("streamed")).isTrue();
            assertThat(call.path("streamed").asBoolean()).isFalse();
            assertThat(call.path("outcome").path("type").asText())
                    .isEqualTo("responded");
        }

        assertThat(LlmsimClient.messagesText(calls.get(0)))
                .contains("break it down into 2-3 distinct approaches")
                .contains("three-month pilot")
                .contains("four-day workweek");

        assertThat(LlmsimClient.messagesText(calls.get(1)))
                .contains("Style: formal")
                .contains("precise, technical")
                .contains("four-day workweek");

        assertThat(LlmsimClient.messagesText(calls.get(2)))
                .contains("Style: conversational")
                .contains("engaging, friendly")
                .contains("four-day workweek");
    }
}
