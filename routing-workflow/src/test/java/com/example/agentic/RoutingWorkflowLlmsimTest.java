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
class RoutingWorkflowLlmsimTest {

    private static final String LLMSIM_BASE_URL =
            System.getenv("LLMSIM_BASE_URL");

    private static final String EXPECTED_RESPONSE = """
            Technical Support Response:
            1. Roll back to version 5.1 to restore service.
            2. Collect the startup log and crash dump.
            3. Escalate with the artifacts if version 5.2 still fails.
            """.strip();

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    private LlmsimClient llmsim;

    @BeforeEach
    void resetSimulator() {
        this.llmsim = new LlmsimClient(LLMSIM_BASE_URL);
        this.llmsim.reset();
    }

    @Test
    void classifiesAndRoutesTechnicalTicket() {
        String result = new RoutingWorkflow(
                this.chatClientBuilder.build()
        ).route(Application.INPUT, Application.supportRoutes());

        assertThat(result.strip()).isEqualTo(EXPECTED_RESPONSE);

        JsonNode calls = this.llmsim.calls();
        assertThat(calls.size()).isEqualTo(2);

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
                .contains("select the most appropriate support team")
                .contains("billing")
                .contains("technical")
                .contains("application crashes immediately");

        assertThat(LlmsimClient.messagesText(calls.get(1)))
                .contains("technical support engineer")
                .contains("exact troubleshooting steps")
                .contains("application crashes immediately");
    }
}
