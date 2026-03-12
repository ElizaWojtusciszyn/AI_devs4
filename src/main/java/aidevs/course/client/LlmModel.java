package aidevs.course.client;

import java.util.Arrays;

public enum LlmModel {
    // ── Claude (Anthropic) ────────────────────────────────────────
    // Claude 4
    CLAUDE_SONNET_4_6("claude-sonnet-4-6"),
    CLAUDE_OPUS_4_6("claude-opus-4-6"),
    // Claude 3.7
    CLAUDE_SONNET_3_7("claude-3-7-sonnet-20250219"),
    // Claude 3.5
    CLAUDE_SONNET_3_5("claude-3-5-sonnet-20241022"),
    CLAUDE_HAIKU_3_5("claude-3-5-haiku-20241022"),
    // Claude 3
    CLAUDE_OPUS_3("claude-3-opus-20240229"),
    CLAUDE_HAIKU_3("claude-3-haiku-20240307"),

    // ── GPT (OpenAI) ──────────────────────────────────────────────
    // Flagship
    GPT_4O("gpt-4o"),
    GPT_4O_MINI("gpt-4o-mini"),
    GPT_4_1("gpt-4.1"),
    GPT_4_1_MINI("gpt-4.1-mini"),
    GPT_4_1_NANO("gpt-4.1-nano"),
    // Reasoning (o-series)
    O3("o3"),
    O3_PRO("o3-pro"),
    O4_MINI("o4-mini"),

    // ── Gemini (Google) ───────────────────────────────────────────
    // Gemini 2.5
    GEMINI_2_5_PRO("gemini-2.5-pro"),
    GEMINI_2_5_FLASH("gemini-2.5-flash"),           // Flash – price/perf
    GEMINI_2_5_FLASH_LITE("gemini-2.5-flash-lite"), // Flash Lite – najtańszy
    // Gemini 3 (preview)
    GEMINI_3_1_PRO_PREVIEW("gemini-3.1-pro-preview"),
    GEMINI_3_FLASH_PREVIEW("gemini-3-flash-preview"),
    GEMINI_3_1_FLASH_LITE_PREVIEW("gemini-3.1-flash-lite-preview");

    private final String name;

    LlmModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static LlmModel fromName(String name) {
        return Arrays.stream(values())
                .filter(m -> m.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown model: " + name));
    }
}
