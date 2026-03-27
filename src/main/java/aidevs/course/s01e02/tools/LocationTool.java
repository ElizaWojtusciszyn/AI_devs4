package aidevs.course.s01e02.tools;

import aidevs.course.s01e02.clients.location.LocationRestClient;
import aidevs.course.tools.ITool;
import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LocationTool implements ITool {

    private final LocationRestClient client;

    @Tool(description = "Returns location for rest client for given data")
    public String location(
            @ToolParam(description = "Individual api key")
            String apikey,
            @ToolParam(description = "Person name")
            String name,
            @ToolParam(description = "Person surname")
            String surname
    ) {
        return client.post(apikey, name, surname);
    }
}
