package aidevs.course.agents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
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
@Slf4j
public class HttpLlmClient implements LlmClient {

    private static final String MESSAGES_ENDPOINT = "/messages";

    private final HttpClient httpClient;
    private final String apiKey;
    private final LlmModel llmModel;
    private final int maxTokens;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public HttpLlmClient(
            @Value("${anthropic.api-key}") String apiKey,
            @Value("${anthropic.max-tokens}") int maxTokens,
            @Value("${anthropic.base-url}") String baseUrl,
            @Value("${anthropic.model}") String modelName,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.maxTokens = maxTokens;
        this.baseUrl = baseUrl;
        this.llmModel = LlmModel.fromName(modelName);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;

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
    public String chat(String systemPrompt, String userMessage, String tools) {
        try {
            log.debug("=== SYSTEM PROMPT ===\n{}", systemPrompt);
            log.debug("=== USER MESSAGE ===\n{}", userMessage);
            String requestBody = buildRequestBody(systemPrompt, userMessage, tools);
            log.debug("HttpClient -> POST {}{}", baseUrl, MESSAGES_ENDPOINT);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + MESSAGES_ENDPOINT))
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

    private String buildRequestBody(String systemPrompt, String userMessage, String tools) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", llmModel.getName());
        root.put("max_tokens", maxTokens);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            // Prompt caching: system jako array z cache_control
            ArrayNode systemArray = root.putArray("system");
            ObjectNode systemBlock = systemArray.addObject();
            systemBlock.put("type", "text");
            systemBlock.put("text", systemPrompt);
        }

        ArrayNode messages = root.putArray("messages");
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        if (tools != null && !tools.isBlank()) {
            JsonNode toolsNode = objectMapper.readTree(tools);
            root.set("tools", toolsNode);
            ObjectNode toolChoice = root.putObject("tool_choice");
            toolChoice.put("type", "any");
        }

        return objectMapper.writeValueAsString(root);
    }

    private String parseResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentArray = root.path("content");

        for (JsonNode block : contentArray) {
            String type = block.path("type").asText();
            if ("tool_use".equals(type)) {
                return objectMapper.writeValueAsString(block.path("input"));
            }
            if ("text".equals(type)) {
                return block.path("text").asText();
            }
        }

        return "";
    }
}
