package aidevs.course.s01e01.solution;

import aidevs.course.solution.SolutionResponse;
import com.fasterxml.jackson.databind.JsonNode;

public class S01E01SolutionResponse extends SolutionResponse {

    private final JsonNode answer;

    public S01E01SolutionResponse(String apiKey, String task, JsonNode answer) {
        super(apiKey, task);
        this.answer = answer;
    }

    public JsonNode getAnswer() {
        return answer;
    }
}
