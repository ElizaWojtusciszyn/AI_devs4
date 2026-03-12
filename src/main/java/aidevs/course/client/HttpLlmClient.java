package aidevs.course.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * PODEJŚCIE 3: Bezpośrednie HTTP – java.net.http.HttpClient (JDK 11+)
 * <p>
 * Zalety:
 * - Zero dodatkowych zależności (tylko Jackson dla JSON)
 * - Pełna widoczność requestu/response – idealne do nauki API
 * - Maksymalna kontrola nad nagłówkami, timeoutami, retry
 * <p>
 * Kiedy używać:
 * - Projekty non-Spring z minimalnymi zależnościami
 * - Nauka i debugowanie Anthropic API
 * - Sytuacje gdy SDK ma zbyt dużo "magii"
 */

@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "http-client")
public class HttpLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(HttpLlmClient.class);
    private static final String CHAT_ENDPOINT = "/chat/completions";

    private final HttpClient httpClient;
    private final String apiKey;
    private final LlmModel llmModel;
    private final int maxTokens;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public HttpLlmClient(
            @Value("${anthropic.api-key}") String apiKey,
            @Value("${anthropic.max-tokens:1024}") int maxTokens,
            @Value("${anthropic.base-url:https://api.anthropic.com}") String baseUrl,
            @Value("${anthropic.model:claude-sonnet-4-6}") String modelName
    ) {

        this.apiKey = apiKey;
        this.maxTokens = maxTokens;
        this.baseUrl = baseUrl;
        this.llmModel = LlmModel.fromName(modelName);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();

        log.info("Zainicjalizowano HttpLlmClient [model={}, url={}]", llmModel.getName(), baseUrl);
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
        try {

            String requestBody = buildRequestBody(systemPrompt, userMessage, inputSchema);
            log.debug("HttpClient -> POST {}{}", baseUrl, CHAT_ENDPOINT);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + CHAT_ENDPOINT))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();


            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                log.error("API zwróciło błąd: status={}, body={}", response.statusCode(), response.body());
                throw new RuntimeException("Anthropic API error: " + response.statusCode() + " - " + response.body());
            }

            return parseResponse(response.body());

        } catch (Exception e) {
            log.error("Błąd podczas komunikacji z Anthropic API", e);
            throw new RuntimeException("LLM communication error", e);
        }
    }

    @Override
    public String providerName() {
        return "Raw HTTP (java.net.http.HttpClient)";
    }

    private String buildRequestBody(String systemPrompt, String userMessage, String inputSchema) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", llmModel.getName());
        root.put("max_tokens", maxTokens);

        ArrayNode messages = root.putArray("messages");

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            ObjectNode sysMsg = messages.addObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
        }

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        return objectMapper.writeValueAsString(root);
    }

    private String parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        return root.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }
}
