package aidevs.course.s01e01;

import aidevs.course.LessonRunner;
import aidevs.course.prompt.PromptLoader;
import aidevs.course.s01e01.pipeline.CsvFilterService;
import aidevs.course.s01e01.pipeline.PersonTaggerService;
import aidevs.course.saver.PipelineResultSaver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e01")
@RequiredArgsConstructor
@Slf4j
public class S01E01Runner implements LessonRunner {

    private final CsvFilterService csvFilterService;
    private final PersonTaggerService personTaggerService;
    private final PipelineResultSaver resultSaver;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;

    @Override
    public void run() throws IOException {
        log.info("=== S01E01: filtrowanie i tagowanie CSV ===");
        String filteredJson = filterAndSave();
        tagAndSave(filteredJson);
    }

    private String filterAndSave() throws IOException {
        String filterCriteria = promptLoader.load("prompts/s01e01/filter-criteria.md");
        String filteredJson = csvFilterService.filter(filterCriteria);
        JsonNode filteredAnswer = objectMapper.readTree(filteredJson);
        resultSaver.save("csv_filter_answer", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(filteredAnswer));
        log.info("Zapisano wynik filtrowania CSV");
        return filteredJson;
    }

    private void tagAndSave(String filteredJson) throws IOException {
        String taggedJson = personTaggerService.tag(filteredJson);
        resultSaver.save("tagger", taggedJson);
        log.info("Zapisano wynik taggera");
    }
}
