package aidevs.course.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class PromptLoader {

    private final ResourceLoader resourceLoader;

    public PromptLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String load(String classpathPath, Map<String, Object> variables) throws IOException {
        var resource = resourceLoader.getResource("classpath:" + classpathPath);
        String template = resource.getContentAsString(StandardCharsets.UTF_8);
        return new PromptTemplate(template).render(variables);
    }

    public String load(String classpathPath) throws IOException {
        var resource = resourceLoader.getResource("classpath:" + classpathPath);
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
