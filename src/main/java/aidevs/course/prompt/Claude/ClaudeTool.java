package aidevs.course.prompt.Claude;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClaudeTool(
        String name,
        String description,
        @JsonProperty("input_schema") ClaudeInputSchema inputSchema
) {}
