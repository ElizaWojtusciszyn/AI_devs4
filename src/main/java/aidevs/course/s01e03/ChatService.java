package aidevs.course.s01e03;

import aidevs.course.tools.ITool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder,
                       List<ITool> toolList,
                       SyncMcpToolCallbackProvider mcpToolCallbackProvider
    ) {
        this.chatClient = builder.build()
                .mutate()
                .defaultTools(toolList)
                .defaultTools(mcpToolCallbackProvider)
                .build();
    }

    public String chat(String systemPrompt, String userMessage) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
}
