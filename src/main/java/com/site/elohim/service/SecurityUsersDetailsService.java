package com.site.elohim.service;

import com.site.elohim.model.Role;
import com.site.elohim.model.Users;
import com.site.elohim.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityUsersDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Users user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 아이디를 가진 사용자를 찾을 수 없습니다 : " + userId));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // DB에는 "ADMIN", "USER", "AWAIT" 이 저장되어 있다고 가정
        String role = user.getUserRole();

        if ("ADMIN".equals(role)) {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue())); // "ROLE_ADMIN"
        } else if ("USER".equals(role)) {
            authorities.add(new SimpleGrantedAuthority(Role.USER.getValue()));  // "ROLE_USER"
        } else {
            authorities.add(new SimpleGrantedAuthority(Role.AWAIT.getValue())); // "ROLE_AWAIT"
        }

        return new User(user.getUserId(), user.getUserPassword(), authorities);
    }
}
