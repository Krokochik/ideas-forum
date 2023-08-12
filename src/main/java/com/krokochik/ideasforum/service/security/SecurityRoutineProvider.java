package com.krokochik.ideasforum.service.security;

import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.functional.Role;
import com.krokochik.ideasforum.repository.CustomPersistentTokenRepository;
import com.krokochik.ideasforum.service.jdbc.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides quick access to basic security actions, such as authorization or checking user.
 **/
@Primary
@Slf4j
@Component
public class SecurityRoutineProvider {

    @Autowired
    CustomPersistentTokenRepository tokenRepository;

    @Autowired
    CustomUserDetailsService userDetailsService;

    /**
     * Returns if the context user is fully authenticated (has full access to the site).
     **/
    public boolean isAuthenticated(SecurityContext ctx) {
        for (GrantedAuthority authority : ctx.getAuthentication().getAuthorities()) {
            try {
                if (Role.valueOf(authority.getAuthority()) != Role.ANONYM)
                    return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return false;
    }

    /**
     * Returns if the context user has the role.
     *
     * @throws NullPointerException if role is {@code null}.
     **/
    public boolean hasRole(@NonNull Role role, SecurityContext ctx) {
        for (GrantedAuthority authority : ctx.getAuthentication().getAuthorities()) {
            try {
                if (Role.valueOf(authority.getAuthority()).equals(role))
                    return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return false;
    }

    /**
     * Authorizes user into Spring manually.
     *
     * @param remember is it needed to add remember-me cookie to response and database.
     * @param request  the user's request (spring-generated). If remember is true, must not be null.
     * @param response the server response (spring-generated). If remember is true, must not be null.
     * @throws NullPointerException if a parameter is null.
     **/
    public SecurityContext authorizeUser(@NonNull User user, boolean remember,
                                         @NonNull SecurityContext securityContext,
                                         HttpServletRequest request, HttpServletResponse response) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::toAuthority)
                .collect(Collectors.toSet());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                convertUserToUserDetails(user), user, authorities);
        securityContext.setAuthentication(authentication);

        if (remember) {
            if (request == null) throw new NullPointerException("request"); // lombok's @NonNull format
            if (response == null) throw new NullPointerException("response");

            String rememberMeCookieName = "remember-me";
            int tokenValiditySeconds = 30 * 24 * 60 * 60;
            request = addParameterToRequest(request, rememberMeCookieName, "on"); // it is necessary for spring
            PersistentTokenBasedRememberMeServices rememberMeServices = new PersistentTokenBasedRememberMeServices(
                    rememberMeCookieName, userDetailsService, tokenRepository);
            rememberMeServices.setTokenValiditySeconds(tokenValiditySeconds);
            rememberMeServices.loginSuccess(request, response, authentication);
        }
        return securityContext;
    }

    /**
     * Authorizes user into Spring manually don't remembering him.
     *
     * @throws NullPointerException if a parameter is null.
     **/
    public void authorizeUser(@NonNull User user, @NonNull SecurityContext securityContext) {
        authorizeUser(user, false, securityContext, null, null);
    }


    private HttpServletRequest addParameterToRequest(HttpServletRequest request, String newName, String newValue) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                if (name.equals(newName)) {
                    return newValue;
                }
                return super.getParameter(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                Map<String, String[]> parameterMap = new HashMap<>(super.getParameterMap());
                parameterMap.put(newName, new String[]{newValue});
                return parameterMap;
            }

            @Override
            public Enumeration<String> getParameterNames() {
                List<String> parameterNames = Collections.list(super.getParameterNames());
                parameterNames.add(newName);
                return Collections.enumeration(parameterNames);
            }
        };
    }

    protected UserDetails convertUserToUserDetails(User user) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::toAuthority)
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
