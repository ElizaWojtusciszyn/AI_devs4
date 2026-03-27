package aidevs.course.s01e03;

import aidevs.course.LessonRunner;
import aidevs.course.s01e03.enpoint.Message;
import aidevs.course.s01e03.enpoint.ProcessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e03")
@RequiredArgsConstructor
@Slf4j
public class S01E03Runner implements LessonRunner {

    private final ProcessMessage process;

    @Override
    public void run() throws IOException {
        log.info("=== S01E03: Proxy ===");
        process.process(new Message("Test", "Hello"));
    }
}
