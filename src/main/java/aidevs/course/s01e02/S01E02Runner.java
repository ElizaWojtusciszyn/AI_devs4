package aidevs.course.s01e02;

import aidevs.course.LessonRunner;
import aidevs.course.solution.SolutionSender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e02")
public class S01E02Runner implements LessonRunner {

    private static final Logger log = LoggerFactory.getLogger(S01E02Runner.class);

    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final SolutionSender solutionSender;
    private final RestClient restClient;
    private final String apiKey;

    public S01E02Runner(
            ChatService chatService,
            ObjectMapper objectMapper,
            SolutionSender solutionSender,
            RestClient.Builder restClientBuilder,
            @Value("${spring.hub.key}") String apiKey
    ) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
        this.solutionSender = solutionSender;
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
    }

    @Override
    public void run() throws IOException {
        log.info("=== S01E02: Find Him ===");

        List<Person> people = objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream("s01e02/data.json"),
                new TypeReference<List<Person>>() {}
        );
        log.info("Suspects: {}", people);

        String powerPlantsJson = fetchPowerPlants();
        log.info("Power plants fetched");

        String suspectsInfo = people.stream()
                .map(p -> "- " + p.getName() + " " + p.getSurname() + ", born " + p.getBorn())
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
                You are an investigative agent. Find which suspect was seen near a nuclear power plant.

                API key for all tool calls: %s

                Suspects:
                %s

                Nuclear power plants (JSON with codes and coordinates):
                %s

                Steps:
                1. For each suspect, call the 'location' tool to get their location history.
                2. For each returned coordinate and each power plant, call 'calculateDistance' to compute the distance in km.
                3. Find which suspect was closest to any power plant.
                4. Call 'accessLevel' for that suspect using their birthYear from the list above.
                5. Respond with ONLY a JSON object — no explanation, no markdown:
                   {"name":"...","surname":"...","accessLevel":<number>,"powerPlant":"PWRxxxxPL"}
                """.formatted(apiKey, suspectsInfo, powerPlantsJson);

        String result = chatService.chat(systemPrompt,
                "Find which suspect was near a nuclear power plant and return the answer JSON.");
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
                .uri("REMOVED/data/" + apiKey + "/findhim_locations.json")
                .retrieve()
                .body(String.class);
    }
}
