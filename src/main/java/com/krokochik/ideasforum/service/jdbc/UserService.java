package com.krokochik.ideasforum.service.jdbc;

import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.functional.Role;
import com.krokochik.ideasforum.repository.UserRepository;
import com.krokochik.ideasforum.service.crypto.TokenService;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenService tokenService;

    // Workers are purposed to generate mfa codes for each user
    HashMap<String, ScheduledExecutorService> mfaCodeGeneratingWorkers = new HashMap<>(); // <username, worker>

    @PostConstruct
    public void init() {
        startGeneratingMfaCodeForAllUsers();
    }

    /**
     * Obtains all users from the database and
     * starts mfa-code generating cycle for each user,
     * if his mfa is activated, todo and he was last seen less than 2 weeks ago.
     */
    private void startGeneratingMfaCodeForAllUsers() {
        User[] users = userRepository.getAllUsers();
        for (User user : users) {
            if (user.isMfaActivated()) {
                startGeneratingMfaCode(user);
            }
        }
    }

    /**
     * Starts generating MFA codes every 30 seconds for the specified user.
     *
     * @param user the user.
     * @throws NullPointerException if the user is {@code null}.
     */
    @SneakyThrows
    public void startGeneratingMfaCode(@NonNull User user) {
        ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        worker.scheduleAtFixedRate(() ->
                user.setMfaCode(tokenService.generateMfaCode()), 0, 30, TimeUnit.SECONDS);
        mfaCodeGeneratingWorkers.put(user.getUsername(), worker);
    }

    /**
     * Starts generating MFA codes every 30 seconds
     * for the user with the specified username.
     *
     * @param username the username.
     * @throws NullPointerException if username is {@code null}.
     */
    public void startGeneratingMfaCode(@NonNull String username) {
        startGeneratingMfaCode(userRepository.findByUsername(username));
    }

    /**
     * Stops generating MFA codes every 30 seconds
     * for the user with the specified username.
     *
     * @param username the username.
     * @throws NullPointerException if username is {@code null}.
     */
    public void stopGeneratingMfaCode(@NonNull String username) {
        ScheduledExecutorService worker = mfaCodeGeneratingWorkers.remove(username);
        if (worker != null) {
            worker.shutdown();
        }
    }

    /**
     * Stops generating MFA codes for the specified user.
     *
     * @param user the user.
     * @throws NullPointerException if user is {@code null}.
     */
    public void stopGeneratingMfaCode(@NonNull User user) {
        stopGeneratingMfaCode(user.getUsername());
    }

    /**
     * Sets the roles for the user with the specified ID.
     *
     * @param id    the ID of the user for whom to set the roles.
     * @param roles the set of roles to be set for the user.
     *              If {@code null} or empty, removes all roles.
     * @throws NullPointerException if ID is {@code null}.
     * @throws IllegalArgumentException if the ID is negative.
     */
    public void setRolesById(@NonNull Long id, Set<Role> roles) {
        if (id < 0) throw new IllegalArgumentException("Id must be greater than or equal to 0.");

        userRepository.clearRoles(id);
        if (roles != null && !roles.isEmpty())
            roles.forEach(role -> userRepository.addRole(id, role.name()));
    }

    /**
     * Checks if a user with the specified username exists.
     *
     * @param username the username.
     * @return true if the user exists, false otherwise.
     * @throws NullPointerException if username is {@code null}.
     */
    public boolean exists(@NonNull String username) {
        return userRepository.findByUsername(username) != null;
    }

    /**
     * Checks if a user with the specified username exists.
     *
     * @param id the ID.
     * @return true if the user exists, false otherwise.
     * @throws NullPointerException if ID is {@code null}.
     * @throws IllegalArgumentException if ID is negative.
     */
    public boolean exists(@NonNull Long id) {
        if (id < 0) throw new IllegalArgumentException("Id must be greater than or equal to 0.");

        return userRepository.findById(id).isPresent();
    }
}
