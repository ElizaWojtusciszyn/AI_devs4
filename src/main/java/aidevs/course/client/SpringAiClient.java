package aidevs.course.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "spring-ai")
public class SpringAiClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(SpringAiClient.class);

    private final ChatClient client;
    private final ObjectMapper objectMapper;

    public SpringAiClient(
            ChatClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.base-url}") String baseUrl
    ) {
        this.client = builder.build();
        this.objectMapper = objectMapper;
        log.info("Zainicjalizowano SpringAiClient [base-url={}]", baseUrl);
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
     * Spring AI nie obsługuje natywnie surowego JSON narzędzi.
     * Zamiast tool use, wstrzykujemy schemat do system promptu i żądamy odpowiedzi w JSON.
     */
    @Override
    public String chat(String systemPrompt, String userMessage, String inputSchema) {
        if (inputSchema == null || inputSchema.isBlank()) {
            return chat(systemPrompt, userMessage);
        }

        try {
            JsonNode toolsNode = objectMapper.readTree(inputSchema);
            JsonNode firstTool = toolsNode.get(0);
            String toolName = firstTool.path("name").asText();
            JsonNode schema = firstTool.path("input_schema");

            String jsonInstruction = String.format(
                    "\n\n## Format wyjścia\nZwróć WYŁĄCZNIE poprawny obiekt JSON zgodny ze schematem narzędzia \"%s\":\n%s\nBez żadnych wyjaśnień — tylko sam JSON.",
                    toolName,
                    objectMapper.writeValueAsString(schema)
            );

            String enhancedSystem = (systemPrompt != null ? systemPrompt : "") + jsonInstruction;
            String raw = chat(enhancedSystem, userMessage);
            return stripMarkdownFences(raw);

        } catch (Exception e) {
            log.error("Błąd parsowania inputSchema — fallback do zwykłego chatu", e);
            return chat(systemPrompt, userMessage);
        }
    }

    private String stripMarkdownFences(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1).trim();
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.lastIndexOf("```")).trim();
            }
        }
        return trimmed;
    }

    @Override
    public String providerName() {
        return "Spring AI (OpenRouter → anthropic/claude-sonnet-4-6)";
    }
}
