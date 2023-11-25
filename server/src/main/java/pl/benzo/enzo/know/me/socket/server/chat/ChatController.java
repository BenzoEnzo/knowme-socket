package pl.benzo.enzo.know.me.socket.server.chat;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@Slf4j
@RequestMapping("/api/chat/session")
@RequiredArgsConstructor
@Lazy
public class ChatController {

    private final ChatSessionService chatSessionService;

    @PostMapping("/create")
    public ResponseEntity<ChatSession> createSession(@RequestBody ChatSession session) {
        return ResponseEntity.ok().body(chatSessionService.createSession(session));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteSession(@RequestBody ChatSession session) {
        chatSessionService.endSession(session.getSessionId());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/validate")
    public ResponseEntity<?> validateSession(@RequestBody ChatSession session){
        return ResponseEntity.ok().body(chatSessionService.findSessionByTalkerId(session));
    }

    @GetMapping(value = "/read-all")
    public ResponseEntity<?> getAllOfSessions(){
        return ResponseEntity.ok().body(chatSessionService.getAllSessions());
    }

}
