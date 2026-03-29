package aidevs.course.s01e03;

import aidevs.course.LessonRunner;
import aidevs.course.s01e03.response.ProxySolutionResponse;
import aidevs.course.solution.SolutionSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01e03")
@RequiredArgsConstructor
@Slf4j
public class S01E03Runner implements LessonRunner {

    private final SolutionSender solutionSender;

    @Value("${spring.hub.key}")
    private String apiKey;

    @Value("${spring.hub.endpoint-url}")
    private String endpointUrl;

    @Override
    public void run() {
        log.info("=== S01E03: Proxy ===");
        log.info("Endpoint URL: {}", endpointUrl);

        var answer = new ProxySolutionResponse.ProxyAnswer(endpointUrl, "proxy-session-s01e03");
        var response = new ProxySolutionResponse(apiKey, answer);

        String result = solutionSender.send(response);
        log.info("Hub response: {}", result);
    }
}
