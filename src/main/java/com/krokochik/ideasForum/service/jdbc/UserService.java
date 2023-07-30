package com.krokochik.ideasForum.service.jdbc;

import com.krokochik.ideasForum.model.functional.Role;
import com.krokochik.ideasForum.model.db.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.crypto.TokenService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenService tokenService;

    HashMap<String, Timer> mfaCodeGeneratingWorkers = new HashMap<>(); // <username, worker>

    {
        // start the mfa code generating cycle for in-db users
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                User[] users = userRepository.getAllUsers();
                for (User user : users) {
                    if (user.isMfaActivated()) {
                        startGeneratingMfaCode(user);
                    }
                }
            }
        }, 1000);

    }

    @SneakyThrows
    public void startGeneratingMfaCode(User user) {
        Timer worker = new Timer();
        worker.schedule(new TimerTask() {
            @Override
            public void run() {
                user.setMfaCode(tokenService.generateMfaCode());
                System.out.println(user.getUsername());
                System.out.println(user.getMfaCode());
            }
        }, 0, 30 * 1000);
        mfaCodeGeneratingWorkers.put(user.getUsername(), worker);
    }

    public void startGeneratingMfaCode(String username) {
        startGeneratingMfaCode(userRepository.findByUsername(username));
    }

    @SneakyThrows
    public void stopGeneratingMfaCodes(String username) {
        mfaCodeGeneratingWorkers.remove(username).cancel();
    }

    public void stopGeneratingMfaCodes(User user) {
        stopGeneratingMfaCodes(user.getUsername());
    }

    public void setRolesById(Long id, Set<Role> roles) {
        userRepository.clearRoles(id);
        roles.forEach(role -> userRepository.addRole(id, role.name()));
    }
}
