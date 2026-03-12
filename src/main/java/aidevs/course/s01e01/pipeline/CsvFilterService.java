package aidevs.course.s01e01.pipeline;

import aidevs.course.client.LlmClient;
import aidevs.course.saver.PipelineResultSaver;
import aidevs.course.prompt.Claude.ClaudeInputSchema;
import aidevs.course.prompt.Claude.ClaudeProperties;
import aidevs.course.prompt.Claude.ClaudeTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CsvFilterService {

    private static final Logger log = LoggerFactory.getLogger(CsvFilterService.class);
    private static final int CHUNK_SIZE = 150;

    private static final String SYSTEM_PROMPT = """
            You are a CSV filtering assistant.
            Always use the filter_csv tool to return results.
            The birthDate column contains a date in YYYY-MM-DD format – return only the year as born (integer).
            The birthPlace column is the city of birth – return it as city.
            The job column contains a job description – extract 1-3 keywords from it as tags.
            If no rows match the criteria, return an empty filtered_rows array.
            """;

    private final LlmClient llmClient;
    private final PipelineResultSaver resultSaver;
    private final ObjectMapper objectMapper;

    public CsvFilterService(LlmClient llmClient, PipelineResultSaver resultSaver) {
        this.llmClient = llmClient;
        this.resultSaver = resultSaver;
        this.objectMapper = new ObjectMapper();
    }

    public String filter(String criteria) throws Exception {
        List<String> chunks = splitCsvIntoChunks();
        String toolsJson = buildToolsJson();

        log.info("CSV podzielony na {} chunków po max {} wierszy", chunks.size(), CHUNK_SIZE);

        ArrayNode allRows = objectMapper.createArrayNode();

        for (int i = 0; i < chunks.size(); i++) {
            log.info("Przetwarzanie chunka {}/{}", i + 1, chunks.size());
            String userMessage = "Filter the following CSV by this criterion: " + criteria + "\n\n" + chunks.get(i);

            String chunkResult = llmClient.chat(SYSTEM_PROMPT, userMessage, toolsJson);
            JsonNode chunkJson = objectMapper.readTree(chunkResult);
            JsonNode rows = chunkJson.path("filtered_rows");

            for (JsonNode row : rows) {
                allRows.add(row);
            }
        }

        ObjectNode merged = objectMapper.createObjectNode();
        merged.set("filtered_rows", allRows);
        merged.put("total_count", allRows.size());

        String resultJson = objectMapper.writeValueAsString(merged);
        resultSaver.save("csv_filter", resultJson);
        return resultJson;
    }

    private List<String> splitCsvIntoChunks() throws IOException {
        ClassPathResource resource = new ClassPathResource("s01e01/people.csv");
        String csvContent = resource.getContentAsString(StandardCharsets.UTF_8);
        String[] lines = csvContent.split("\n");
        String header = lines[0];

        List<String> chunks = new ArrayList<>();
        List<String> currentChunk = new ArrayList<>();
        currentChunk.add(header);

        for (int i = 1; i < lines.length; i++) {
            currentChunk.add(lines[i]);
            if (currentChunk.size() - 1 == CHUNK_SIZE) {
                chunks.add(String.join("\n", currentChunk));
                currentChunk = new ArrayList<>();
                currentChunk.add(header);
            }
        }

        if (currentChunk.size() > 1) {
            chunks.add(String.join("\n", currentChunk));
        }

        return chunks;
    }

    private String buildToolsJson() throws Exception {
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
