package aidevs.course.solution;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SolutionResponse {

    @JsonProperty("apikey")
    private final String apiKey;

    @JsonProperty("task")
    private final String task;


    protected SolutionResponse(String apiKey, String task) {
        this.apiKey = apiKey;
        this.task = task;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getTask() {
        return task;
    }
}
