package aidevs.course.prompt.Claude;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClaudeInputSchema(
        String type,
        Map<String, Object> properties,
        List<String> required
) {}
