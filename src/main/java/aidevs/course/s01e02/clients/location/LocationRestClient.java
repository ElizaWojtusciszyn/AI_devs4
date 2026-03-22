package aidevs.course.s01e02.clients.location;

import aidevs.course.s01e02.S01E02Runner;
import aidevs.course.s01e02.clients.accesslevel.AccessLevelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LocationRestClient {

    private static final Logger log = LoggerFactory.getLogger(S01E02Runner.class);
    private final RestClient restClient;
    private final LocationRestClientConfiguration locationRestClientConfiguration;

    public LocationRestClient(RestClient.Builder builder,
                              LocationRestClientConfiguration config) {
        this.locationRestClientConfiguration = config;
        this.restClient = builder.baseUrl(config.baseUrl).build();
    }

    public String post(String apikey,
                       String name,
                       String surname) {
        AccessLevelRequest request = AccessLevelRequest.builder()
                .apikey(apikey)
                .name(name)
                .surname(surname)
                .build();

        log.info("=== POST [{s}] ===".formatted(request));

        return restClient.post()
                .uri(locationRestClientConfiguration.baseUrl)
                .body(request)
                .retrieve()
                .body(String.class);
    }


}
