package aidevs.course.s01e01.pipeline;

import aidevs.course.client.LlmClient;
import aidevs.course.prompt.Claude.ClaudeInputSchema;
import aidevs.course.prompt.Claude.ClaudeProperties;
import aidevs.course.prompt.Claude.ClaudeTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class CsvFilterService {

    private static final Logger log = LoggerFactory.getLogger(CsvFilterService.class);

    private static final String SYSTEM_PROMPT = """
            You are a CSV filtering assistant.
            Always use the filter_csv tool to return results.
            The birthDate column contains a date in YYYY-MM-DD format – return only the year as born (integer).
            The birthPlace column is the city of birth – return it as city.
            The job column contains a job description – extract 1-3 keywords from it as tags.
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public CsvFilterService(LlmClient llmClient) {
        this.llmClient = llmClient;
        this.objectMapper = new ObjectMapper();
    }

    public String filter(String criteria) throws Exception {
        String csvContent = loadCsv();
        String toolsJson = buildToolsJson();
        String userMessage = "Filter the following CSV by this criterion: " + criteria + "\n\n" + csvContent;

        log.info("Wywołanie LLM z {} znakami CSV", csvContent.length());

        String resultJson = llmClient.chat(SYSTEM_PROMPT, userMessage, toolsJson);

        log.info("Odpowiedź LLM: {}", resultJson);
        return resultJson;
    }

    private String loadCsv() throws Exception {
        ClassPathResource resource = new ClassPathResource("s01e01/people.csv");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    private String buildToolsJson() throws Exception {
        // Schemat pojedynczego wiersza wynikowego
        Map<String, Object> rowProperties = Map.of(
                "name",    new ClaudeProperties("string", null),
                "surname", new ClaudeProperties("string", null),
                "gender",  new ClaudeProperties("string", null),
                "born",    new ClaudeProperties("integer", null),
                "city",    new ClaudeProperties("string", null),
                "tags",    new ClaudeProperties("array", null,
                        Map.of("type", "string"))
        );

        Map<String, Object> schemaProperties = Map.of(
                "filtered_rows", new ClaudeProperties("array", "Przefiltrowane wiersze",
                        Map.of("type", "object", "properties", rowProperties)),
                "total_count", new ClaudeProperties("integer", "Liczba zwróconych wierszy")
        );

        ClaudeTool tool = new ClaudeTool(
                "filter_csv",
                "Filtruje dane CSV według podanych kryteriów i zwraca dopasowane wiersze",
                new ClaudeInputSchema("object", schemaProperties, List.of("filtered_rows", "total_count"))
        );

        return objectMapper.writeValueAsString(List.of(tool));
    }

}
