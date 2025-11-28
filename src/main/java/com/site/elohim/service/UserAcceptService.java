package com.site.elohim.service;

import com.site.elohim.dto.UserAcceptRequest;
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
@Transactional(readOnly = true)
public class UserAcceptService {

    private final UsersRepository usersRepository;
    private final MembersRepository membersRepository;

    /**
     * leaderId 에 해당하는 memberId를 이미 누가 사용 중인지 확인
     * - true  : 아직 아무도 사용 안 함 (비어 있음)
     * - false : 이미 사용 중
     */
    public boolean isLeaderIdEmpty(Long memberId) {
        if (memberId == null)
            return true;
        return !usersRepository.existsByMemberId(memberId);
    }

    /**
     * 역할별 유저 목록 조회
     * ex) "AWAIT", "USER", "ADMIN"
     */
    public List<Users> findAllUserByRole(String role) {
        return usersRepository.findByUserRoleOrderByUserName(role);
    }

    /**
     * 셀 리더로 등록된 멤버 목록 조회
     */
    public List<Members> getMembersCellLeader() {
        return membersRepository.findByCellLeaderStatusOrderByMemberNameAsc(true);
    }

    /**
     * 유저 삭제
     */
    @Transactional
    public boolean deleteUser(Long id) {
        try {
            usersRepository.deleteById(id);
            // FK 제약 오류 등은 flush 시점에 바로 터지도록
            usersRepository.flush();
            return !usersRepository.existsById(id);
        } catch (EmptyResultDataAccessException | DataIntegrityViolationException e) {
            // 이미 없는 사용자 / FK 제약으로 삭제 불가
            return false;
        }
    }

    /**
     * 유저 승인 / 권한 변경 / memberId 연결
     */
    @Transactional
    public boolean updateUser(UserAcceptRequest request) {

        Long userId = request.getId();
        if (userId == null)
            return false;

        Users user = usersRepository.findById(userId).orElse(null);
        if (user == null)
            return false;

        // 권한 변경 (예: "AWAIT" -> "USER" or "ADMIN")
        user.setUserRole(request.getUserRole());

        // 셀 리더 memberId 연결/해제
        if (request.getLeaderId() == null)
            user.setMemberId(null);
        else
            user.setMemberId(request.getLeaderId());

        // 영속성 컨텍스트에 의해 자동 flush
        return true;
    }
}
