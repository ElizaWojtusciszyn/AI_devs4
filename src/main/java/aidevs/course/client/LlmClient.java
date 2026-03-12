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
     * Wysyła wiadomość z systemowym promptem.
     *
     * @param systemPrompt instrukcja systemowa (rola, kontekst)
     * @param userMessage  treść wiadomości użytkownika
     * @return odpowiedź modelu
     */
    String chat(String systemPrompt, String userMessage,  String inputSchema);

    /**
     * Nazwa aktualnej implementacji – przydatna do logowania/debugowania.
     */
    String providerName();

}
