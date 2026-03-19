package aidevs.course.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "spring-ai")
public class SpringAiClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(SpringAiClient.class);

    private final ChatClient client;

    public SpringAiClient(ChatClient.Builder builder) {
        this.client = builder.build();
        log.info("Zainicjalizowano SpringAiClient");
    }

    @Override
    public String chat(String userMessage) {
        log.debug("SpringAI -> wysyłam zapytanie [user.len={}]", userMessage.length());

        return client.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        log.debug("SpringAI -> wysyłam zapytanie [system={}, user.len={}]",
                systemPrompt != null, userMessage.length());


        var prompt = client.prompt();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            prompt = prompt.system(systemPrompt);
        }

        return prompt
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * Spring AI nie obsługuje natywnie surowego JSON narzędzi w tym formacie.
     * Parametr {@code inputSchema} jest ignorowany — użyj HttpLlmClient dla tool use.
     */
    @Override
    public String chat(String systemPrompt, String userMessage, String inputSchema) {
        log.warn("SpringAiClient: parametr inputSchema jest ignorowany — tool use wymaga HttpLlmClient");
        return chat(systemPrompt, userMessage);
    }

    @Override
    public String providerName() {
        return "Spring AI 1.0.3 (Anthropic)";
    }
}
