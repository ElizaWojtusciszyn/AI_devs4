package aidevs.course.s01e03.enpoint;

import aidevs.course.prompt.PromptLoader;
import aidevs.course.s01e02.entities.FindHimSolutionResponse;
import aidevs.course.s01e03.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessMessage {

    private final Map<String, List<String>> messageHistory = new HashMap<>();
    private final ChatService chatService;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public String process(Message message) throws IOException {

        if (messageHistory.containsKey(message.sessionID())) {
            var history = messageHistory.get(message.sessionID());
            history.add(message.message());
            messageHistory.replace(message.sessionID(), history);
        } else {
            messageHistory.put(message.sessionID(), List.of(message.message()));
        }

        String systemPrompt = promptLoader.load("s01e03/system-prompt.md", Map.of(
                "messageHistory", messageHistory.toString())
        );

        String result = chatService.chat(systemPrompt, message.message());
        log.info("Agent result: {}", result);


        String jsonStr = result.contains("{")
                ? result.substring(result.indexOf('{'), result.lastIndexOf('}') + 1)
                : result;

        String answer =
                objectMapper.readValue(jsonStr, FindHimSolutionResponse.FindHimAnswer.class);

        return answer;

    }
}
