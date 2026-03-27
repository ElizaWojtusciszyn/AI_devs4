package aidevs.course.s01e02;

import aidevs.course.LessonRunner;
import aidevs.course.agents.ChatService;
import aidevs.course.prompt.PromptLoader;
import aidevs.course.s01e02.entities.FindHimSolutionResponse;
import aidevs.course.s01e02.entities.Person;
import aidevs.course.solution.SolutionSender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e02")
@Slf4j
public class S01E02Runner implements LessonRunner {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final SolutionSender solutionSender;
    private final PromptLoader promptLoader;
    private final RestClient restClient;
    private final String apiKey;
    private final String powerPlantsUrl;

    public S01E02Runner(
            ChatService chatService,
            ObjectMapper objectMapper,
            SolutionSender solutionSender,
            PromptLoader promptLoader,
            RestClient.Builder restClientBuilder,
            @Value("${spring.hub.key}") String apiKey,
            @Value("${api.s01e02.power-plants-url}") String powerPlantsUrl
    ) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
        this.solutionSender = solutionSender;
        this.promptLoader = promptLoader;
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.powerPlantsUrl = powerPlantsUrl;
    }

    @Override
    public void run() throws IOException {
        log.info("=== S01E02: Find Him ===");

        List<Person> people = objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream("s01e02/data.json"),
                new TypeReference<>() {}
        );
        log.info("Suspects: {}", people);

        String powerPlantsJson = fetchPowerPlants();
        log.info("Power plants fetched");

        String suspectsInfo = people.stream()
                .map(p -> "- " + p.getName() + " " + p.getSurname() + ", born " + p.getBorn())
                .collect(Collectors.joining("\n"));

        String systemPrompt = promptLoader.load("prompts/s01e02/system-prompt.md", Map.of(
                "apiKey", apiKey,
                "suspects", suspectsInfo,
                "powerPlants", powerPlantsJson
        ));

        String userPrompt = promptLoader.load("prompts/s01e02/user-prompt.md");

        String result = chatService.chat(systemPrompt, userPrompt);
        log.info("Agent result: {}", result);

        String jsonStr = result.contains("{")
                ? result.substring(result.indexOf('{'), result.lastIndexOf('}') + 1)
                : result;

        FindHimSolutionResponse.FindHimAnswer answer =
                objectMapper.readValue(jsonStr, FindHimSolutionResponse.FindHimAnswer.class);

        FindHimSolutionResponse response = new FindHimSolutionResponse(apiKey, answer);
        String verifyResult = solutionSender.send(response);
        log.info("Verification result: {}", verifyResult);
    }

    private String fetchPowerPlants() {
        return restClient.get()
                .uri(powerPlantsUrl, apiKey)
                .retrieve()
                .body(String.class);
    }
}
