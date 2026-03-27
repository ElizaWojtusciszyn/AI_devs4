package aidevs.course.s01e03;

import aidevs.course.LessonRunner;
import aidevs.course.s01e03.enpoint.Message;
import aidevs.course.s01e03.enpoint.ProcessMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e03")
@Slf4j
public class S01E03Runner implements LessonRunner {

    private final ProcessMessage process;
    private final String apiKey;

    public S01E03Runner(
            ProcessMessage process,
            @Value("${spring.hub.key}") String apiKey
    ) {
        this.process = process;
        this.apiKey = apiKey;
    }

    @Override
    public void run() throws IOException {
        log.info("=== S01E03: Find Him ===");

        process.process(Message.builder()
                .message("Hello")
                .sessionID("Test")
                .build());
    }
}
