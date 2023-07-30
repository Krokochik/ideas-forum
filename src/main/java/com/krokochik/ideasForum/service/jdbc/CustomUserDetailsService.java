package com.krokochik.ideasForum.service.jdbc;

import com.krokochik.ideasForum.model.db.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.security.SecurityRoutineProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    SecurityRoutineProvider srp;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("Couldn't find a user with username '" + username + "'");
        System.out.println("User details");
        return srp.convertUserToUserDetails(user);
    }
}
