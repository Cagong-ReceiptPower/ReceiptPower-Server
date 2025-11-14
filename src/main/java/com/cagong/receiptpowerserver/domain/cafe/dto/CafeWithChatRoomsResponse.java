// domain/cafe/dto/CafeWithChatRoomsResponse.java

package com.cagong.receiptpowerserver.domain.cafe.dto;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.chat.dto.ChatRoomResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class CafeWithChatRoomsResponse {

    // 1. 카페 정보 필드
    private final Long cafeId;
    private final String name;
    private final String address;
    private final String phoneNumber;

    // 2. 해당 카페에 속한 채팅방 목록 필드
    private final List<ChatRoomResponse> chatRooms;

    // 생성자: Cafe 객체와 ChatRoomResponse 리스트를 받아서 이 DTO를 만듭니다.
    public CafeWithChatRoomsResponse(Cafe cafe, List<ChatRoomResponse> chatRooms) {
        this.cafeId = cafe.getId();
        this.name = cafe.getName();
        this.address = cafe.getAddress();
        this.phoneNumber = cafe.getPhoneNumber();
        this.chatRooms = chatRooms;
    }
}