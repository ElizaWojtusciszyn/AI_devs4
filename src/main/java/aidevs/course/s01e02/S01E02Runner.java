package aidevs.course.s01e02;

import aidevs.course.LessonRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e02")
public class S01E02Runner implements LessonRunner {

    private static final Logger log = LoggerFactory.getLogger(S01E02Runner.class);

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public S01E02Runner(
            ChatService chatService,
            ObjectMapper objectMapper
    ) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() throws IOException {
        log.info("=== S01E02: Find Him ===");
    }

}
