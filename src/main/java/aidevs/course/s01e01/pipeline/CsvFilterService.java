package aidevs.course.s01e01.pipeline;

import aidevs.course.client.LlmClient;
import aidevs.course.saver.PipelineResultSaver;
import aidevs.course.prompt.Claude.ClaudeInputSchema;
import aidevs.course.prompt.Claude.ClaudeProperties;
import aidevs.course.prompt.Claude.ClaudeTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class CsvFilterService {

    private static final Logger log = LoggerFactory.getLogger(CsvFilterService.class);
    private static final String SYSTEM_PROMPT = """
            Jesteś asystentem do precyzyjnego filtrowania danych CSV.

            Kolumny wejściowe: name, surname, gender, birthDate, birthPlace, birthCountry, job

            ## Filtrowanie
            Uwzględnij WYŁĄCZNIE wiersze spełniające WSZYSTKIE kryteria podane przez użytkownika.
            Każde kryterium traktuj jako warunek obowiązkowy — pomiń wiersz, jeśli choć jedno nie jest spełnione.
            Przy kryterium liczbowym (np. rok urodzenia) wykonuj porównanie arytmetyczne, nie tekstowe.

            ## Przekształcenia wyjściowe
            - birthDate (format RRRR-MM-DD) → wyciągnij pierwsze 4 znaki i zwróć jako born (integer)
            - birthPlace → zwróć jako city
            - name, surname, gender, job → bez zmian

            ## Format wyjścia
            Użyj narzędzia filter_csv. Jeśli żaden wiersz nie pasuje, zwróć filtered_rows: [].
            """;

    private final LlmClient llmClient;
    private final PipelineResultSaver resultSaver;
    private final ObjectMapper objectMapper;

    public CsvFilterService(LlmClient llmClient, PipelineResultSaver resultSaver, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.resultSaver = resultSaver;
        this.objectMapper = objectMapper;
    }

    public String filter(String criteria) throws IOException {
        String csvContent = loadCsv();
        String toolsJson = buildToolsJson();

        String userMessage = "Filter the following CSV by this criterion: " + criteria + "\n\n" + csvContent;
        String result = llmClient.chat(SYSTEM_PROMPT, userMessage, toolsJson);

        resultSaver.save("csv_filter", result);
        return result;
    }

    private String loadCsv() throws IOException {
        ClassPathResource resource = new ClassPathResource("s01e01/people.csv");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    private String buildToolsJson() throws IOException {
        Map<String, Object> rowProperties = Map.of(
                "name",    new ClaudeProperties("string", null),
                "surname", new ClaudeProperties("string", null),
                "gender",  new ClaudeProperties("string", null),
                "born",    new ClaudeProperties("integer", null),
                "city",    new ClaudeProperties("string", null),
                "job",    new ClaudeProperties("string", null,
                        Map.of("type", "string"))
        );

        Map<String, Object> schemaProperties = Map.of(
                "filtered_rows", new ClaudeProperties("array", "Filtered rows matching the criteria",
                        Map.of("type", "object", "properties", rowProperties)),
                "total_count", new ClaudeProperties("integer", "Number of matched rows")
        );

        ClaudeTool tool = new ClaudeTool(
                "filter_csv",
                "Filters CSV data by given criteria and returns matching rows",
                new ClaudeInputSchema("object", schemaProperties, List.of("filtered_rows", "total_count"))
        );

        return objectMapper.writeValueAsString(List.of(tool));
    }
}
