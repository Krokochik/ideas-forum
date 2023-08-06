package com.krokochik.ideasforum.config.security;

import com.krokochik.ideasforum.model.functional.Role;
import com.krokochik.ideasforum.repository.CustomPersistentTokenRepository;
import com.krokochik.ideasforum.service.jdbc.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    CustomPersistentTokenRepository tokenRepository;

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
                        "/mfa/**", "/terminal",
                        "/googleb8fcdd64aa45ba54.html", "/yandex_f4f03a518326d43b.html", "/bootstrap.min.css.map")
                    .permitAll()
                .requestMatchers("/oauth2/**", "/sign-up", "/login/**", "/password-reset-request")
                    .access((authentication, object) ->
                        new AuthorityAuthorizationDecision(false, Collections
                                .singleton(Role.USER.toAuthority())))
                .requestMatchers(HttpMethod.POST, "/sign-up/**", "/password-reset-request")
                    .access((authentication, object) ->
                            new AuthorityAuthorizationDecision(false, Collections
                                    .singleton(Role.USER.toAuthority())))
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
                    .ignoringRequestMatchers("/mfa/**")
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
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
}
