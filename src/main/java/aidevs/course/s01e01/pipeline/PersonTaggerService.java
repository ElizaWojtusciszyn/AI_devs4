package aidevs.course.s01e01.pipeline;

import aidevs.course.client.LlmClient;
import aidevs.course.prompt.Claude.ClaudeInputSchema;
import aidevs.course.prompt.Claude.ClaudeProperties;
import aidevs.course.prompt.Claude.ClaudeTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PersonTaggerService {

    private static final Logger log = LoggerFactory.getLogger(PersonTaggerService.class);

    private static final String SYSTEM_PROMPT = """
            You are a tagging assistant.
            For each person in the list, assign all tags from the provided tag list that match their profile.
            Base your decisions on the person's job description, name, gender, age, and city.
            A person can have multiple tags or none.
            Always use the tag_people tool to return results.
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public PersonTaggerService(LlmClient llmClient) {
        this.llmClient = llmClient;
        this.objectMapper = new ObjectMapper();
    }

    private static final List<String> AVAILABLE_TAGS = List.of(
            "IT", "transport", "edukacja", "medycyna",
            "praca z ludźmi", "praca z pojazdami", "praca fizyczna"
    );

    public String tag(String filteredJson) throws Exception {
        String toolsJson = buildToolsJson();
        String userMessage = """
                Available tags: %s

                People to tag:
                %s

                Assign relevant tags to each person.
                """.formatted(AVAILABLE_TAGS, filteredJson);

        log.info("Tagging people with {} available tags", AVAILABLE_TAGS.size());

        String resultJson = llmClient.chat(SYSTEM_PROMPT, userMessage, toolsJson);

        log.info("Tagger response: {}", resultJson);
        return resultJson;
    }

    private String buildToolsJson() throws Exception {
        Map<String, Object> taggedPersonProperties = Map.of(
                "name",    new ClaudeProperties("string", null),
                "surname", new ClaudeProperties("string", null),
                "gender",  new ClaudeProperties("string", null),
                "born",    new ClaudeProperties("integer", null),
                "city",    new ClaudeProperties("string", null),
                "tags",    new ClaudeProperties("array", "Tags assigned to this person",
                        Map.of("type", "string"))
        );

        Map<String, Object> schemaProperties = Map.of(
                "answer", new ClaudeProperties("array", "List of people with assigned tags",
                        Map.of("type", "object", "properties", taggedPersonProperties)),
                "total_count", new ClaudeProperties("integer", "Number of people processed")
        );

        ClaudeTool tool = new ClaudeTool(
                "tag_people",
                "Assigns tags to each person based on their profile and the available tag list",
                new ClaudeInputSchema("object", schemaProperties, List.of("answer", "total_count"))
        );

        return objectMapper.writeValueAsString(List.of(tool));
    }
}
