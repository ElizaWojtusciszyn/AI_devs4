package aidevs.course.prompt.Claude;

public class ClaudeMessage {
    public String role;
    public String content;

    public ClaudeMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
