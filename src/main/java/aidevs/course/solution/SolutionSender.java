package aidevs.course.solution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class SolutionSender {

    private final RestClient restClient;
    @Value("${spring.hub.url}")
    private String hubUrl;

    public SolutionSender(
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder.build();
    }

    public String send(SolutionResponse response) {
        log.info("Wysyłanie odpowiedzi do {} [task={}]", hubUrl, response.getTask());

        String result = restClient.post()
                .uri(hubUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response)
                .retrieve()
                .body(String.class);

        log.info("Odpowiedź z hub: {}", result);
        return result;
    }
}
