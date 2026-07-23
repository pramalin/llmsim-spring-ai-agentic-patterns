package com.example.agentic;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import tools.jackson.databind.JsonNode;

@SpringBootTest(
        classes = Application.class,
        properties = "agentic.demo.run=false"
)
@ActiveProfiles("llmsim")
@EnabledIfEnvironmentVariable(named = "LLMSIM_BASE_URL", matches = ".+")
class ChainWorkflowLlmsimTest {

    private static final String LLMSIM_BASE_URL =
            System.getenv("LLMSIM_BASE_URL");

    private static final String EXPECTED_RESULT = """
            | Metric | Value |
            |:--|--:|
            | Customer Satisfaction | 92% |
            | Employee Satisfaction | 87% |
            | Product Adoption Rate | 78% |
            | Revenue Growth | 45% |
            | New User Acquisition Cost | $43 |
            | Operating Margin | 34% |
            | Market Share | 23% |
            | Customer Churn | 5% |
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
    void executesAllFourChainStagesInOrder() {
        String result = new ChainWorkflow(this.chatClientBuilder.build())
                .chain(Application.REPORT);

        assertThat(result.strip()).isEqualTo(EXPECTED_RESULT);

        JsonNode calls = this.llmsim.calls();
        assertThat(calls.size()).isEqualTo(4);

        for (int index = 0; index < 4; index++) {
            JsonNode call = calls.get(index);
            assertThat(call.path("sequence").asInt()).isEqualTo(index + 1);
            assertThat(call.path("stepIndex").asInt()).isEqualTo(index);
            assertThat(call.path("provider").asText()).isEqualTo("anthropic");
            assertThat(call.path("model").asText()).isEqualTo("llmsim-demo");
            assertThat(call.has("streamed"))
                    .as("journal entry should expose its response transport")
                    .isTrue();
            assertThat(call.path("streamed").asBoolean()).isFalse();
            assertThat(call.path("outcome").path("type").asText())
                    .isEqualTo("responded");
        }

        assertThat(LlmsimClient.messagesText(calls.get(0)))
                .contains("Q3 Performance Summary")
                .contains("customer satisfaction score rose to 92 points");

        assertThat(LlmsimClient.messagesText(calls.get(1)))
                .contains("92: customer satisfaction")
                .contains("$43: new user acquisition cost");

        assertThat(LlmsimClient.messagesText(calls.get(2)))
                .contains("92%: customer satisfaction")
                .contains("87%: employee satisfaction");

        assertThat(LlmsimClient.messagesText(calls.get(3)))
                .contains("92%: customer satisfaction")
                .contains("5%: customer churn");
    }
}
