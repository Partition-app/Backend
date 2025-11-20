package com.partition.domain.auth.service;

import com.partition.domain.auth.dto.request.KakaoLoginRequest;
import com.partition.domain.auth.dto.response.TokenResponse;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.User;
import com.partition.entity.enums.UserRole;
import com.partition.global.config.jwt.JwtTokenProvider;
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

        // Front에서 받은 카카오 토큰으로 카카오 유저 정보 가져오기
        String kakaoAccessToken = request.getKakaoAccessToken();

        if (kakaoAccessToken == null || kakaoAccessToken.trim().isEmpty()) {
            log.error("카카오 액세스 토큰이 비어있습니다.");
            throw new IllegalArgumentException("카카오 액세스 토큰이 필요합니다.");
        }

        log.debug("카카오 액세스 토큰 수신: {}...", kakaoAccessToken.substring(0, Math.min(10, kakaoAccessToken.length())));

        Map<String, Object> kakaoUserInfo = getUserInfoFromKakao(kakaoAccessToken);

        // 유저 정보 파싱 (Null 체크 강화)
        String providerId = extractProviderId(kakaoUserInfo);
        String email = extractEmail(kakaoUserInfo, providerId);
        String name = extractName(kakaoUserInfo);
        String profileImage = extractProfileImage(kakaoUserInfo);

        log.info("카카오 사용자 정보 추출 완료 - providerId: {}, email: {}", providerId, email);

        // DB 조회 후 없으면 회원가입, 있으면 로그인 처리
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

        // partition 전용 토큰 발급
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

    /**
     * 카카오 서버로 유저 정보 요청
     */
    private Map<String, Object> getUserInfoFromKakao(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("카카오 API 호출 시작: https://kapi.kakao.com/v2/user/me");

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            log.debug("카카오 API 응답 성공: {}", response.getStatusCode());

            if (response.getBody() == null) {
                throw new IllegalStateException("카카오 API 응답이 비어있습니다.");
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("카카오 API 호출 실패 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new IllegalArgumentException("유효하지 않은 카카오 액세스 토큰입니다.", e);
            }

            throw new RuntimeException("카카오 사용자 정보를 가져오는데 실패했습니다.", e);

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 예상치 못한 오류 발생", e);
            throw new RuntimeException("카카오 인증 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 카카오 응답에서 providerId 추출
     */
    private String extractProviderId(Map<String, Object> kakaoUserInfo) {
        Object id = kakaoUserInfo.get("id");
        if (id == null) {
            throw new IllegalStateException("카카오 사용자 ID를 찾을 수 없습니다.");
        }
        return String.valueOf(id);
    }

    /**
     * 카카오 응답에서 이메일 추출
     */
    private String extractEmail(Map<String, Object> kakaoUserInfo, String providerId) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
        if (kakaoAccount == null) {
            log.warn("kakao_account 정보가 없습니다. 기본 이메일 사용");
            return providerId + "@kakao.com";
        }

        String email = (String) kakaoAccount.get("email");
        if (email == null || email.trim().isEmpty()) {
            log.warn("이메일 정보가 없습니다. 기본 이메일 사용");
            return providerId + "@kakao.com";
        }

        return email;
    }

    /**
     * 카카오 응답에서 닉네임 추출
     */
    private String extractName(Map<String, Object> kakaoUserInfo) {
        try {
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
            if (kakaoAccount == null) {
                return "카카오 사용자";
            }

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile == null) {
                return "카카오 사용자";
            }

            String nickname = (String) profile.get("nickname");
            if (nickname == null || nickname.trim().isEmpty()) {
                return "카카오 사용자";
            }

            return nickname;

        } catch (Exception e) {
            log.warn("닉네임 추출 실패, 기본값 사용", e);
            return "카카오 사용자";
        }
    }

    /**
     * 카카오 응답에서 프로필 이미지 추출
     */
    private String extractProfileImage(Map<String, Object> kakaoUserInfo) {
        try {
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
            if (kakaoAccount == null) {
                return null;
            }

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile == null) {
                return null;
            }

            return (String) profile.get("profile_image_url");

        } catch (Exception e) {
            log.warn("프로필 이미지 추출 실패", e);
            return null;
        }
    }
}