package aidevs.course.s01e02.clients.location;

import lombok.Builder;

@Builder
public record LocationRequest(
        String apikey,
        String name,
        String surname
) {
}
