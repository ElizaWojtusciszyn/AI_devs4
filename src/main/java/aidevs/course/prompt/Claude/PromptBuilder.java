package aidevs.course.prompt.Claude;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PromptBuilder {

    public static String buildPrompt(@Value("${spring.ai.openai.base-url}") String promptFile) throws IOException {
        return Files.readString(Path.of(promptFile));
    }
}
