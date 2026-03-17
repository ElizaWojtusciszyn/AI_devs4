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
            Jesteś asystentem do filtrowania CSV.
             
             Twoim zadaniem jest filtrowanie i przekształcanie wierszy z pliku CSV na podstawie podanych kryteriów.
             
             Plik CSV ma następujące kolumny:
             name, surname, gender, birthDate, birthPlace, birthCountry, job
             
             Zasady przekształcania:
             
             birthDate jest w formacie RRRR-MM-DD → zwróć tylko rok jako born (liczba całkowita)
             
             birthPlace → zwróć jako city
             
             Zachowaj name, surname, job oraz gender bez zmian
             
             Filtrowanie:
             
             Zwracaj tylko wiersze spełniające podane kryteria (określone w zapytaniu użytkownika)
             
             Format wyjścia:
             Zwróć obiekt JSON w postaci:
             {
             "filtered_rows": [
             {
             "name": "...",
             "surname": "...",
             "gender": "...",
             "born": 1974,
             "city": "...",
             "job": "..."
             }
             ]
             }
             
             Jeśli żaden wiersz nie spełnia kryteriów, zwróć:
             {
             "filtered_rows": []
             }
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
            log.info("PROMPT: {}", SYSTEM_PROMPT);

            JsonNode chunkJson;
            try {
                chunkJson = objectMapper.readTree(chunkResult);
            } catch (Exception e) {
                log.warn("Chunk {}/{} nie zwrócił JSON (brak dopasowań): {}", i + 1, chunks.size(), chunkResult);
                continue;
            }
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
