package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class AuthUser implements UserDetails {

    private final Long id;
    private final String email;
    private final UserRole userRole;
    private final String nickname;

    public AuthUser(Long id, String email, UserRole userRole, String nickname) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
        this.nickname = nickname;
    }

    // 권환 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(userRole.getGrantedAuthority());
    }

    @Override
    public String getPassword() {
        return email;
    }

    @Override
    public String getUsername() {
        return null;
    }

}
