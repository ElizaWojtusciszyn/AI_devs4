package aidevs.course.s01e02;

import aidevs.course.s01e02.tools.AccessLevelTool;
import aidevs.course.s01e02.tools.CalculateLocationTool;
import aidevs.course.s01e02.tools.LocationTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService_2 {

    private final ChatClient chatClient;

    public ChatService_2(ChatClient.Builder builder,
                         AccessLevelTool accessLevelTool,
                         LocationTool locationTool,
                         CalculateLocationTool calculateLocationTool
    ) {
        this.chatClient = builder.build()
                .mutate()
                .defaultTools(accessLevelTool, locationTool, calculateLocationTool)
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
