package aidevs.course.s01e03;

import aidevs.course.LessonRunner;
import aidevs.course.s01e03.enpoint.Message;
import aidevs.course.s01e03.enpoint.ProcessMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e03")
@Slf4j
public class S01E03Runner implements LessonRunner {

    private final ProcessMessage process;

    public S01E03Runner(
            ProcessMessage process
    ) {
        this.process = process;
    }

    @Override
    public void run() throws IOException {
        log.info("=== S01E03: Proxy ===");

        process.process(Message.builder()
                .message("Hello")
                .sessionID("Test")
                .build());
    }
}
