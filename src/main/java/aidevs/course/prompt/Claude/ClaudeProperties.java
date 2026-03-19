package aidevs.course.prompt.Claude;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Schemat pojedynczej właściwości w input_schema narzędzia Claude.
 * <p>
 * Przykład dla pola prostego:   {@code new ClaudeProperties("integer", "Opis", null)}
 * Przykład dla pola tablicowego: {@code new ClaudeProperties("array", "Opis", Map.of("type","object", "properties", innerProps))}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClaudeProperties(
        String type,
        String description,
        Map<String, Object> items
) {
    public ClaudeProperties(String type, String description) {
        this(type, description, null);
    }
}
