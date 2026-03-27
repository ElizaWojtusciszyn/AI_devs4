package aidevs.course.s01e01.pipeline;

import aidevs.course.agents.LlmClient;
import aidevs.course.prompt.Claude.ClaudeInputSchema;
import aidevs.course.prompt.Claude.ClaudeProperties;
import aidevs.course.prompt.Claude.ClaudeTool;
import aidevs.course.prompt.PromptLoader;
import aidevs.course.saver.PipelineResultSaver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvFilterService {

    private final LlmClient llmClient;
    private final PipelineResultSaver resultSaver;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;

    public String filter(String criteria) throws IOException {
        String systemPrompt = promptLoader.load("prompts/s01e01/csv-filter-system.md");
        String csvContent = loadCsv();
        String toolsJson = buildToolsJson();

        String userMessage = "Filter the following CSV by this criterion: " + criteria + "\n\n" + csvContent;
        String result = llmClient.chat(systemPrompt, userMessage, toolsJson);

        resultSaver.save("csv_filter", result);
        return result;
    }

    private String loadCsv() throws IOException {
        return new ClassPathResource("s01e01/people.csv").getContentAsString(StandardCharsets.UTF_8);
    }

    private String buildToolsJson() throws IOException {
        Map<String, Object> rowProperties = Map.of(
                "name",    new ClaudeProperties("string", null),
                "surname", new ClaudeProperties("string", null),
                "gender",  new ClaudeProperties("string", null),
                "born",    new ClaudeProperties("integer", null),
                "city",    new ClaudeProperties("string", null),
                "job",     new ClaudeProperties("string", null, Map.of("type", "string"))
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
