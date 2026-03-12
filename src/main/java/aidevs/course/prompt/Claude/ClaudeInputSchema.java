package aidevs.course.prompt.Claude;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaudeInputSchema {
    public String type;
    public Map<String, Object> properties;
    public List<String> required;

    public ClaudeInputSchema(String type, Map<String, Object> properties, List<String> required) {
        this.type = type;
        this.properties = properties;
        this.required = required;
    }
}
