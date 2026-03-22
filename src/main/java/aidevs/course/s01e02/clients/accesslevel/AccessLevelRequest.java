package aidevs.course.s01e02.clients.accesslevel;

import lombok.Builder;

@Builder
public record AccessLevelRequest(

        String apikey,
        String name,
        String surname,
        String birthYear
) {
}
