package aidevs.course.s01e03.tools;

import aidevs.course.tools.ITool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows the AI agent to persist key findings during a conversation session
 * (e.g. security codes, package IDs, reactor-related package identifiers).
 *
 * Data is stored in-memory, keyed by sessionId → (dataKey → value).
 */
@Component
@Slf4j
public class SaveDataTool implements ITool {

    // sessionId → (key → value)
    private final Map<String, Map<String, String>> store = new ConcurrentHashMap<>();

    public record SaveRequest(
            @ToolParam(description = "Session identifier (provided in the system prompt)") String sessionId,
            @ToolParam(description = "Short label describing what is being saved, e.g. 'security_code', 'package_id', 'reactor_package_id'") String key,
            @ToolParam(description = "The value to save") String value
    ) {}

    @Tool(description = """
            Save important data discovered during the conversation (security codes, package IDs, etc.).
            Use this whenever the operator reveals a security code or you identify a package related to reactor fuel.
            """)
    public String saveData(SaveRequest request) {
        store.computeIfAbsent(request.sessionId(), id -> new ConcurrentHashMap<>())
                .put(request.key(), request.value());
        log.info("[SaveDataTool] session={} key={} value={}", request.sessionId(), request.key(), request.value());
        return "Saved: %s = %s".formatted(request.key(), request.value());
    }

    @Tool(description = "Retrieve all data previously saved for a given session.")
    public String getSavedData(
            @ToolParam(description = "Session identifier") String sessionId
    ) {
        Map<String, String> sessionData = store.getOrDefault(sessionId, Collections.emptyMap());
        if (sessionData.isEmpty()) {
            return "No data saved for session: " + sessionId;
        }
        StringBuilder sb = new StringBuilder("Saved data for session ").append(sessionId).append(":\n");
        sessionData.forEach((k, v) -> sb.append("  ").append(k).append(" = ").append(v).append("\n"));
        return sb.toString();
    }

    /** Called by ProcessMessage to read a specific saved value (e.g. to pass to SolutionSender). */
    public String get(String sessionId, String key) {
        return store.getOrDefault(sessionId, Collections.emptyMap()).get(key);
    }
}
