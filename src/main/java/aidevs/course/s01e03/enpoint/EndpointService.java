package aidevs.course.s01e03.enpoint;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EndpointService {

    private ProcessMessage process;

    public MessageResponse process(MessageRequest messageRequest) {
        var message = Message.builder()
                .message(messageRequest.msg)
                .sessionID(messageRequest.sessionID)
                .build();
        var response = process.process(message);
        return new MessageResponse(response);
    }
}
