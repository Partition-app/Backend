package com.partition.domain.auth.service;

import com.partition.domain.auth.dto.request.KakaoLoginRequest;
import com.partition.domain.auth.dto.response.TokenResponse;
import com.partition.domain.auth.exception.AuthErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.User;
import com.partition.entity.enums.UserRole;
import com.partition.global.config.jwt.JwtTokenProvider;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.access-token-validity}")
    private Long accessTokenValidity;

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    @Transactional
    public TokenResponse kakaoLogin(KakaoLoginRequest request) {
        log.info("카카오 로그인 시작");

        String kakaoAccessToken = request.getKakaoAccessToken();

        // [변경] CustomException 사용 (토큰 누락 시)
        if (kakaoAccessToken == null || kakaoAccessToken.trim().isEmpty()) {
            log.error("카카오 액세스 토큰이 비어있습니다.");
            throw new CustomException(AuthErrorCode.EMPTY_ACCESS_TOKEN);
        }

        // 카카오 API 호출 (내부에서 CustomException 처리됨)
        Map<String, Object> kakaoUserInfo = getUserInfoFromKakao(kakaoAccessToken);

        // 유저 정보 파싱
        String providerId = extractProviderId(kakaoUserInfo);
        String email = extractEmail(kakaoUserInfo, providerId);
        String name = extractName(kakaoUserInfo);
        String profileImage = extractProfileImage(kakaoUserInfo);

        log.info("카카오 사용자 정보 추출 완료 - providerId: {}, email: {}", providerId, email);

        // DB 조회 및 가입/로그인
        User user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> {
                    log.info("신규 사용자 회원가입 - providerId: {}", providerId);
                    return userRepository.save(User.builder()
                            .email(email)
                            .name(name)
                            .profileImage(profileImage)
                            .provider("KAKAO")
                            .providerId(providerId)
                            .memberRole(UserRole.GUEST)
                            .build());
                });

        // JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getMemberRole().getKey());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        log.info("JWT 토큰 발급 완료 - userId: {}", user.getId());

        return TokenResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenValidity)
                .build();
    }

    private Map<String, Object> getUserInfoFromKakao(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() == null) {
                // [변경] 응답 바디가 비어있으면 카카오 연동 실패 간주
                throw new CustomException(AuthErrorCode.KAKAO_LOGIN_FAILED);
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("카카오 API 호출 실패 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // [변경] 401 에러 -> 유효하지 않은 토큰 에러
                throw new CustomException(AuthErrorCode.INVALID_ACCESS_TOKEN);
            }
            // 그 외 4xx 에러 -> 일반적인 로그인 실패
            throw new CustomException(AuthErrorCode.KAKAO_LOGIN_FAILED);

        } catch (CustomException e) {
            throw e; // 이미 CustomException이면 그대로 던짐
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 예상치 못한 오류 발생", e);
            // [변경] 알 수 없는 에러 -> 로그인 실패 처리
            throw new CustomException(AuthErrorCode.KAKAO_LOGIN_FAILED);
        }
    }

    private String extractProviderId(Map<String, Object> kakaoUserInfo) {
        Object id = kakaoUserInfo.get("id");
        if (id == null) {
            // [변경] ID가 없으면 심각한 에러
            throw new CustomException(AuthErrorCode.KAKAO_LOGIN_FAILED);
        }
        return String.valueOf(id);
    }

    private String extractEmail(Map<String, Object> kakaoUserInfo, String providerId) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
        // 이메일 추출 로직은 기존 유지 (없으면 더미 이메일 사용)
        if (kakaoAccount == null) return providerId + "@kakao.com";
        String email = (String) kakaoAccount.get("email");
        if (email == null || email.trim().isEmpty()) return providerId + "@kakao.com";
        return email;
    }

    private String extractName(Map<String, Object> kakaoUserInfo) {
        // 닉네임 추출 로직 유지 (예외 시 기본값 반환)
        try {
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
            if (kakaoAccount == null) return "카카오 사용자";
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile == null) return "카카오 사용자";
            String nickname = (String) profile.get("nickname");
            return (nickname != null && !nickname.trim().isEmpty()) ? nickname : "카카오 사용자";
        } catch (Exception e) {
            return "카카오 사용자";
        }
    }

    private String extractProfileImage(Map<String, Object> kakaoUserInfo) {
        // 프로필 이미지 추출 로직 유지
        try {
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
            if (kakaoAccount == null) return null;
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile == null) return null;
            return (String) profile.get("profile_image_url");
        } catch (Exception e) {
            return null;
        }
    }
}