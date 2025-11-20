package com.partition.global.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final UserDetailsService userDetailsService;

    // 환경변수 값 주입
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-validity}") long accessTokenValidity,
                            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity,
                            UserDetailsService userDetailsService) {

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        this.accessTokenValidityInMilliseconds = accessTokenValidity;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidity;
        this.userDetailsService = userDetailsService;
    }

    // Access Token 생성
    public String createAccessToken(Long userId, String role) {
        return createToken(userId, role, accessTokenValidityInMilliseconds);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long userId) {
        return createToken(userId, null, refreshTokenValidityInMilliseconds);
    }

    private String createToken(Long userId, String role, long validity) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(validityDate)
                .signWith(key, Jwts.SIG.HS512);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    // 토큰에서 User ID 추출
    public Long getUserId(String token) {
        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(key) // setSigningKey -> verifyWith
                        .build()
                        .parseSignedClaims(token) // parseClaimsJws -> parseSignedClaims
                        .getPayload() // getBody -> getPayload
                        .getSubject()
        );
    }

    public Authentication getAuthentication(String accessToken) {
        // 토큰에서 userId 꺼낸 뒤 DB에서 찾아옴
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(accessToken).toString());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}