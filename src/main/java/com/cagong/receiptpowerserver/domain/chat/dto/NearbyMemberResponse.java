package com.cagong.receiptpowerserver.domain.chat.dto;

import com.cagong.receiptpowerserver.domain.member.Member;
import lombok.Getter;

@Getter
public class NearbyMemberResponse {

    private final Long memberId;
    private final String username;
    private final String email;
    private final Double distance;

    public NearbyMemberResponse(Member member, Double distance) {
        this.memberId = member.getId();
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.distance = distance;
    }
}