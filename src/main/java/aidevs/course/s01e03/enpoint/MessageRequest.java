package aidevs.course.s01e03.enpoint;

import lombok.Data;

@Data
public class MessageRequest {

    String sessionID;
    String msg;
}
