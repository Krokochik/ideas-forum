package com.krokochik.ideasforum;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        Sentry.init(options -> {
            options.setDsn("https://e3d706867b357765b2e71e8019ca11d5@o4505852418654208.ingest.sentry.io/4505852418654208");
            options.setDebug(true);
            options.setDiagnosticLevel(SentryLevel.DEBUG);
            options.setTracesSampleRate(1.0);
        });
        SpringApplication.run(Main.class, args);
    }
}
