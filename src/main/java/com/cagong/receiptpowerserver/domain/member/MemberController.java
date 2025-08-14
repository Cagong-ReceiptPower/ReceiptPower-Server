package com.cagong.receiptpowerserver.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody MemberRegisterDto dto) {
        memberService.registerMember(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto dto) {
        memberService.login(dto.getLoginId(), dto.getPassword());
        return ResponseEntity.ok("로그인 성공");
    }
}
