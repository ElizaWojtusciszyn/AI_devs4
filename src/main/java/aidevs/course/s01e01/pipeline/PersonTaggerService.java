package aidevs.course.s01e01.pipeline;

import aidevs.course.agents.LlmClient;
import aidevs.course.prompt.Claude.ClaudeInputSchema;
import aidevs.course.prompt.Claude.ClaudeProperties;
import aidevs.course.prompt.Claude.ClaudeTool;
import aidevs.course.prompt.PromptLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PersonTaggerService {

    private static final List<String> AVAILABLE_TAGS = List.of(
            "IT", "transport", "edukacja", "medycyna",
            "praca z ludźmi", "praca z pojazdami", "praca fizyczna"
    );

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;

    public String tag(String filteredJson) throws IOException {
        String systemPrompt = promptLoader.load("prompts/s01e01/person-tagger-system.md");
        String toolsJson = buildToolsJson();
        String userMessage = """
                Dostępne tagi: %s

                Osoby do otagowania:
                %s

                Przypisz odpowiednie tagi do każdej osoby.
                """.formatted(AVAILABLE_TAGS, filteredJson);

        log.info("Tagging people with {} available tags", AVAILABLE_TAGS.size());
        String resultJson = llmClient.chat(systemPrompt, userMessage, toolsJson);
        log.info("Tagger response: {}", resultJson);
        return resultJson;
    }

    private String buildToolsJson() throws IOException {
        Map<String, Object> taggedPersonProperties = Map.of(
                "name",    new ClaudeProperties("string", null),
                "surname", new ClaudeProperties("string", null),
                "gender",  new ClaudeProperties("string", null),
                "born",    new ClaudeProperties("integer", null),
                "city",    new ClaudeProperties("string", null),
                "tags",    new ClaudeProperties("array", "Tagi przypisane do tej osoby",
                        Map.of("type", "string"))
        );

        Map<String, Object> schemaProperties = Map.of(
                "answer", new ClaudeProperties("array", "Lista osób z przypisanymi tagami",
                        Map.of("type", "object", "properties", taggedPersonProperties)),
                "total_count", new ClaudeProperties("integer", "Liczba przetworzonych osób")
        );

        ClaudeTool tool = new ClaudeTool(
                "answer",
                "Przypisuje tagi do każdej osoby na podstawie jej profilu i dostępnej listy tagów",
                new ClaudeInputSchema("object", schemaProperties, List.of("answer", "total_count"))
        );

        return objectMapper.writeValueAsString(List.of(tool));
    }
}
