package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomCreateRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomStatusUpdateRequest;
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
@RequestMapping("/api/chat-rooms")
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

    @GetMapping("/{id}")
    public ResponseEntity<ChatRoomResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(chatRoomService.getById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms(Authentication authentication) {
        Long authenticatedUserId = extractUserId(authentication);
        return ResponseEntity.ok(chatRoomService.getMyRooms(authenticatedUserId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ChatRoomResponse>> nearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(name = "radiusKm", defaultValue = "3.0") Double radiusKm
    ) {
        return ResponseEntity.ok(chatRoomService.findNearby(latitude, longitude, radiusKm));
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
     * 지원 케이스:
     * - UserDetails#getUsername()이 숫자 문자열인 경우
     * - principal이 String이고 숫자 문자열인 경우
     * - principal이 getId() 메서드(Long 반환)를 보유한 커스텀 Principal인 경우(리플렉션)
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();

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
