package com.krokochik.ideasForum.config.security;

import com.krokochik.ideasForum.model.functional.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure();
        http.authorizeRequests()
                .antMatchers(
                        "/", "/main", "/main/**",
                        "/settings",
                        "/email-validity-confirmation", "/password-reset-request", "/password-change", "/password-change-instructions",
                        "/scripts/**", "/images/**", "/css/**",
                        "/avatar", "/privacy",
                        "/mfa/**", "/terminal",
                        "/googleb8fcdd64aa45ba54.html", "/yandex_f4f03a518326d43b.html", "/bootstrap.min.css.map")
                    .permitAll()
                .antMatchers(HttpMethod.POST, "/sign-up", "/sign-up/**", "/terminal")
                    .permitAll()
                .antMatchers(HttpMethod.GET,"/login", "/sign-up", "/password-reset-request", "/oauth2/**")
                    .not().hasAnyAuthority(Role.USER.name(), Role.ANONYM.name())
                .antMatchers(HttpMethod.POST, "/profile")
                    .hasAnyAuthority(Role.USER.name())
                .antMatchers("/add-note")
                    .hasAnyAuthority(Role.USER.name())
                .anyRequest()
                    .authenticated()
                .and()
                    .formLogin()
                        .loginPage("/login")
                        .failureUrl("/login?loginError")
                        .defaultSuccessUrl("/email-validity-confirmation")
                .and()
                    .oauth2Login()
                    .loginPage("/login")
                    .failureUrl("/oauth2/failure")
                    .defaultSuccessUrl("/oauth2/success")
                .and()
                    .logout()
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/main")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .and()
                    .rememberMe()
                        .useSecureCookie(true)
                        .key("4R284g33vDkKkNm47ef9ADtm3XKhx9HD")
                        .rememberMeCookieName("remember-me")
                        .tokenValiditySeconds(15 * 24 * 60 * 60)
                        .tokenRepository(tokenRepository())
                .and()
                    .csrf()
                        .ignoringAntMatchers("/mfa/**")
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and().cors().and();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(NoOpPasswordEncoder.getInstance())
                .usersByUsernameQuery("select username, password, active from usr where username=?")
                .authoritiesByUsernameQuery("select usr.username, user_roles.roles " +
                                            "from usr inner join user_role user_roles " +
                                            "on usr.id = user_roles.user_id " +
                                            "where usr.username=?");
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        tokenRepository.setCreateTableOnStartup(false);

        return tokenRepository;
    }
}
