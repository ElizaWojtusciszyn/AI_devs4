package aidevs.course.s01e02.clients.accesslevel;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api.access-level")
public class AccessLevelRestClientConfiguration {

    String baseUrl;
}
