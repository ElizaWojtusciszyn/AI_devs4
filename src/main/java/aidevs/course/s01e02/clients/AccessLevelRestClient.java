package aidevs.course.s01e02.clients;

import aidevs.course.s01e02.S01E02Runner;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AccessLevelRestClient {

    private static final Logger log = LoggerFactory.getLogger(S01E02Runner.class);
    private final RestClient restClient;
    private final AccessLevelRestClientConfiguration accessLevelRestClientConfiguration;

    public AccessLevelRestClient(RestClient.Builder builder,
                                 AccessLevelRestClientConfiguration config) {
        this.accessLevelRestClientConfiguration = config;
        this.restClient = builder.baseUrl(config.baseUrl).build();
    }

    public String post(String apikey,
                       String name,
                       String surname,
                       String birthYear) {
        AccessLevelRequest request = AccessLevelRequest.builder()
                .apikey(apikey)
                .name(name)
                .surname(surname)
                .birthYear(birthYear)
                .build();

        log.info("=== POST [{s}] ===".formatted(request));

        return restClient.post()
                .uri(accessLevelRestClientConfiguration.baseUrl)
                .body(request)
                .retrieve()
                .body(String.class);
    }


}
