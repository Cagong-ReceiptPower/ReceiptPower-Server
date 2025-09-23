package com.cagong.receiptpowerserver.domain.chat.dto;

import com.cagong.receiptpowerserver.domain.chat.ChatRoom;
import com.cagong.receiptpowerserver.domain.chat.ChatRoomStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // JSON 역직렬화를 위한 기본 생성자
public class ChatRoomResponse {

    private Long chatRoomId;
    private String title;
    private String creatorUsername;
    private Double latitude;
    private Double longitude;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private ChatRoomStatus status;

    // 엔티티를 DTO로 변환하기 위한 정적 팩토리 메서드
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        ChatRoomResponse response = new ChatRoomResponse();
        response.setChatRoomId(chatRoom.getId());
        response.setTitle(chatRoom.getTitle());
        response.setCreatorUsername(chatRoom.getCreator().getUsername());
        response.setLatitude(chatRoom.getLocation().getLatitude());
        response.setLongitude(chatRoom.getLocation().getLongitude());
        response.setMaxParticipants(chatRoom.getMaxParticipants());
        response.setStatus(chatRoom.getStatus());
        return response;
    }
}