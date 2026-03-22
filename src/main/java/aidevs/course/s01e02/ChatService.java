package aidevs.course.s01e02;

import aidevs.course.s01e02.tools.AccessLevelTool;
import aidevs.course.s01e02.tools.LocationTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final AccessLevelTool accessLevelTool;
    private final LocationTool locationTool;

    @Autowired
    public ChatService(ChatClient.Builder builder,
                       AccessLevelTool accessLevelTool,
                       LocationTool locationTool
    ) {
        this.chatClient = builder.build()
                .mutate()
                .defaultTools(accessLevelTool, locationTool)
                .build();
        this.accessLevelTool = accessLevelTool;
        this.locationTool = locationTool;
    }
}
