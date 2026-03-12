package aidevs.course.prompt.Claude;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaudeRequest {

    public String model;

    @JsonProperty("max_tokens")
    public int maxTokens;

    public String system;

    public List<ClaudeMessage> messages;

    public List<ClaudeTool> tools;

    @JsonProperty("tool_choice")
    public ClaudeToolChoice toolChoice;
}
