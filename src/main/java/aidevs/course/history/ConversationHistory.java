package aidevs.course.history;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Universal per-session conversation history.
 * <p>
 * Each session is stored as a JSON file: {baseDir}/{sessionId}.json
 * Every added message is also appended to a shared log: {baseDir}/conversation.log
 */
@Component
@Slf4j
public class ConversationHistory {

    private final ObjectMapper objectMapper;

    public ConversationHistory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Loads existing history for the given session, appends the new message,
     * persists the updated list, and appends a log entry.
     *
     * @return updated history including the new message
     */
    public List<String> add(Path baseDir, String sessionId, String message) throws IOException {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }

        List<String> history = load(baseDir, sessionId);
        history.add(message);
        save(baseDir, sessionId, history);
        appendLog(baseDir, sessionId, message);

        return history;
    }

    /** Returns the current history for a session (empty list if none exists). */
    public List<String> load(Path baseDir, String sessionId) throws IOException {
        Path file = sessionFile(baseDir, sessionId);
        if (!Files.exists(file)) return new ArrayList<>();
        return objectMapper.readValue(file.toFile(), new TypeReference<>() {});
    }

    /** Overwrites the stored history for a session. */
    public void save(Path baseDir, String sessionId, List<String> history) throws IOException {
        objectMapper.writeValue(sessionFile(baseDir, sessionId).toFile(), history);
    }

    /** Deletes the session file. */
    public void clear(Path baseDir, String sessionId) throws IOException {
        Files.deleteIfExists(sessionFile(baseDir, sessionId));
    }

    private void appendLog(Path baseDir, String sessionId, String message) throws IOException {
        String entry = "[%s] [%s] %s%n".formatted(LocalDateTime.now(), sessionId, message);
        Files.writeString(baseDir.resolve("conversation.log"), entry,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private Path sessionFile(Path baseDir, String sessionId) {
        return baseDir.resolve(sessionId + ".json");
    }
}
