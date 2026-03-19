package aidevs.course.prompt.Claude;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PromptBuilder {

    //Load prompt form resources
    public static String buildPrompt(@Value("${spring.ai.openai.base-url}") String promptFile) throws IOException {
        return Files.readString(Path.of(promptFile));
    }

    //Load prompt form resources with variables
    public static String buildPrompt(@Value("${spring.ai.openai.base-url}") String promptFile,
                                     Map<String, String> variables) throws IOException {
        String result = Files.readString(Path.of(promptFile));
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
