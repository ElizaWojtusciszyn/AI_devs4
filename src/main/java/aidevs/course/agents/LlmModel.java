package aidevs.course.agents;

import java.util.Arrays;

public enum LlmModel {
    // ── Claude (Anthropic) ────────────────────────────────────────
    // Claude 4
    CLAUDE_SONNET_4_6("anthropic/claude-sonnet-4-6"),
    CLAUDE_OPUS_4_6("anthropic/claude-opus-4-6"),
    CLAUDE_HAIKU_4_5("anthropic/claude-haiku-4-5"),
    // Claude 3.7
    CLAUDE_SONNET_3_7("anthropic/claude-3.7-sonnet"),
    // Claude 3.5
    CLAUDE_SONNET_3_5("anthropic/claude-3.5-sonnet"),
    CLAUDE_HAIKU_3_5("anthropic/claude-3.5-haiku"),
    // Claude 3
    CLAUDE_OPUS_3("anthropic/claude-3-opus"),
    CLAUDE_HAIKU_3("anthropic/claude-3-haiku"),

    // ── GPT (OpenAI) ──────────────────────────────────────────────
    // Flagship
    GPT_4O("openai/gpt-4o"),
    GPT_4O_MINI("openai/gpt-4o-mini"),
    GPT_4_1("openai/gpt-4.1"),
    GPT_4_1_MINI("openai/gpt-4.1-mini"),
    GPT_4_1_NANO("openai/gpt-4.1-nano"),
    // Reasoning (o-series)
    O3("openai/o3"),
    O3_PRO("openai/o3-pro"),
    O4_MINI("openai/o4-mini"),

    // ── Gemini (Google) ───────────────────────────────────────────
    // Gemini 2.5
    GEMINI_2_5_PRO("google/gemini-2.5-pro-preview"),
    GEMINI_2_5_FLASH("google/gemini-2.5-flash-preview"),    // Flash – price/perf
    GEMINI_2_5_FLASH_LITE("google/gemini-2.5-flash-lite"),  // Flash Lite – najtańszy
    // Gemini 3 (preview)
    GEMINI_3_1_PRO_PREVIEW("google/gemini-3.1-pro-preview"),
    GEMINI_3_FLASH_PREVIEW("google/gemini-3-flash-preview"),
    GEMINI_3_1_FLASH_LITE_PREVIEW("google/gemini-3.1-flash-lite-preview");

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
