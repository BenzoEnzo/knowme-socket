package pl.benzo.enzo.know.me.socket.server.chat;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Lazy
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> sessionGroups = new HashMap<>();

   // @Autowired
 //   private KafkaLogService kafkaLogService;

    public ChatWebSocketHandler() {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session)  {
        Map<String, Object> attributes = session.getAttributes();
        String sessionId;
        if (!attributes.containsKey("sessionId")) {
            String uri = String.valueOf(session.getUri());
            sessionId = uri.substring(uri.lastIndexOf('/') + 1);
            attributes.put("sessionId", sessionId);
        } else {
            sessionId = String.valueOf(attributes.get("sessionId"));
        }

        sessionGroups
                .computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(session);

//        sessionGroups.forEach((sesID,ogSession) ->
//                kafkaLogService.sendLog("KNOW_ME_SOCKET -> sesja: " + sesID));

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = String.valueOf(session.getAttributes().get("sessionId"));
        List<WebSocketSession> sessions = sessionGroups.get(sessionId);
        if (sessions != null) {
            for (WebSocketSession webSocketSession : sessions) {
//                kafkaLogService.sendLog("KNOW_ME_SOCKET wysłano wiadomość w czacie o ID: " + sessionId);
                webSocketSession.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = String.valueOf(session.getAttributes().get("sessionId"));
        List<WebSocketSession> sessions = sessionGroups.get(sessionId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }
}
