package com.krokochik.ideasForum.service;

import com.krokochik.ideasForum.model.db.User;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserValidationService {
    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

    public static boolean validate(User user) throws EmailFormatException, UsernameLengthException, PasswordInsecureException, NullPointerException {
        if (!user.getUsername().isEmpty()) {
            if (user.getUsername().length() >= 4) {
                if (validateEmail(user.getEmail())) {
                    if (validatePassword(user.getPassword()))
                        return true;
                    else throw new PasswordInsecureException();
                } else throw new EmailFormatException();
            } else throw new UsernameLengthException();
        } else throw new NullPointerException();
    }

    public static boolean validatePassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.\\S+$).{8,}$");
    }

    public static boolean validateEmail(String email) {
        return rfc2822.matcher(email).matches();
    }

    public static class EmailFormatException extends Exception {
        EmailFormatException() {
            super();
        }
    }

    public static class UsernameLengthException extends Exception {
        public UsernameLengthException() {
            super();
        }
    }

    public static class PasswordInsecureException extends Exception {
        public PasswordInsecureException() {
            super();
        }
    }

}
