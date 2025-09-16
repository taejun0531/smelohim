package com.site.elohim.service;

import com.site.elohim.model.Users;
import com.site.elohim.model.createAccountRequest;
import com.site.elohim.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class createAccountService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public boolean createAccount(createAccountRequest request) {

        Optional<Users> tmp = usersRepository.findByUserId(request.getUserId());

        if(tmp.isPresent())
            return false;

        if(!request.getUserPassword().equals(request.getUserCheckPassword()))
            return false;

        Users user = Users.builder()
                .userId(request.getUserId())
                .userPassword(passwordEncoder.encode(request.getUserPassword()))
                .userName(request.getUserName())
                .userRole("AWAIT")
                .build();

        usersRepository.save(user);

        return usersRepository.findByUserId(user.getUserId()).isPresent();
    }

    public boolean userIdCheck(String userId){
        Optional<Users> tmp = usersRepository.findByUserId(userId);
        return tmp.isEmpty();
    }

    public boolean userNameCheck(String userName){
        Optional<Users> tmp = usersRepository.findByUserName(userName);
        return tmp.isEmpty();
    }

}
