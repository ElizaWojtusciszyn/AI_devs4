package aidevs.course.s01e01.solution;

import lombok.Builder;

import java.util.List;

@Builder
public record PersonAnswer(
        String name,
        String surname,
        String gender,
        int born,
        String city,
        List<String> tags
) {}
