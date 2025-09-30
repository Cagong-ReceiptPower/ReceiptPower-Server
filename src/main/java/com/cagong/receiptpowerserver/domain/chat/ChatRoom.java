package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.cafe.Cafe;
import com.cagong.receiptpowerserver.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    // 기본값 설정
    private static final Integer DEFAULT_MAX_PARTICIPANTS = 10; // 기본값 우선 10명

    //기본 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 제목 - 필수 입력
    @Column(nullable = false)
    private String title;

    // 하나의 카페에 여러 채팅방 가능
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    // 생성자 정보 - 다대일 관게 ChatRoom : Member = N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    // 채팅방 설정
    @Column(nullable = false)
    private Integer maxParticipants = DEFAULT_MAX_PARTICIPANTS;  // 최대 참여자 수 - 필수 입력, 기본값 : 10명

    // 상태 관리 - ChatRoomStatus Enum - ACTIVE, INACTIVE, CLOSED
    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public ChatRoom(String title, Cafe cafe, Member creator, Integer maxParticipants) {
        this.title = title;
        this.cafe = cafe; // location -> cafe
        this.creator = creator;
        this.maxParticipants = maxParticipants != null ? maxParticipants : 10;
    }

    // 상태 변경 메서드 추가
    public void setStatus(ChatRoomStatus status) {
        this.status = status;
    }
}
