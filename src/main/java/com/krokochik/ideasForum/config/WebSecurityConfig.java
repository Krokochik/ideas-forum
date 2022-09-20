package com.krokochik.ideasForum.config;

import com.krokochik.ideasForum.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    DataSource dataSource;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/", "/main", "/main/**", "/settings", "/mail-confirm", "/password-abort", "/abortPass", "/pass-abort-notify", "/terminal", "/terminal/**")
                    .permitAll()
                .antMatchers("/login", "/sign-up", "/password-abort")
                    .not().hasAnyAuthority(Role.USER.name(), Role.ADMIN.name(), Role.MODER.name(), Role.ANONYM.name())
                .antMatchers("/add-note")
                    .hasAnyAuthority(Role.USER.name())
                .antMatchers("/terminal.js")
                    .hasAnyAuthority(Role.DEVELOPER.name())
                .anyRequest()
                    .authenticated()
                .and()
                    .formLogin()
                        .loginPage("/login")
                        .failureUrl("/login?loginError")
                        .defaultSuccessUrl("/mail-confirm")
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
                        .key("AUJf1g3u1eHnpGTV3GYPJ%r1qUVNqF7msy$YNUAw")
                        .useSecureCookie(true)
                .and()
                    .csrf()
                        .ignoringAntMatchers("/terminal/**");
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
}
