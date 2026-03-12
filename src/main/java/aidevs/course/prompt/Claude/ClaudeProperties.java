package aidevs.course.prompt.Claude;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Reprezentuje schemat pojedynczej właściwości w input_schema.
 * Używany jako wartość w Map<String, Object> properties.
 *
 * Przykład dla pola tablicowego:
 *   new ClaudeProperty("array", "Opis", Map.of("type","object", "properties", innerProps))
 *
 * Przykład dla pola prostego:
 *   new ClaudeProperty("integer", "Opis")
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaudeProperties {
    public String type;
    public String description;
    public Map<String, Object> items;

    public ClaudeProperties(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public ClaudeProperties(String type, String description, Map<String, Object> items) {
        this.type = type;
        this.description = description;
        this.items = items;
    }
}
