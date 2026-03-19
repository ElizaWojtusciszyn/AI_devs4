package aidevs.course.saver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generyczny serwis zapisujący wyniki pipeline'ów do plików JSON.
 * Format nazwy: {lesson}_{pipelineName}_{yyyyMMdd}_{HHmmss}.json
 * Ścieżka: src/main/resources/{lesson}/
 */
@Component
public class PipelineResultSaver {

    private static final Logger log = LoggerFactory.getLogger(PipelineResultSaver.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final String lesson;

    public PipelineResultSaver(@Value("${spring.hub.lesson}") String lesson) {
        this.lesson = lesson;
    }

    public Path save(String pipelineName, String json) throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        String filename = lesson + "_" + pipelineName + "_" + timestamp + ".json";

        Path dir = resolveResourcesDir().resolve(lesson);
        Files.createDirectories(dir);

        Path file = dir.resolve(filename);
        Files.writeString(file, json, StandardCharsets.UTF_8);

        log.info("Zapisano wynik pipeline '{}' do: {}", pipelineName, file.toAbsolutePath());
        return file;
    }

    private Path resolveResourcesDir() {
        // Szuka src/main/resources względem katalogu roboczego projektu
        Path workDir = Path.of(System.getProperty("user.dir"));
        Path resources = workDir.resolve("src/main/resources");
        if (Files.exists(resources)) {
            return resources;
        }
        // Fallback: katalog roboczy
        return workDir;
    }
}
