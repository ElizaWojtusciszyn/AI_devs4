package aidevs.course;

import aidevs.course.client.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CourseApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(CourseApplication.class);

	private final LlmClient llmClient;

	public CourseApplication(LlmClient llmClient) {
		this.llmClient = llmClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(CourseApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("=== Aktywny provider: {} ===", llmClient.providerName());

		// --- Test 1: proste zapytanie ---
		String odpowiedz = llmClient.chat("Powiedz 'Cześć!' po polsku w jednym zdaniu.");
		log.info("Odpowiedź: {}", odpowiedz);

		// --- Test 2: zapytanie z system promptem ---
		String odpowiedz2 = llmClient.chat(
				"Jesteś ekspertem Java. Odpowiadaj zwięźle, max 2 zdania.",
				"Co to jest Spring AI?"
		);
		log.info("Odpowiedź z system promptem: {}", odpowiedz2);
	}

}
