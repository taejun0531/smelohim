package com.site.elohim.service;

import com.site.elohim.model.Members;
import com.site.elohim.model.Users;
import com.site.elohim.repository.MembersRepository;
import com.site.elohim.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class userAcceptService {

    private final UsersRepository usersRepository;
    private final MembersRepository membersRepository;

    public List<Users> getAllUser () {
        return usersRepository.findAll();
    }

    public List<Users> findAllUserByRole(String role) {
        return usersRepository.findByUserRoleOrderByUserName(role);
    }

    public List<Members> getMembersCellLeader() {
        return membersRepository.findByCellLeaderStatusOrderByMemberNameAsc(true);
    }

    @Transactional
    public boolean deleteUser(Long id) {
        try {
            usersRepository.deleteById(id);
            usersRepository.flush();                 // 강제로 delete SQL 실행
            return !usersRepository.existsById(id);  // id가 없을 때 true 반환 시켜 삭제 완료되었다고 리턴해줌.
        } catch (EmptyResultDataAccessException | DataIntegrityViolationException e) {
            // 이미 없는 사용자 // FK 제약 등으로 삭제 불가
            return false;
        }
    }

    @Transactional
    public boolean updateUser(String Id, String userRole){
        Long LongId = Long.parseLong(Id);

        if(usersRepository.findById(LongId).isEmpty())
            return false;

        Users user = usersRepository.findById(LongId).get();
        user.setUserRole(userRole);
        return true;
    }

}
