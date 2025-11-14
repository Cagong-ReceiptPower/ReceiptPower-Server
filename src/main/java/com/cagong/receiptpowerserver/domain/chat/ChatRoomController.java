package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomCreateRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomStatusUpdateRequest;
import com.cagong.receiptpowerserver.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.reflect.Method;
import java.util.List;

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

        // --- 이하 코드는 기존 코드의 안전 장치로 사용됩니다 ---

        if (principal instanceof UserDetails userDetails) {
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }

        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }

        try {
            Method getId = principal.getClass().getMethod("getId");
            Object id = getId.invoke(principal);
            if (id instanceof Long l) return l;
            if (id instanceof Number n) return n.longValue();
        } catch (Exception ignored) {
            // fall through
        }

        throw new org.springframework.security.authentication.BadCredentialsException("Cannot resolve user id from principal");
    }
}
