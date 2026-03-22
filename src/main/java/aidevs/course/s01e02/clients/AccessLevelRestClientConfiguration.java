package aidevs.course.s01e02.clients;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("${api.access-level}")
public class AccessLevelRestClientConfiguration {

    String baseUrl;
}
