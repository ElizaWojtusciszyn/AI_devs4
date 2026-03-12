package aidevs.course.solution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class SolutionResponse {

    @JsonProperty("apikey")
    private final String apiKey;

     @JsonProperty("task")
    private final String task;
}
