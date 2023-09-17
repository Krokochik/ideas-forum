package com.krokochik.ideasforum.config;

import dev.samstevens.totp.time.NtpTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.net.UnknownHostException;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
    @Bean
    public TimeProvider timeProvider() throws UnknownHostException {
        return new NtpTimeProvider("pool.ntp.org");
    }
}
