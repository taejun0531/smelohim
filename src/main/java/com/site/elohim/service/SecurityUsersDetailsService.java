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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityUsersDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Optional<Users> OPuser = usersRepository.findByUserId(userId);

        if(OPuser.isEmpty())
            return null;

        Users user = OPuser.get();

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getUserRole().equals("ADMIN")) {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
        } else if (user.getUserRole().equals("USER")) {
            authorities.add(new SimpleGrantedAuthority(Role.USER.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(Role.AWAIT.getValue()));
        }

        return new User(user.getUserId(), user.getUserPassword(), authorities);
    }

}
