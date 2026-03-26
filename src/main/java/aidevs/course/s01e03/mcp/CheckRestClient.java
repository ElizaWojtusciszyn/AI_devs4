package aidevs.course.s01e03.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class CheckRestClient {

    private final RestClient restClient;
    private final CheckRestClientConfiguration checkRestClientConfiguration;

    public CheckRestClient(RestClient.Builder builder,
                           CheckRestClientConfiguration config) {
        this.checkRestClientConfiguration = config;
        this.restClient = builder.baseUrl(config.baseUrl).build();
    }

    public String post(String apikey,
                       String action,
                       String packageid) {
        CheckRequest request = CheckRequest.builder()
                .apikey(apikey)
                .action(action)
                .packageid(packageid)
                .build();

        log.info("=== POST [%s] ===".formatted(request));

        return restClient.post()
                .body(request)
                .retrieve()
                .body(String.class);
    }


}
