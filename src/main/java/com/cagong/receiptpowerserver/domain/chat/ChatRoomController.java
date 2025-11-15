package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.*;
import com.cagong.receiptpowerserver.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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
        // [!!] ChatMessageRepository/Service가 구현되어 있어야 합니다.
        return ResponseEntity.ok(chatRoomService.getMessages(roomId));
    }

    /**
     * ✅ 3 & 4. 채팅방 입장 API (인원 제한 로직은 서비스 레이어)
     */
    @PostMapping("/{id}/enter")
    public ResponseEntity<ChatParticipantCountResponse> enterChatRoom(@PathVariable Long id, Authentication authentication) {
        Long authenticatedUserId = extractUserId(authentication);
        try {
            ChatParticipantCountResponse response = chatRoomService.enterRoom(id, authenticatedUserId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse); // Swagger가 403 응답을 문서화할 수 있음
        }
    }

    /**
     * ✅ 3. 채팅방 나가기 API
     */
    @PostMapping("/{id}/leave")
    public ResponseEntity<ChatParticipantCountResponse> leaveChatRoom(@PathVariable Long id, Authentication authentication) {
        Long authenticatedUserId = extractUserId(authentication);
        // [!!] ChatParticipantRepository/Service가 구현되어 있어야 합니다.
        ChatParticipantCountResponse response = chatRoomService.leaveRoom(id, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 3. 현재 참여 중인 인원 가져오기
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ChatParticipantResponse>> getChatRoomParticipants(@PathVariable Long id) {
        // [!!] ChatParticipantRepository/Service가 구현되어 있어야 합니다.
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
            Object id = getId.invoke(principal);
            if (id instanceof Long l) return l;
            if (id instanceof Number n) return n.longValue();
        } catch (Exception ignored) {}

        throw new org.springframework.security.authentication.BadCredentialsException("Cannot resolve user id from principal");
    }


}
