package com.site.elohim.service;

import com.site.elohim.model.Users;
import com.site.elohim.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersService {
    @Autowired
    private UsersRepository usersRepository;
    private PasswordEncoder passwordEncoder;

    public Optional<Users> selectUsers(String userId, String userPassword) {

        return usersRepository.findByUserIdAndUserPassword(userId,  passwordEncoder.encode(userPassword));
    }
}
