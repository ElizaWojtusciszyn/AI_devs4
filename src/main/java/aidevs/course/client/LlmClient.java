package aidevs.course.client;

public interface LlmClient {

    /**
     * Wysyła pojedynczą wiadomość i zwraca odpowiedź jako String.
     *
     * @param userMessage treść wiadomości użytkownika
     * @return odpowiedź modelu
     */
    String chat(String userMessage);

    /**
     * Wysyła wiadomość z systemowym promptem.
     *
     * @param systemPrompt instrukcja systemowa (rola, kontekst)
     * @param userMessage  treść wiadomości użytkownika
     * @return odpowiedź modelu
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * Wysyła wiadomość z systemowym promptem i definicją narzędzi (tool use).
     *
     * @param systemPrompt instrukcja systemowa (rola, kontekst)
     * @param userMessage  treść wiadomości użytkownika
     * @param toolsJson    JSON z definicją narzędzi (format Anthropic tools array)
     * @return odpowiedź modelu (JSON wyniku narzędzia lub tekst)
     */
    String chat(String systemPrompt, String userMessage, String toolsJson);

    /**
     * Nazwa aktualnej implementacji – przydatna do logowania/debugowania.
     */
    String providerName();

}
