package aidevs.course.s01e03.response;

import aidevs.course.solution.SolutionResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Proxy;

public class ProxySolutionResponse extends SolutionResponse {

    @JsonProperty("answer")
    private final ProxyAnswer answer;

    public ProxySolutionResponse(String apiKey, ProxyAnswer answer) {
        super(apiKey, "proxy");
        this.answer = answer;
    }

    public ProxyAnswer getAnswer() {
        return answer;
    }

    public record ProxyAnswer(
            @JsonProperty("url") String url,
            @JsonProperty("sessionID") String sessionID
    ) {
    }
}
