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
    private static final String MESSAGES_ENDPOINT = "/v1/messages";

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
            @Value("${anthropic.model:anthropic/claude-4.6-sonnet}") String modelName
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
    public String chat(String systemPrompt, String userMessage, String tools) {
        try {

            String requestBody = buildRequestBody(systemPrompt, userMessage, tools);
            log.debug("HttpClient -> POST {}{}", baseUrl, MESSAGES_ENDPOINT);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + MESSAGES_ENDPOINT))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
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

    private String buildRequestBody(String systemPrompt, String userMessage, String tools) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", llmModel.getName());
        root.put("max_tokens", maxTokens);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            root.put("system", systemPrompt);
        }

        ArrayNode messages = root.putArray("messages");
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        if (tools != null && !tools.isBlank()) {
            JsonNode toolsNode = objectMapper.readTree(tools);
            root.set("tools", toolsNode);
            ObjectNode toolChoice = root.putObject("tool_choice");
            toolChoice.put("type", "auto");
        }

        return objectMapper.writeValueAsString(root);
    }

    private String parseResponse(String responseBody) throws Exception {
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
