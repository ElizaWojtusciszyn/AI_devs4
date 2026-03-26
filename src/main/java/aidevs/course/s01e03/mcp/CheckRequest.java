package aidevs.course.s01e03.mcp;

import lombok.Builder;

@Builder
public record CheckRequest(

        String apikey,
        String action,
        String packageid
) {
}
