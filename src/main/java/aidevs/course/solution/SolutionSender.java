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
    private final String hubUrl;

    public SolutionSender(
            RestClient.Builder restClientBuilder,
            @Value("${spring.hub.url}") String hubUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.hubUrl = hubUrl;
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
