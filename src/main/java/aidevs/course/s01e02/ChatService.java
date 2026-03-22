package aidevs.course.s01e02;

import aidevs.course.s01e02.tools.AccessLevelTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final AccessLevelTool accessLevelTool;

    @Autowired
    public ChatService(ChatClient.Builder builder,
                       AccessLevelTool accessLevelTool
    ) {
        this.chatClient = builder.build()
                .mutate()
                .defaultTools(accessLevelTool)
                .build();
        this.accessLevelTool = accessLevelTool;
    }
}
