package aidevs.course.s01e01;

import aidevs.course.LessonRunner;
import aidevs.course.s01e01.pipeline.CsvFilterService;
import aidevs.course.s01e01.pipeline.PersonTaggerService;
import aidevs.course.saver.PipelineResultSaver;
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
    private final PersonTaggerService personTaggerService;
    private final PipelineResultSaver resultSaver;
    private final ObjectMapper objectMapper;

    public S01E01Runner(
            CsvFilterService csvFilterService,
            PersonTaggerService personTaggerService,
            PipelineResultSaver resultSaver,
            @Value("${spring.hub.key}") String apiKey,
            @Value("${spring.hub.task}") String task
    ) {
        this.csvFilterService = csvFilterService;
        this.personTaggerService = personTaggerService;
        this.resultSaver = resultSaver;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() throws Exception {
        log.info("=== S01E01: filtrowanie CSV ===");

        String filteredJson = csvFilterService.filter("""
                Znajdź osoby, które spełniają WSZYSTKIE poniższe kryteria:                                                                
                 - płeć (gender): M
                 - urodzeni między 1986 a 2006 rokiem (wiek 20–40 lat w 2026)
                 - miejsce urodzenia: Grudziądz
                 - zawód związany z transportem
                 Wszystkie warunki muszą być spełnione jednocześnie.
                """);

        JsonNode filteredAnswer = objectMapper.readTree(filteredJson);
        resultSaver.save("csv_filter_answer", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(filteredAnswer));
        log.info("Zapisano wynik filtrowania CSV");

        String taggedJson = personTaggerService.tag(filteredJson);
        resultSaver.save("tagger", taggedJson);
        log.info("Zapisano wynik taggera");
    }
}
