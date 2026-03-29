package aidevs.course.s01e03.enpoint;

import aidevs.course.agents.ChatService;
import aidevs.course.history.ConversationHistory;
import aidevs.course.prompt.PromptLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessMessage {

    private static final Path SESSIONS_DIR = Path.of("src/main/resources/s01e03/sessions");

    private final ChatService chatService;
    private final PromptLoader promptLoader;
    private final ConversationHistory conversationHistory;

    public String process(Message message) throws IOException {
        conversationHistory.addEntry(SESSIONS_DIR, message.sessionID(), "user", message.message());

        String systemPrompt = promptLoader.load("prompts/s01e03/system-prompt.md",
                Map.of("sessionId", message.sessionID()));

        String result = chatService.chat(message.sessionID(), systemPrompt, message.message());

        conversationHistory.addEntry(SESSIONS_DIR, message.sessionID(), "assistant", result);
        log.info("Agent result: {}", result);

        return result;
    }
}
