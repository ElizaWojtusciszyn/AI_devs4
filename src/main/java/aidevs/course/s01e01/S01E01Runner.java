package aidevs.course.s01e01;

import aidevs.course.LessonRunner;
import aidevs.course.s01e01.pipeline.CsvFilterService;
import aidevs.course.s01e01.pipeline.PersonTaggerService;
import aidevs.course.saver.PipelineResultSaver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e01")
public class S01E01Runner implements LessonRunner {

    private static final Logger log = LoggerFactory.getLogger(S01E01Runner.class);

    private static final String FILTER_CRITERIA = """
            Znajdź osoby spełniające WSZYSTKIE poniższe kryteria jednocześnie:
             1. gender = "M"
             2. Rok urodzenia z kolumny birthDate (format RRRR-MM-DD) musi być >= 1986 ORAZ <= 2006.
                Weź tylko pierwsze 4 znaki pola birthDate jako rok i porównaj liczbowo.
                ODRZUĆ każdą osobę, której rok urodzenia < 1986 lub > 2006.
             3. birthPlace = "Grudziądz" (dokładne dopasowanie)
             4. Opis w polu job wskazuje na branżę transportową (transport towarów, logistyka, kierowca, spedycja itp.)
            """;

    private final CsvFilterService csvFilterService;
    private final PersonTaggerService personTaggerService;
    private final PipelineResultSaver resultSaver;
    private final ObjectMapper objectMapper;

    public S01E01Runner(
            CsvFilterService csvFilterService,
            PersonTaggerService personTaggerService,
            PipelineResultSaver resultSaver,
            ObjectMapper objectMapper
    ) {
        this.csvFilterService = csvFilterService;
        this.personTaggerService = personTaggerService;
        this.resultSaver = resultSaver;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() throws IOException {
        log.info("=== S01E01: filtrowanie i tagowanie CSV ===");
        String filteredJson = filterAndSave();
        tagAndSave(filteredJson);
    }

    private String filterAndSave() throws IOException {
        String filteredJson = csvFilterService.filter(FILTER_CRITERIA);
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
