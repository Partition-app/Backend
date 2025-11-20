package com.partition.global.config.security;

import com.partition.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // UserRole을 시큐리티 권한으로 변환
        return Collections.singletonList(new SimpleGrantedAuthority(user.getMemberRole().getKey()));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // 소셜 로그인은 null일 수 있음
    }

    @Override
    public String getUsername() {
        return user.getId().toString(); // PK를 Username으로 사용
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }
}