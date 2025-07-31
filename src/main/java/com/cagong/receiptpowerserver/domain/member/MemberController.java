package com.cagong.receiptpowerserver.domain.member;

import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginDto;
import com.cagong.receiptpowerserver.domain.member.dto.MemberRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<Member> getAllMembers(){
        return ResponseEntity.ok().build();
    }


    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody MemberRequestDto dto) {
        try {
            memberService.signup(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody MemberLoginDto dto) {
        try {
            String jwt = memberService.login(dto);
            return ResponseEntity.ok(jwt);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
