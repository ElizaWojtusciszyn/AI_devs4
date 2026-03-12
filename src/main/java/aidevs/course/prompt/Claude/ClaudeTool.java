package aidevs.course.prompt.Claude;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClaudeTool {
    public String name;
    public String description;

    @JsonProperty("input_schema")
    public ClaudeInputSchema inputSchema;

    public ClaudeTool(String name, String description, ClaudeInputSchema inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
}
