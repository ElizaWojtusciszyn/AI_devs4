package aidevs.course.s01e01;

import aidevs.course.LessonRunner;
import aidevs.course.s01e01.pipeline.CsvFilterService;
import aidevs.course.s01e01.solution.S01E01SolutionResponse;
import aidevs.course.solution.SolutionSender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e01")
public class S01E01Runner implements LessonRunner {

    private static final Logger log = LoggerFactory.getLogger(S01E01Runner.class);

    private final CsvFilterService csvFilterService;
    private final SolutionSender solutionSender;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String task;

    public S01E01Runner(
            CsvFilterService csvFilterService,
            SolutionSender solutionSender,
            @Value("${spring.hub.key}") String apiKey,
            @Value("${spring.hub.task}") String task
    ) {
        this.csvFilterService = csvFilterService;
        this.solutionSender = solutionSender;
        this.apiKey = apiKey;
        this.task = task;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() throws Exception {
        log.info("=== S01E01: filtrowanie CSV ===");

        String json = csvFilterService.filter("""
                Find people who meet ALL of the following criteria:
                - gender: male
                - born between 1986 and 2006 (age 20–40 in 2026)
                - birthPlace: Grudziądz
                - job related to transportation industry
                All conditions must be satisfied simultaneously.
                """);
        JsonNode answer = objectMapper.readTree(json);

        String result = solutionSender.send(new S01E01SolutionResponse(apiKey, task, answer));
        log.info("Odpowiedź hub: {}", result);
    }
}
