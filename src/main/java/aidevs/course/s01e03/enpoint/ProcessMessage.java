package aidevs.course.s01e03.enpoint;

import aidevs.course.history.ConversationHistory;
import aidevs.course.prompt.PromptLoader;
import aidevs.course.s01e03.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessMessage {

    private static final Path SESSIONS_DIR = Path.of("src/main/resources/s01e03/sessions");

    private final ChatService chatService;
    private final PromptLoader promptLoader;
    private final ConversationHistory conversationHistory;
    @Value("${spring.hub.key}")
    private String apiKey;

    public String process(Message message) throws IOException {
        List<String> history = conversationHistory.add(SESSIONS_DIR, message.sessionID(), message.message());

        String systemPrompt = promptLoader.load("s01e03/system-prompt.md", Map.of(
                "apiKey", apiKey,
                "messageHistory", history.toString())
        );

        String result = chatService.chat(systemPrompt, message.message());
        log.info("Agent result: {}", result);

        return result;
    }
}
