// global/jwt/StompHandler.java

package com.cagong.receiptpowerserver.global.jwt;

import com.cagong.receiptpowerserver.global.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP 연결 요청일 때만 토큰을 검증하고 사용자 정보를 세션에 저장합니다.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                String jwt = accessor.getFirstNativeHeader("Authorization");

                if (jwt != null && jwt.startsWith("Bearer ") && jwtUtil.validateToken(jwt.substring(7))) {
                    String token = jwt.substring(7);
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                    // [핵심] STOMP 세션 속성에 사용자 이름을 직접 저장합니다.
                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    if (sessionAttributes != null) {
                        sessionAttributes.put("username", userDetails.getUsername());
                        log.info("User connected and authenticated: {}", userDetails.getUsername());
                    }
                }
            } catch (Exception e) {
                log.error("Authentication error during WebSocket connect", e);
                // 여기서 예외를 던지면 클라이언트에게 오류가 전달됩니다.
                // throw new MessagingException("Authentication failed");
            }
        }
        return message;
    }
}