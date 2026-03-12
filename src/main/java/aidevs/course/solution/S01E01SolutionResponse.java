package aidevs.course.solution;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

@Getter
public class S01E01SolutionResponse extends SolutionResponse {

    private final JsonNode answer;

    public S01E01SolutionResponse(String apiKey, JsonNode answer) {
        super(apiKey, "people");
        this.answer = answer;
    }
}
