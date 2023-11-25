package pl.benzo.enzo.know.me.socket.server.chat;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ChatSession {
    private String sessionId;
    private Long talkerId1;
    private Long talkerId2;
}

