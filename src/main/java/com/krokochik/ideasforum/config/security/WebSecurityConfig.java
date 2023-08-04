package com.krokochik.ideasforum.config.security;

import com.krokochik.ideasforum.model.functional.Role;
import com.krokochik.ideasforum.service.jdbc.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    DataSource dataSource;

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.requiresChannel(registry ->
                registry.anyRequest().requiresSecure());
        http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(
                                "/", "/main", "/main/**",
                                "/settings",
                                "/email-validity-confirmation", "/password-reset-request", "/password-change", "/password-change-instructions",
                                "/scripts/**", "/images/**", "/css/**",
                                "/avatar", "/privacy", "oauth2/**", "/login", "/sign-up",
                                "/mfa/**", "/terminal",
                                "/googleb8fcdd64aa45ba54.html", "/yandex_f4f03a518326d43b.html", "/bootstrap.min.css.map")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/sign-up", "/sign-up/**", "/terminal")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/profile")
                        .hasAnyAuthority(Role.USER.name())
                        .requestMatchers("/add-note")
                        .hasAnyAuthority(Role.USER.name())
                        .anyRequest()
                        .authenticated()
                )
                .formLogin(withDefaults ->
                        withDefaults
                                .loginPage("/login")
                                .failureUrl("/login?loginError")
                                .defaultSuccessUrl("/email-validity-confirmation")
                )
                .oauth2Login(withDefaults ->
                        withDefaults
                                .loginPage("/login")
                                .failureUrl("/oauth2/failure")
                                .defaultSuccessUrl("/oauth2/success")
                )
                .logout(logout ->
                        logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessUrl("/main")
                                .deleteCookies("JSESSIONID")
                                .invalidateHttpSession(true)
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )
                .rememberMe(rememberMe ->
                        rememberMe
                                .useSecureCookie(true)
                                .key("4R284g33vDkKkNm47ef9ADtm3XKhx9HD")
                                .rememberMeCookieName("remember-me")
                                .tokenValiditySeconds(15 * 24 * 60 * 60)
                                .tokenRepository(tokenRepository())
                                .userDetailsService(new CustomUserDetailsService())
                )
                .userDetailsService(new CustomUserDetailsService())
                .csrf(csrf ->
                        csrf
                            .ignoringRequestMatchers("/mfa/**")
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .cors(request -> new CorsConfiguration().applyPermitDefaultValues());

        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        return new JdbcTokenRepositoryImpl(){{
            setDataSource(dataSource);
            setCreateTableOnStartup(false);
        }};
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new JdbcUserDetailsManager(dataSource) {{
            setUsersByUsernameQuery("select username, password, active from usr where username=?");
            setAuthoritiesByUsernameQuery("select usr.username, user_roles.roles " +
                    "from usr inner join user_role user_roles " +
                    "on usr.id = user_roles.user_id " +
                    "where usr.username=?");
        }};
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
