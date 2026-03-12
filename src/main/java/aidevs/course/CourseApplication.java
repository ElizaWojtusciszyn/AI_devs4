package aidevs.course;

import aidevs.course.client.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@SpringBootApplication
public class CourseApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(CourseApplication.class);

	private final LlmClient llmClient;
	private final Optional<LessonRunner> lessonRunner;

	public CourseApplication(LlmClient llmClient, Optional<LessonRunner> lessonRunner) {
		this.llmClient = llmClient;
		this.lessonRunner = lessonRunner;
	}

	public static void main(String[] args) {
		SpringApplication.run(CourseApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("=== Aktywny provider: {} ===", llmClient.providerName());

		if (lessonRunner.isPresent()) {
			lessonRunner.get().run();
		} else {
			log.warn("Brak aktywnego LessonRunner – ustaw spring.hub.lesson w application.yml");
		}
	}

}
