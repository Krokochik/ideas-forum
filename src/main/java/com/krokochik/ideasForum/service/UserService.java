package com.krokochik.ideasForum.service;

import com.krokochik.ideasForum.model.Role;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Repository
    public interface PrivateUserRepository extends JpaRepository<User, Long> {
    }

    public void setRolesById(Long id, Set<Role> roles) {
        userRepository.clearRoles(id);
        roles.forEach(role -> userRepository.addRole(id, role.name()));
    }
}
