package com.cagong.receiptpowerserver.domain.chat.dto;

import com.cagong.receiptpowerserver.domain.chat.ChatRoom;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ChatRoomListResponse {

    private final Long chatRoomId;
    private final String title;
    private final String creatorUsername;
    @Setter
    private Integer currentParticipants;

    public ChatRoomListResponse(ChatRoom chatRoom) {
        this.chatRoomId = chatRoom.getId();
        this.title = chatRoom.getTitle();
        this.creatorUsername = chatRoom.getCreator().getUsername();
    }
}