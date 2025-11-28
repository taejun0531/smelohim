package com.site.elohim.service;

import com.site.elohim.dto.CreateAccountRequest;
import com.site.elohim.model.Role;
import com.site.elohim.model.Users;
import com.site.elohim.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateAccountService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     * - userId 중복이면 false
     * - 비밀번호/확인 비밀번호 다르면 false
     * - 성공 시 true
     */
    @Transactional
    public boolean createAccount(CreateAccountRequest request) {

        // 1) 아이디 중복 체크
        Optional<Users> existing = usersRepository.findByUserId(request.getUserId());
        if (existing.isPresent())
            return false;

        // 2) 비밀번호 & 비밀번호 확인 일치 여부
        if (!request.getUserPassword().equals(request.getUserCheckPassword()))
            return false;

        // 3) 유저 생성 (기본 ROLE = AWAIT)
        Users user = Users.builder()
                .userId(request.getUserId())
                .userPassword(passwordEncoder.encode(request.getUserPassword()))
                .userName(request.getUserName())
                .userRole(Role.AWAIT.name())    // "AWAIT"
                .build();

        usersRepository.save(user);

        return true;
    }

    /**
     * 아이디 사용 가능 여부
     * - true  = 사용 가능 (없음)
     * - false = 이미 존재
     */
    public boolean userIdCheck(String userId) {
        return usersRepository.findByUserId(userId).isEmpty();
    }

    /**
     * 이름 사용 가능 여부
     * - true  = 사용 가능 (없음)
     * - false = 이미 존재
     */
    public boolean userNameCheck(String userName) {
        return usersRepository.findByUserName(userName).isEmpty();
    }

}
