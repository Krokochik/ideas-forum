package com.krokochik.ideasforum.service;

import com.krokochik.ideasforum.model.db.User;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserValidator {
    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

    private static final Pattern securePassword = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.\\S+$).{8,}$"
    );

    public static Enum<? extends Enum<?>> validate(@NonNull User user) {
        if (user.getUsername().isBlank()) {
            return Result.INVALID_USERNAME.BLANK;
        }
        if (user.getUsername().length() < 4) {
            return Result.INVALID_USERNAME.SHORT;
        }
        if (!validateEmail(user.getEmail())) {
            return Result.INVALID_EMAIL;
        }
        if (!validatePassword(user.getPassword())) {
            return Result.INVALID_PASSWORD;
        }
        return Result.OK;
    }

    public enum Result {
        OK,
        INVALID_PASSWORD,
        INVALID_EMAIL;
        public enum INVALID_USERNAME {
            BLANK,
            SHORT
        }
    }

    /**
     * Returns if the password is at least 8 chars long,
     * contains a number, upper and lower case letter, special symbol.
     **/
    public static boolean validatePassword(String password) {
        return securePassword.matcher(password).matches();
    }

    /**
     * Returns if email is matching rfc2822 standard.
     **/
    public static boolean validateEmail(String email) {
        return rfc2822.matcher(email).matches();
    }

}
