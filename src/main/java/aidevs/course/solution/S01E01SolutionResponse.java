package aidevs.course.solution;

import lombok.Getter;

import java.util.List;

@Getter
public class S01E01SolutionResponse extends SolutionResponse {

    private final List<PersonAnswer> answer;

    public S01E01SolutionResponse(String apiKey, List<PersonAnswer> answer) {
        super(apiKey, "people");
        this.answer = answer;
    }
}
