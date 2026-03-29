package aidevs.course.history;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
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
 * Per-session conversation history (debug log).
 * Each session stored as pretty-printed JSON: {baseDir}/{sessionId}.json
 * Format: [{"role":"user","content":"..."},{"role":"assistant","content":"..."}]
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationHistory {

    public record Entry(String role, String content) {}

    private final ObjectMapper objectMapper;

    public void addEntry(Path baseDir, String sessionId, String role, String content) throws IOException {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        List<Entry> history = load(baseDir, sessionId);
        history.add(new Entry(role, content));
        save(baseDir, sessionId, history);
        appendLog(baseDir, sessionId, role, content);
    }

    public List<Entry> load(Path baseDir, String sessionId) throws IOException {
        Path file = sessionFile(baseDir, sessionId);
        if (!Files.exists(file)) return new ArrayList<>();
        return objectMapper.readValue(file.toFile(), new TypeReference<>() {});
    }

    public void clear(Path baseDir, String sessionId) throws IOException {
        Files.deleteIfExists(sessionFile(baseDir, sessionId));
    }

    private void save(Path baseDir, String sessionId, List<Entry> history) throws IOException {
        objectMapper.writer(SerializationFeature.INDENT_OUTPUT)
                .writeValue(sessionFile(baseDir, sessionId).toFile(), history);
    }

    private void appendLog(Path baseDir, String sessionId, String role, String content) throws IOException {
        String entry = "[%s] [%s] [%s] %s%n".formatted(LocalDateTime.now(), sessionId, role, content);
        Files.writeString(baseDir.resolve("conversation.log"), entry,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private Path sessionFile(Path baseDir, String sessionId) {
        return baseDir.resolve(sessionId + ".json");
    }
}
