package com.krokochik.ideasForum.service.security;

import com.krokochik.ideasForum.model.Role;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.CustomPersistentTokenRepository;
import com.krokochik.ideasForum.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityRoutineProvider {

    @Autowired
    CustomPersistentTokenRepository tokenRepository;

    @Autowired
    CustomUserDetailsService userDetailsService;

    public boolean isAuthenticated() {
        for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
            try {
                if (Role.valueOf(authority.getAuthority()) != Role.ANONYM)
                    return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return false;
    }

    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    public boolean hasRole(Role role) {
        for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
            try {
                if (Role.valueOf(authority.getAuthority()).equals(role))
                    return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return false;
    }

    public void authorizeUser(SecurityContext securityContext, User user,
                              boolean remember, HttpServletRequest request, HttpServletResponse response) {
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                convertUserToUserDetails(user), user, authorities);
        securityContext.setAuthentication(authentication);

        if (remember) {
            String rememberMeCookieName = "remember-me";
            int tokenValiditySeconds = 15 * 24 * 60 * 60;

            System.out.println("remember-me");
            PersistentTokenBasedRememberMeServices rememberMeServices = new PersistentTokenBasedRememberMeServices(
                    rememberMeCookieName, userDetailsService, tokenRepository);
            rememberMeServices.setTokenValiditySeconds(tokenValiditySeconds);
            rememberMeServices.loginSuccess(request, response, authentication);
        }
    }

    public void authorizeUser(SecurityContext securityContext, User user) {
        authorizeUser(securityContext, user, false, null, null);
    }

    public UserDetails convertUserToUserDetails(User user) {
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return authorities;
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getUsername();
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
        };
    }

}
