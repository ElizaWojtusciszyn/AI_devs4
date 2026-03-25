package aidevs.course.s01e02.entities;

import aidevs.course.solution.SolutionResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FindHimSolutionResponse extends SolutionResponse {

    @JsonProperty("answer")
    private final FindHimAnswer answer;

    public FindHimSolutionResponse(String apiKey, FindHimAnswer answer) {
        super(apiKey, "findhim");
        this.answer = answer;
    }

    public FindHimAnswer getAnswer() {
        return answer;
    }

    public record FindHimAnswer(
            @JsonProperty("name") String name,
            @JsonProperty("surname") String surname,
            @JsonProperty("accessLevel") int accessLevel,
            @JsonProperty("powerPlant") String powerPlant
    ) {}
}
