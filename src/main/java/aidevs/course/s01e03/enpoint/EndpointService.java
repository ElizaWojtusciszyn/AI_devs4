package aidevs.course.s01e03.enpoint;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class EndpointService {

    private ProcessMessage process;

    public MessageResponse process(MessageRequest messageRequest) throws IOException {
        var message = new Message(messageRequest.sessionID(), messageRequest.msg());
        var response = process.process(message);
        return new MessageResponse(response);
    }
}
