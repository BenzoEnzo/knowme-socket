package pl.benzo.enzo.know.me.socket.server.chat;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {
    private static final Logger loggerChatSessionService = LoggerFactory.getLogger(ChatSessionService.class);

    private static final String SESSION_PREFIX = "session:";
    private static final String TALKER_INDEX = "talker_index";
    private final RedisTemplate<String, Object> redisTemplate;

    public ChatSession createSession(ChatSession session) {
        final int randVal = new Random().nextInt(9873);
        final String randomSession = String.valueOf((session.getTalkerId1() + session.getTalkerId2()) * randVal);

        if (redisTemplate.opsForHash().hasKey(TALKER_INDEX, session.getTalkerId1().toString()) ||
                redisTemplate.opsForHash().hasKey(TALKER_INDEX, session.getTalkerId2().toString())) {
            throw new IllegalStateException("Jeden z talkerów już ma aktywną sesję.");
        }

        redisTemplate.opsForHash().put(SESSION_PREFIX + randomSession, "talkerId1", session.getTalkerId1());
        redisTemplate.opsForHash().put(SESSION_PREFIX + randomSession, "talkerId2", session.getTalkerId2());

        loggerChatSessionService.info("Stworzono sesje: " + randomSession);

        redisTemplate.opsForHash().put(TALKER_INDEX, session.getTalkerId1().toString(), randomSession);
        redisTemplate.opsForHash().put(TALKER_INDEX, session.getTalkerId2().toString(), randomSession);

        return ChatSession.builder()
                .talkerId1(session.getTalkerId1())
                .talkerId2(session.getTalkerId2())
                .sessionId(randomSession).build();
    }

    public void endSession(String sessionId) {
        Long talkerId1 = (Long) redisTemplate.opsForHash().get(SESSION_PREFIX + sessionId, "talkerId1");
        Long talkerId2 = (Long) redisTemplate.opsForHash().get(SESSION_PREFIX + sessionId, "talkerId2");

        redisTemplate.opsForHash().delete("talkers_index", talkerId1.toString());
        redisTemplate.opsForHash().delete("talkers_index", talkerId2.toString());

        loggerChatSessionService.info("Zamknieto sesje uzytkownika: " + talkerId1 + " oraz" + talkerId2);
    }

    public ChatSession findSessionByTalkerId(ChatSession session) {
        final String talkerId1 = session.getTalkerId1().toString();
        if (!redisTemplate.opsForHash().hasKey(TALKER_INDEX, talkerId1)) {
            return null;
        }

        final String sessionId = (String) redisTemplate.opsForHash().get(TALKER_INDEX, talkerId1);
        return ChatSession.builder()
                .sessionId(sessionId)
                .build();
    }

    public List<ChatSession> getAllSessions() {
        Set<String> sessionKeys = redisTemplate.keys(SESSION_PREFIX + "*");
        List<ChatSession> sessions = new ArrayList<>();

        if (sessionKeys != null) {
            for (String key : sessionKeys) {
                try {
                    Long talkerId1 = (Long) redisTemplate.opsForHash().get(key, "talkerId1");
                    Long talkerId2 = (Long) redisTemplate.opsForHash().get(key, "talkerId2");
                    String sessionId = key.substring(SESSION_PREFIX.length());

                    ChatSession session = ChatSession.builder()
                            .talkerId1(talkerId1)
                            .talkerId2(talkerId2)
                            .sessionId(sessionId)
                            .build();
                    sessions.add(session);
                } catch (Exception e) {
                    loggerChatSessionService.error("Błąd podczas pobierania informacji o sesji: " + key, e);
                }
            }
        }

        return sessions;
    }
}

