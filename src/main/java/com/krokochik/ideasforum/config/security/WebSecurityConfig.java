package com.krokochik.ideasforum.config.security;

import com.krokochik.ideasforum.model.functional.Role;
import com.krokochik.ideasforum.repository.CustomPersistentTokenRepository;
import com.krokochik.ideasforum.service.jdbc.CustomUserDetailsService;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.spring.autoconfigure.TotpAutoConfiguration;
import dev.samstevens.totp.time.NtpTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@Import(TotpAutoConfiguration.class)
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    CustomPersistentTokenRepository tokenRepository;

    // Same as Spring Boot 3 .not().hasAnyAuthority()
    private AuthorizationDecision hasNotAnyRole(Authentication authentication, Role... role) {
        Set<GrantedAuthority> authorities = Arrays.stream(role)
                .map(Role::toAuthority)
                .collect(Collectors.toSet());
        boolean granted = true;
        for (GrantedAuthority authority : authorities) {
            if (authentication.getAuthorities().contains(authority)) {
                granted = false;
                break;
            }
        }
        return new AuthorizationDecision(granted);
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.requiresChannel(registry ->
            registry
                .anyRequest()
                    .requiresSecure()
        );
        http
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(
                        "/", "/main", "/main/**",
                        "/settings",
                        "/email-validity-confirmation",
                        "/scripts/**", "/images/**", "/css/**",
                        "/avatar", "/privacy",
                        "/mfa/**", "/terminal", "/ver/**",
                        "/googleb8fcdd64aa45ba54.html", "/yandex_f4f03a518326d43b.html", "/bootstrap.min.css.map")
                    .permitAll()
                .requestMatchers("/oauth2/**", "/sign-up", "/login/**", "/password-reset-request")
                    .access((auth, o) ->
                            hasNotAnyRole(auth.get(), Role.USER))
                .requestMatchers(HttpMethod.POST, "/sign-up/**", "/password-reset-request")
                    .access((auth, o) ->
                            hasNotAnyRole(auth.get(), Role.USER))
                .requestMatchers(HttpMethod.POST, "/profile")
                    .hasAnyAuthority(Role.USER.name())
                .anyRequest()
                    .authenticated()
            )
            .userDetailsService(
                userDetailsService
            )
            .oauth2Login(conf ->
                conf
                    .loginPage("/login")
                    .failureUrl("/oauth2/failure")
                    .defaultSuccessUrl("/oauth2/success")
            )
            .formLogin(conf ->
                conf
                    .loginPage("/login")
                    .failureUrl("/login?loginError")
                    .defaultSuccessUrl("/email-validity-confirmation")
            )
            .logout(conf ->
                conf
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/main")
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)
            )
            .sessionManagement(conf ->
                conf
                    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            .rememberMe(conf ->
                conf
                    .useSecureCookie(true)
                    .key("4R284g33vDkKkNm47ef9ADtm3XKhx9HD")
                    .rememberMeCookieName("remember-me")
                    .tokenValiditySeconds(15 * 24 * 60 * 60)
                    .tokenRepository(tokenRepository)
                    .userDetailsService(userDetailsService)
            )
            .csrf(conf ->
                conf
                    .ignoringRequestMatchers("/mfa/**", "/profile/**")
            )
            .cors(request ->
                    new CorsConfiguration().applyPermitDefaultValues()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HashingAlgorithm hashingAlgorithm() {
        return HashingAlgorithm.SHA512;
    }

    @Bean
    public TimeProvider timeProvider() throws UnknownHostException {
        return new NtpTimeProvider("pool.ntp.org");
    }
}
