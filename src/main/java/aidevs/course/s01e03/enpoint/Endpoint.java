package aidevs.course.s01e03.enpoint;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/endpoint")
public class Endpoint {

    private final EndpointService service;

    @PostMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public MessageResponse processMessage(
            @RequestBody MessageRequest messageRequest
    ) throws IOException {
        log.info("Request {%s}".formatted(messageRequest.toString()));
        return service.process(messageRequest);
    }
}
