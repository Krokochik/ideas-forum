package com.krokochik.ideasforum.service.jdbc;

import com.krokochik.ideasforum.annotation.NonBlank;
import com.krokochik.ideasforum.annotation.NonNegative;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.functional.Role;
import com.krokochik.ideasforum.repository.UserRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * Checks if a user with the specified username exists.
     *
     * @param username the username.
     * @return true if the user exists, false otherwise.
     */
    public boolean exists(@NonNull String username) {
        return userRepository.findByUsername(username) != null;
    }

    /**
     * Checks if a user with the specified username exists.
     *
     * @param id the ID.
     * @return true if the user exists, false otherwise.
     */
    public boolean exists(@NonNegative Long id) {
        return userRepository.findById(id).isPresent();
    }

    /**
     * Checks if a specified user exists.
     *
     * @param user the user.
     * @return true if the user exists, false otherwise.
     * @throws NullPointerException if user is {@code null} or
     *                              username is {@code null} and ID is {@code null} or negative.
     */
    public boolean exists(@NonNull User user) {
        if (user.getId() != null && user.getId() >= 0)
            return exists(user.getId());
        else return exists(user.getUsername());
    }

    /**
     * Saves a new user to database.
     *
     * @param user the user to be saved.
     * @param encryptPassword should the user's password be encrypted before saving.
     * @return {@link Optional} of saved user if the successfully saved,
     * empty {@link Optional} if the user is already exists.
     * @throws NullPointerException if user is {@code null} or
     *                              username is {@code null} and
     *                              ID is {@code null} or negative.
     */
    public Optional<User> save(@NonNull User user, boolean encryptPassword) {
        if (!exists(user)) {
            if (encryptPassword) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return Optional.of(userRepository.save(user));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Overloads {@link #save(User, boolean)} with encryptPassword set to true.
     *
     * @param user the user to be saved.
     * @return {@link Optional} of saved user if the successfully saved,
     * empty {@link Optional} if the user is already exists.
     *
     * @see #save(User, boolean)
     */
    public Optional<User> save(@NonNull User user) {
        return save(user, true);
    }

    /**
     * Updates the user in the database.
     *
     * @param user the user to be updated.
     * @param encryptPassword should the user's password be encrypted before updating.
     * @return {@link Optional} of updated user if the successfully saved,
     * empty {@link Optional} if the user doesn't exist.
     * @throws NullPointerException if user is {@code null} or
     *                              username is {@code null} and
     *                              ID is {@code null} or negative.
     */
    public Optional<User> update(@NonNull User user, boolean encryptPassword) {
        if (exists(user)) {
            if (encryptPassword) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return Optional.of(userRepository.save(user));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Overloads {@link #update(User, boolean)} with encryptPassword set to false.
     *
     * @param user the user to be updated.
     * @return {@link Optional} of updated user if the successfully saved,
     *          empty {@link Optional} if the user doesn't exist.
     *
     * @see #update(User, boolean)
     */
    public Optional<User> update(@NonNull User user) {
        return update(user, false);
    }

    public Optional<User> findById(@NonNegative Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByOAuth2Id(@NonBlank String id) {
        return Optional.ofNullable(userRepository.findUserByOAuth2Id(id));
    }

    public Optional<User> findByUsername(@NonNull String username) {
        User user = userRepository.findByUsername(username);
        return Optional.ofNullable(user);
    }

    /**
     * Obtains user with the specified username from database.<p>
     * If user was found returns it and {@link User#unknown()} otherwise.
     **/
    @Contract("_ -> !null")
    public @NonNull User findByUsernameOrUnknown(@NonNull String username) {
        User user = userRepository.findByUsername(username);
        if (user != null)
            return user;
        else return User.unknown();
    }

    @Contract(" -> !null")
    public @NonNull User[] getAllUsers() {
        User[] users = userRepository.getAllUsers();
        if (users != null)
            return users;
        else return new User[]{};
    }

    /**
     * Sets the roles for the user with the specified ID.
     *
     * @param id    the ID of the user for whom to set the roles.
     * @param roles the set of roles to be set for the user.
     *              If {@code null} or empty, removes all roles.
     */
    public void setRolesById(@NonNegative Long id, Set<Role> roles) {
        clearRoles(id);
        if (roles != null && !roles.isEmpty())
            roles.forEach(role -> addRole(id, role));
    }

    public void clearRoles(@NonNegative Long id) {
        userRepository.clearRoles(id);
    }

    public void addRole(@NonNegative Long id, @NonNull Role role) {
        userRepository.addRole(id, role.name());
    }

    public void setQRCodeById(byte[] qrcode, @NonNegative Long id) {
        userRepository.setQRCodeById(qrcode, id);
    }

    public void setAvatarById(byte[] avatar, @NonNegative Long id) {
        userRepository.setAvatarById(avatar, id);
    }

    public void setUsernameById(@NonNull String username, @NonNegative Long id) {
        userRepository.setUsernameById(username, id);
    }

    public void setNicknameById(@NonNull String nickname, @NonNegative Long id) {
        userRepository.setNicknameById(nickname, id);
    }

    public void setEmailById(@NonNull String email, @NonNegative Long id) {
        userRepository.setEmailById(email, id);
    }

    public void setPasswordById(@NonNull String password, @NonNegative Long id) {
        password = passwordEncoder.encode(password);
        userRepository.setPasswordById(password, id);
    }

    public void setMfaResetTokensById(@NonNull Set<String> tokens, @NonNegative Long id) {
        userRepository.setMfaResetTokensById(tokens, id);
    }

    public void setConfirmMailSentById(boolean sent, @NonNegative Long id) {
        userRepository.setConfirmMailSentById(sent, id);
    }

    public void setPasswordAbortSentById(boolean sent, @NonNegative Long id) {
        userRepository.setPasswordAbortSentById(sent, id);
    }

    public void setMailConfirmationTokenById(@NonNull String token, @NonNegative Long id) {
        userRepository.setMailConfirmationTokenById(token, id);
    }

    public void setPasswordAbortTokenById(@NonNull String token, @NonNegative Long id) {
        userRepository.setPasswordAbortTokenById(token, id);
    }

    public void setMfaTokenById(@NonNull String token, @NonNegative Long id) {
        userRepository.setMfaTokenById(token, id);
    }
}
