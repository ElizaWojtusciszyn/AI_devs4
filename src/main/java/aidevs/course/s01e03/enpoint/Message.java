package aidevs.course.s01e03.enpoint;

import lombok.Builder;

@Builder
public record Message(
        String sessionID,
        String message

) {

}
