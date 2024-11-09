package com.datn.tourhotel.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomUserDetails implements UserDetails {
    private final DefaultOAuth2User oauth2User;

    public CustomUserDetails(DefaultOAuth2User oauth2User) {
        this.oauth2User = oauth2User;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getPassword() {
        return null; // OAuth2 không có mật khẩu truyền thống
    }

    @Override
    public String getUsername() {
        return oauth2User.getAttribute("email");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }
}
