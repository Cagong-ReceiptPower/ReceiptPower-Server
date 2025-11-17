    package com.cagong.receiptpowerserver.domain.chat;

    import com.cagong.receiptpowerserver.domain.chat.dto.*;
    import com.cagong.receiptpowerserver.global.security.CustomUserDetails;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    // [!!] HttpStatus 임포트는 이제 GlobalChatExceptionHandler가 사용하므로 여기서 필요 없습니다.
    // import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

    import java.lang.reflect.Method;
    import java.util.List;
    // [!!] Map 임포트도 이제 DTO를 사용하므로 필요 없습니다.
    // import java.util.Map;

    @RestController
    @RequestMapping("/api/chatrooms")
    @RequiredArgsConstructor
    public class ChatRoomController {

        private final ChatRoomService chatRoomService;

        @PostMapping
        public ResponseEntity<ChatRoomResponse> create(
                @Valid @RequestBody ChatRoomCreateRequest request,
                Authentication authentication
        ) {
            Long authenticatedUserId = extractUserId(authentication);

            ChatRoomResponse response = chatRoomService.create(request, authenticatedUserId);

            return ResponseEntity.created(
                    ServletUriComponentsBuilder.fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(response.getId())
                            .toUri()
            ).body(response);
        }

        // 모든 활성화된 채팅방 조회
        @GetMapping
        public ResponseEntity<List<ChatRoomResponse>> getAllActiveRooms() {
            return ResponseEntity.ok(chatRoomService.getAllActiveRooms());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ChatRoomResponse> getById(@PathVariable Long id) {
            return ResponseEntity.ok(chatRoomService.getById(id));
        }

        @GetMapping("/me")
        public ResponseEntity<List<ChatRoomResponse>> getMyRooms(Authentication authentication) {
            Long authenticatedUserId = extractUserId(authentication);
            return ResponseEntity.ok(chatRoomService.getMyRooms(authenticatedUserId));
        }

        @PatchMapping("/{id}/status")
        public ResponseEntity<ChatRoomResponse> updateStatus(
                @PathVariable Long id,
                @Valid @RequestBody ChatRoomStatusUpdateRequest request,
                Authentication authentication
        ) {
            Long authenticatedUserId = extractUserId(authentication);
            return ResponseEntity.ok(
                    chatRoomService.updateStatus(id, authenticatedUserId, request.getStatus())
            );
        }

        /**
         * ✅ 2. 채팅 메시지 저장 API (과거 기록 불러오기)
         */
        @GetMapping("/{roomId}/messages")
        public ResponseEntity<List<ChatMessageResponse>> getChatRoomMessages(@PathVariable Long roomId) {
            return ResponseEntity.ok(chatRoomService.getMessages(roomId));
        }

        /**
         * ✅ 3 & 4. 채팅방 입장 API (인원 제한 로직은 서비스 레이어)
         * (try-catch가 제거된 리팩토링 완료 버전)
         */
        @PostMapping("/{id}/enter")
        public ResponseEntity<ChatParticipantCountResponse> enterChatRoom(@PathVariable Long id, Authentication authentication) {
            Long authenticatedUserId = extractUserId(authentication);

            ChatParticipantCountResponse response = chatRoomService.enterRoom(id, authenticatedUserId);
            return ResponseEntity.ok(response);
        }

        /**
         * ✅ 3. 채팅방 나가기 API
         */
        @PostMapping("/{id}/leave")
        public ResponseEntity<ChatParticipantCountResponse> leaveChatRoom(@PathVariable Long id, Authentication authentication) {
            Long authenticatedUserId = extractUserId(authentication);
            ChatParticipantCountResponse response = chatRoomService.leaveRoom(id, authenticatedUserId);
            return ResponseEntity.ok(response);
        }

        /**
         * ✅ 3. 현재 참여 중인 인원 가져오기
         */
        @GetMapping("/{id}/participants")
        public ResponseEntity<List<ChatParticipantResponse>> getChatRoomParticipants(@PathVariable Long id) {
            return ResponseEntity.ok(chatRoomService.getParticipants(id));
        }



        /**
         * 인증 주체에서 Long 타입의 사용자 ID를 추출합니다.
         */
        private Long extractUserId(Authentication authentication) {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthenticated");
            }

            Object principal = authentication.getPrincipal();

            // [수정된 부분]: CustomUserDetails 타입인지 확인하고 ID를 직접 추출
            if (principal instanceof CustomUserDetails customUserDetails) {
                return customUserDetails.getMember().getId();
            }

            if (principal instanceof UserDetails userDetails) {
                try {
                    return Long.parseLong(userDetails.getUsername());
                } catch (NumberFormatException ignored) {}
            }

            if (principal instanceof String s) {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException ignored) {}
            }

            try {
                Method getId = principal.getClass().getMethod("getId");
                // [!!] --- 오타 수정 ---
                // (X) Object id = getId.invoke(praincipal);
                Object id = getId.invoke(principal); // (O)

                if (id instanceof Long l) return l;
                if (id instanceof Number n) return n.longValue();
            } catch (Exception ignored) {}

            throw new org.springframework.security.authentication.BadCredentialsException("Cannot resolve user id from principal");
        }
    }