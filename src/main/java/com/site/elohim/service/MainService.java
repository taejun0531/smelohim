package com.site.elohim.service;

import com.site.elohim.model.Users;
import com.site.elohim.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final UsersRepository usersRepository;

    public String getUsernameByUserId(String userId) {
        return usersRepository.findByUserId(userId).map(Users::getUserName)
                .orElseThrow(() -> new IllegalArgumentException("해당 userId(" + userId + ")를 가진 유저가 존재하지 않습니다."));
    }
}
