package com.cagong.receiptpowerserver.domain.member;

import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberSignupResponse;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginRequest;
import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    //회원가입 api
    @PostMapping("/signup")
    public ResponseEntity<MemberSignupResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
        try {
            MemberSignupResponse response = memberService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    //로그인 api
    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        try {
            MemberLoginResponse response = memberService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<Member> getAllMembers(){
        return ResponseEntity.ok().build();
    }
}
