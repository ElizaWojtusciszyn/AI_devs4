package aidevs.course.s01e02.clients.location;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api.location")
public class LocationRestClientConfiguration {

    String baseUrl;
}
