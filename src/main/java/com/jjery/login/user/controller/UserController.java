package com.jjery.login.user.controller;

import com.jjery.login.user.dto.*;
import com.jjery.login.user.service.UserService;
import com.jjery.login.user.util.JwtUtil;
import com.jjery.login.user.util.NicknameGenerator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
  private final UserService userService;
  private final JwtUtil jwtUtil;

  // 회원가입 엔드포인트
  /*
   * URL: /api/users/register
   * HTTP Method: POST
   * HTTP Body: UserRegistrationRequest 객체 (JSON 형식)
   * 요청 JSON 형식:
   * {
   *   "loginId": "user123",
   *   "password": "password123",
   *   "nickname": "UserNickname"
   * }
   */

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록( 회원가입 )")
  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
    try {
      // 회원가입 서비스 호출 (User Service 호출)
      // 자동으로 닉네임 생성
      String nickname = NicknameGenerator.generateNickname();

      UserRegistrationResponseDTO response =
          userService.registerUser(request.getLoginId(), request.getPassword(), nickname);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      // 예외 발생 시 로그 출력 디버깅용
      log.error("Error during user registration in controller /api/register: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "회원가입 중복된 아이디 확인", description = "회원가입 중복된 아이디 확인")
  @GetMapping("/check-id")
  public ResponseEntity<?> checkRegisterLoginId(@RequestParam String loginId) {
    try {
      boolean isAvailable = userService.isLoginIdAvailable(loginId);

      if (isAvailable) {
        return ResponseEntity.ok(Collections.singletonMap("available", true));
      } else {
        log.warn("회원가입 실패: 중복된 Login ID - {}", loginId);
        return ResponseEntity.status(HttpServletResponse.SC_CONFLICT)
            .body(Collections.singletonMap("available", false));
      }
    } catch (RuntimeException e) {
      log.error("Error during ID check in controller /api/check-id: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 로그인 엔드포인트
  /*
   * URL: /api/users/login
   * HTTP Method: POST
   * HTTP Body: UserLoginRequest 객체 (JSON 형식)
   * 요청 JSON 형식:
   * {
   *   "loginId": "user123",
   *   "password": "password123"
   * }
   */
  @Operation(summary = "로그인", description = "사용자가 로그인하고 토큰을 발급받음.")
  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request) {
    try {
      // 로그인 서비스 호출
      String accessToken = userService.loginUser(request.getLoginId(), request.getPassword());
      // Refresh Token 생성
      String refreshToken = jwtUtil.createRefreshToken(request.getLoginId());
      // Refresh Token DB에 저장 ( 만료일 7일 )
      userService.saveRefreshToken(request.getLoginId(), refreshToken);
      return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    } catch (RuntimeException e) {
      log.error("Error during user login in controller /api/login: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 리프레시 토큰 엔드포인트
  /*
   * URL: /api/users/refresh-token
   * HTTP Method: POST
   * HTTP Body: RefreshTokenRequest 객체 (JSON 형식)
   * 요청 JSON 형식:
   * {
   *   "loginId": "user123",
   *   "nickname": "UserNickname"
   *   "refreshToken": "refreshToken"
   * }
   */
  @Operation(summary = "토큰 재발급", description = "refresh token을 검증 후, 새로운 accessToken 발급.")
  @PostMapping("/refresh-token")
  public ResponseEntity<?> refreshAuthToken(@RequestBody RefreshTokenRequest request) {
    try {
      // Refresh Token 유효성 검증
      String refreshToken = request.getRefreshToken();
      if (!jwtUtil.validateToken(refreshToken, request.getLoginId())) {
        return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
            .body("Invalid Refresh Token");
      }
      // 새로운 Access Token 생성
      String newAccessToken =
          jwtUtil.createAccessToken(request.getLoginId(), request.getNickname());
      return ResponseEntity.ok(new AuthResponse(newAccessToken));
    } catch (RuntimeException e) {
      // 예외 발생 시 로그 출력
      log.error("Error during token refresh in controller /api/refresh-token: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
