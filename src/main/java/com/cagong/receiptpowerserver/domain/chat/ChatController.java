package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomCreateRequest;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomListResponse;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import com.cagong.receiptpowerserver.domain.chat.dto.NearbyMemberResponse;
import com.cagong.receiptpowerserver.domain.chat.service.ChatRoomService;
import com.cagong.receiptpowerserver.global.util.MemberUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-rooms")
public class ChatController {

    private final ChatRoomService chatRoomService;

    // 주변 회원 검색 API
    @GetMapping("/nearby-members")
    public ResponseEntity<List<NearbyMemberResponse>> getNearbyMembers(@RequestParam Double radiusKm) {
        Long currentMemberId = MemberUtil.getCurrentMember();
        List<NearbyMemberResponse> nearbyMembers = chatRoomService.findNearbyMembers(currentMemberId, radiusKm);
        return ResponseEntity.ok(nearbyMembers);
    }

    // 1:1 채팅방 생성 API
    @PostMapping("/direct")
    public ResponseEntity<ChatRoomResponse> createDirectChatRoom(@Valid @RequestBody ChatRoomCreateRequest request) {
        Long currentMemberId = MemberUtil.getCurrentMember();
        ChatRoomResponse response = chatRoomService.createDirectChatRoom(currentMemberId, request.getTargetMemberId());
        return ResponseEntity.ok(response);
    }

    // 특정 채팅방 조회 API
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(@PathVariable Long chatRoomId) {
        ChatRoomResponse response = chatRoomService.getChatRoom(chatRoomId);
        return ResponseEntity.ok(response);
    }

    // 내가 생성한 채팅방 목록 조회 API
    @GetMapping("/my-rooms")
    public ResponseEntity<List<ChatRoomListResponse>> getMyChatRooms() {
        Long currentMemberId = MemberUtil.getCurrentMember();
        List<ChatRoomListResponse> myChatRooms = chatRoomService.getMyChatRooms(currentMemberId);
        return ResponseEntity.ok(myChatRooms);
    }

    // 채팅방 상태 변경 API
    @PostMapping("/{chatRoomId}/status")
    public ResponseEntity<Void> updateChatRoomStatus(@PathVariable Long chatRoomId, @RequestParam ChatRoomStatus status) {
        Long currentMemberId = MemberUtil.getCurrentMember();
        chatRoomService.updateChatRoomStatus(chatRoomId, status, currentMemberId);
        return ResponseEntity.ok().build();
    }
}