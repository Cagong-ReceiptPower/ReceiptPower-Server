package com.cagong.receiptpowerserver.global.exception;
import com.cagong.receiptpowerserver.domain.chat.dto.ApiErrorResponse;
import com.cagong.receiptpowerserver.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.cagong.receiptpowerserver.domain.chat")
// [!!] 'domain.chat' 패키지 하위의 컨트롤러에서 발생하는 예외만 처리
public class GlobalChatExceptionHandler {

    /**
     * ✅ 4번 기능 (정원 초과) 등 IllegalStateException 처리
     * 서비스 로직에서 "new IllegalStateException("채팅방 정원이 초과되었습니다.")"가 발생하면
     * 이 메서드가 가로채서 403 Forbidden 응답을 생성합니다.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException e) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * NotFoundException 처리 (e.g., 방이 존재하지 않음)
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NotFoundException e) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}