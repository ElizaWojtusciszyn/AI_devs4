# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build
./mvnw clean install          # Unix
mvnw.cmd clean install        # Windows

# Run
./mvnw spring-boot:run

# Tests
./mvnw test
./mvnw test -Dtest=ClassName  # single test class
```

## Architecture

This is a **Spring Boot 4 / Java 21** app for an AI_DEVS course — each lesson is a self-contained "runner" that integrates with an external hub at `REMOVED`.

### Lesson Runner Pattern

The active lesson is selected by a single property:
```yaml
spring.hub.lesson: s01e02   # activates S01E02Runner
```

Each lesson is a `@Component` implementing `LessonRunner` with `@ConditionalOnProperty(name = "spring.hub.lesson", havingValue = "s01eXX")`. Only one runner is instantiated per run. New lessons follow this pattern.

### Pluggable LLM Clients

Three implementations of `LlmClient` exist, selected via:
```yaml
app.llm.provider: spring-ai   # or: anthropic-sdk | http-client
```

| Provider | Class | Notes |
|---|---|---|
| `spring-ai` | `SpringAiClient` | Spring AI abstraction via OpenRouter |
| `anthropic-sdk` | `AnthropicSdkClient` | Official Anthropic Java SDK (type-safe) |
| `http-client` | `HttpLlmClient` | Raw `java.net.http.HttpClient`, no framework |

`LlmClient` interface has three `chat(...)` overloads: user-only, system+user, and system+user+tools.

### Tool Use

Two styles are used depending on the LLM client:

- **Anthropic/HTTP style** (S01E01): manually build `ClaudeTool` records → serialize to JSON → pass as `toolsJson` string to `LlmClient.chat(...)`
- **Spring AI style** (S01E02): annotate methods with `@Tool` / `@ToolParam` → register with `ChatClient.Builder` in `ChatService` → Spring AI handles invocation automatically

> **Important Spring AI caveat:** tool methods with multiple primitive parameters fail at runtime (`MethodToolCallback` JSON deserialization bug). Always wrap parameters in a record:
> ```java
> public record MyRequest(@ToolParam(...) double x, @ToolParam(...) double y) {}
>
> @Tool(description = "...")
> public String myTool(MyRequest request) { ... }
> ```

### Prompt Management

`PromptLoader` (in `aidevs.course.prompt`) loads `.md` files from `src/main/resources/` and substitutes variables using Spring AI `PromptTemplate` syntax (`{variableName}`):

```java
promptLoader.load("s01e02/system-prompt.md", Map.of("apiKey", key, "suspects", info));
promptLoader.load("s01e02/user-prompt.md");  // no variables
```

Prompts live alongside their lesson's resources: `src/main/resources/s01eXX/`.

### External Integrations

- **Solution submission:** `SolutionSender` POSTs to `${spring.hub.url}` (REMOVED/verify)
- **Result persistence:** `PipelineResultSaver` writes timestamped JSON to `src/main/resources/{lesson}/`
- **REST clients:** built with Spring `RestClient.Builder`; lesson-specific clients have their own `*RestClientConfiguration` classes

### Key Configuration Properties

```yaml
spring.hub.key: <api-key>       # used in tool calls and solution submission
spring.hub.lesson: s01eXX       # selects active runner
spring.hub.task: <task-name>    # included in solution payload
app.llm.provider: spring-ai     # selects LLM client bean
spring.ai.openai.*              # Spring AI / OpenRouter config
anthropic.*                     # Anthropic SDK / HTTP client config
```

Secrets go in `application-local.properties` (gitignored), referenced as `${ENV_VAR}` placeholders in `application.yml`.
