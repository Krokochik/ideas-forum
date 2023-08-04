package com.krokochik.ideasforum.repository;

import com.krokochik.ideasforum.model.db.PersistentRememberMe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class CustomPersistentTokenRepository implements PersistentTokenRepository {

    @Autowired
    RememberMeRepository repository;

    private LocalDateTime getLocal(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        String series = token.getSeries();
        String username = token.getUsername();
        String tokenValue = token.getTokenValue();
        LocalDateTime lastUsed = getLocal(token.getDate());

        repository.save(new PersistentRememberMe(series, username, tokenValue, lastUsed));
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        PersistentRememberMeToken token = getTokenForSeries(series);
        repository.save(new PersistentRememberMe(
                series, token.getUsername(), tokenValue, getLocal(lastUsed)));
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        PersistentRememberMe rememberMe = repository.findById(seriesId).orElse(new PersistentRememberMe());
        return new PersistentRememberMeToken(
                rememberMe.getUsername(),
                rememberMe.getSeries(),
                rememberMe.getToken(),
                Date.from(rememberMe.getLastUsed()
                        .atZone(ZoneId.systemDefault()).toInstant())
        );
    }

    @Override
    public void removeUserTokens(String username) {
        repository.deleteForUsername(username);
    }
}
