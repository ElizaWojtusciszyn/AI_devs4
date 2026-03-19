package aidevs.course.s01e01.pipeline;

import aidevs.course.client.LlmClient;
import aidevs.course.prompt.Claude.ClaudeInputSchema;
import aidevs.course.prompt.Claude.ClaudeProperties;
import aidevs.course.prompt.Claude.ClaudeTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class PersonTaggerService {

    private static final Logger log = LoggerFactory.getLogger(PersonTaggerService.class);

    private static final String SYSTEM_PROMPT = """
            Jesteś asystentem do tagowania osób na podstawie opisu stanowiska (pole job).

            ## Zasady tagowania
            - Rozważ każdy dostępny tag OSOBNO i przypisz go, jeśli opis stanowiska daje ku temu uzasadnienie.
            - Przypisuj WSZYSTKIE pasujące tagi — nie ograniczaj się do jednego.
            - Osoba może mieć wiele tagów lub żadnego.
            - Nie dodawaj tagów spoza dostępnej listy.

            ## Format wyjścia
            Użyj narzędzia answer. Przykład:
            {
              "answer": [
                { "name": "Jan", "surname": "Kowalski", "gender": "M", "born": 1987, "city": "Warszawa", "tags": ["transport", "praca z pojazdami", "praca fizyczna"] },
                { "name": "Anna", "surname": "Nowak", "gender": "F", "born": 1993, "city": "Gdańsk", "tags": ["medycyna", "praca z ludźmi"] }
              ]
            }
            
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public PersonTaggerService(LlmClient llmClient, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    private static final List<String> AVAILABLE_TAGS = List.of(
            "IT", "transport", "edukacja", "medycyna",
            "praca z ludźmi", "praca z pojazdami", "praca fizyczna"
    );

    public String tag(String filteredJson) throws IOException {
        String toolsJson = buildToolsJson();
        String userMessage = """
                Dostępne tagi: %s

                Osoby do otagowania:
                %s

                Przypisz odpowiednie tagi do każdej osoby.
                """.formatted(AVAILABLE_TAGS, filteredJson);

        log.info("Tagging people with {} available tags", AVAILABLE_TAGS.size());

        log.debug("PROMPT: {}", SYSTEM_PROMPT);

        String resultJson = llmClient.chat(SYSTEM_PROMPT, userMessage, toolsJson);

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
