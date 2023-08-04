package com.krokochik.ideasforum.service.jdbc;

import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.repository.UserRepository;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// It is necessary to implement remember me

@Service
public class CustomUserDetailsService extends SecurityRoutineProvider implements UserDetailsService  {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("Couldn't find a user with username '" + username + "'");
        System.out.println("User details");
        return convertUserToUserDetails(user);
    }


}
