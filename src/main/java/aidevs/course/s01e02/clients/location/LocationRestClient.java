package aidevs.course.s01e02.clients.location;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class LocationRestClient {

    private final RestClient restClient;

    public LocationRestClient(RestClient.Builder builder, LocationRestClientConfiguration config) {
        this.restClient = builder.baseUrl(config.baseUrl).build();
    }

    public String post(String apikey, String name, String surname) {
        LocationRequest request = LocationRequest.builder()
                .apikey(apikey)
                .name(name)
                .surname(surname)
                .build();
        log.info("=== POST [{}] ===", request);
        return restClient.post().body(request).retrieve().body(String.class);
    }
}
