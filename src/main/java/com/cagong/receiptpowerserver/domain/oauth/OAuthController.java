package com.cagong.receiptpowerserver.domain.oauth;

import com.cagong.receiptpowerserver.domain.member.dto.MemberLoginResponse;
import com.cagong.receiptpowerserver.domain.oauth.dto.OAuthLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponse> socialLogin(@RequestBody OAuthLoginRequest request){
        MemberLoginResponse response = oAuthService.login(request);
        return ResponseEntity.ok().body(response);
    }
}
