package com.krokochik.ideasForum.service;

import com.krokochik.ideasForum.model.User;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserValidationService {
    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

    public static boolean validate(User user) throws EmailFormatException, UsernameLengthException, PasswordInsecureException, NullPointerException {
        if (user.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.\\S+$).{8,}$")) {
            if (!user.getUsername().isEmpty()) {
                if (user.getUsername().length() >= 4) {
                    if (rfc2822.matcher(user.getEmail()).matches()) {
                        return true;
                    } else throw new EmailFormatException();
                } else throw new UsernameLengthException();
            } else throw new NullPointerException();
        } else throw new PasswordInsecureException();
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
