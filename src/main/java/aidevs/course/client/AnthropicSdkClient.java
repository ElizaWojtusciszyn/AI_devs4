package aidevs.course.client;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ContentBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * PODEJŚCIE 1: Oficjalny Anthropic Java SDK
 * <p>
 * Zalety:
 * - Pełna kontrola nad parametrami Anthropic API
 * - Type-safe builder pattern
 * - Wsparcie dla sync i async (CompletableFuture)
 * - Automatyczne retry, timeout management przez OkHttp
 * <p>
 * Kiedy używać:
 * - Gdy potrzebujesz specyficznych funkcji Claude (extended thinking, tool use)
 * - Gdy nie używasz Spring lub chcesz mieć minimalną liczbę zależności
 */

@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "anthropic-sdk")
public class AnthropicSdkClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicSdkClient.class);

    private final AnthropicClient client;
    private final LlmModel llmModel;
    private final long maxTokens;

    public AnthropicSdkClient(
            @Value("${anthropic.api-key}") String apiKey,
            @Value("${anthropic.model:anthropic/claude-sonnet-4-6}") String model,
            @Value("${anthropic.max-tokens:1024}") long maxTokens
    ) {
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
        this.llmModel = LlmModel.fromName(model);
        this.maxTokens = maxTokens;

        log.info("Zainicjalizowano AnthropicSdkClient [model={}]", model);
    }

    @Override
    public String chat(String userMessage) {
        return chat(null, userMessage, null);
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, userMessage, null);
    }

    @Override
    public String chat(String systemPrompt, String userMessage, String inputSchema) {
        log.debug("AnthropicSDK -> wysyłam zapytanie [system={}, user={}]",
                systemPrompt != null, userMessage.length());

        MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
                .model(Model.of(llmModel.getName()))
                .maxTokens(maxTokens)
                .addUserMessage(userMessage);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            paramsBuilder.addAssistantMessage(systemPrompt);
        }

        Message response = client.messages().create(paramsBuilder.build());


        return response.content().stream()
                .filter(ContentBlock::isText)
                .map(block -> block.asText().text())
                .findFirst()
                .orElse("");
    }

    @Override
    public String providerName() {
        return "Anthropic Java SDK v2.16.0";
    }
}
