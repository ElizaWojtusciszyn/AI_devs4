package aidevs.course.s01e02.clients.location;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("${api.location}")
public class LocationRestClientConfiguration {

    String baseUrl;
}
