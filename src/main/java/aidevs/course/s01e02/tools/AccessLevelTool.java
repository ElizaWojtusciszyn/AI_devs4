package aidevs.course.s01e02.tools;

import aidevs.course.s01e02.clients.accesslevel.AccessLevelRestClient;
import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AccessLevelTool {

    private final AccessLevelRestClient client;

    @Tool(description = "Returns access level for given data")
    public String accessLevel(
            @ToolParam(description = "Individual api key")
            String apikey,
            @ToolParam(description = "Person name")
            String name,
            @ToolParam(description = "Person surname")
            String surname,
            @ToolParam(description = "Person birth year")
            String birthYear
    ) {
        return client.post(apikey, name, surname, birthYear);
    }
}
