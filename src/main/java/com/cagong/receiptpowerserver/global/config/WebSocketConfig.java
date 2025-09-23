package com.cagong.receiptpowerserver.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .withSockJS(); // SockJS를 사용하여 WebSocket을 지원하지 않는 브라우저에서도 연결 가능하게 합니다.
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버 -> 클라이언트로 메시지를 전송할 때 사용하는 prefix
        registry.enableSimpleBroker("/sub");
        // 클라이언트 -> 서버로 메시지를 전송할 때 사용하는 prefix
        registry.setApplicationDestinationPrefixes("/pub");
    }
}