package com.site.elohim.service;

import com.site.elohim.model.Users;
import com.site.elohim.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityUsersService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public SecurityUsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<Users> user = usersRepository.findByUserId(userId);

        if(user.isEmpty())
            throw new UsernameNotFoundException("User not found");

        UserDetails userDetails = User.builder()
                .username(user.get().getUserId())
                .password(user.get().getUserPassword())
                .roles(user.get().getUserRole())
                .build();

        System.out.println("SecurityUsersService : " + userDetails);

        return userDetails;
    }

}
