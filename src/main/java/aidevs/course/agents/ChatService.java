package aidevs.course.agents;

import aidevs.course.tools.ITool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ChatService {

    private static final int MAX_TOOL_ROUNDS = 6;

    private final ChatModel chatModel;
    private final MessageWindowChatMemory chatMemory;
    private final Map<String, ToolCallback> allToolCallbacks;
    private final OpenAiChatOptions toolOptions;

    public ChatService(ChatClient.Builder builder,
                       ChatModel chatModel,
                       List<ITool> toolList,
                       SyncMcpToolCallbackProvider mcpToolCallbackProvider) {
        this.chatModel = chatModel;

        var methodProvider = MethodToolCallbackProvider.builder()
                .toolObjects(toolList.toArray())
                .build();

        Map<String, ToolCallback> callbacks = new HashMap<>();
        for (ToolCallback cb : methodProvider.getToolCallbacks()) {
            callbacks.put(cb.getToolDefinition().name(), cb);
        }
        for (ToolCallback cb : mcpToolCallbackProvider.getToolCallbacks()) {
            callbacks.put(cb.getToolDefinition().name(), cb);
        }
        this.allToolCallbacks = callbacks;

        this.toolOptions = OpenAiChatOptions.builder()
                .toolCallbacks(new ArrayList<>(callbacks.values()))
                .internalToolExecutionEnabled(false)
                .build();

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(50)
                .build();
    }

    public String chat(String systemPrompt, String userMessage) {
        log.debug("=== SYSTEM PROMPT ===\n{}", systemPrompt);
        log.debug("=== USER MESSAGE ===\n{}", userMessage);
        return chatModel.call(new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage)
        ))).getResult().getOutput().getText();
    }

    public String chat(String sessionId, String systemPrompt, String userMessage) {
        log.debug("=== SESSION ===\n{}", sessionId);
        log.debug("=== SYSTEM PROMPT ===\n{}", systemPrompt);
        log.debug("=== USER MESSAGE ===\n{}", userMessage);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.addAll(chatMemory.get(sessionId));
        messages.add(new UserMessage(userMessage));

        chatMemory.add(sessionId, new UserMessage(userMessage));

        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            var response = chatModel.call(new Prompt(messages, toolOptions));
            AssistantMessage output = response.getResult().getOutput();
            messages.add(output);

            List<AssistantMessage.ToolCall> toolCalls = output.getToolCalls();

            if (toolCalls.isEmpty()) {
                chatMemory.add(sessionId, output);
                return output.getText();
            }

            if (round == MAX_TOOL_ROUNDS - 1) {
                log.warn("Max tool rounds ({}) reached for session {}", MAX_TOOL_ROUNDS, sessionId);
                chatMemory.add(sessionId, output);
                return output.getText() != null ? output.getText() : "Przekroczono limit iteracji narzędzi.";
            }

            List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();
            for (AssistantMessage.ToolCall toolCall : toolCalls) {
                ToolCallback callback = allToolCallbacks.get(toolCall.name());
                String result = callback != null
                        ? callback.call(toolCall.arguments())
                        : "Narzędzie '%s' nie zostało znalezione.".formatted(toolCall.name());
                log.info("[Round {}/{}] Tool: {} -> {}", round + 1, MAX_TOOL_ROUNDS, toolCall.name(), result);
                toolResponses.add(new ToolResponseMessage.ToolResponse(
                        toolCall.id(), toolCall.name(), result));
            }
            messages.add(ToolResponseMessage.builder().responses(toolResponses).build());
        }

        return "Przekroczono limit iteracji narzędzi.";
    }
}
