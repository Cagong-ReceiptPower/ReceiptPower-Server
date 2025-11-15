// [새 파일] domain/chat/ChatParticipantRepository.java
package com.cagong.receiptpowerserver.domain.chat;

import com.cagong.receiptpowerserver.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    // ✅ 기능 1: 현재 참여 인원 목록 조회
    List<ChatParticipant> findByChatRoom_Id(Long chatRoomId);

    // ✅ 기능 2: 현재 참여 인원 수 조회
    long countByChatRoom_Id(Long chatRoomId);

    // ✅ 기능 2: 이미 참여 중인지 확인
    boolean existsByChatRoom_IdAndMember_Id(Long chatRoomId, Long memberId);

    // (보너스) 나가기 처리를 위함
    Optional<ChatParticipant> findByChatRoom_IdAndMember_Id(Long chatRoomId, Long memberId);
}