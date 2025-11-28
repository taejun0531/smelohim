package com.site.elohim.service;

import com.site.elohim.model.Members;
import com.site.elohim.model.Users;
import com.site.elohim.repository.MembersRepository;
import com.site.elohim.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final MembersRepository membersRepository;
    private final UsersRepository usersRepository;

    /**
     * 로그인한 셀 리더의 셀원 목록 가져오기
     */
    public List<Members> getMyCellMembers(String loginUserId) {

        Users loginUser = usersRepository.findByUserId(loginUserId)
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없습니다."));

        Long loginUserPkId = loginUser.getId();

        // 리포지토리 구현체에서 loginUserPkId 를 이용해
        // 해당 리더가 담당하는 셀원 목록을 조회
        return membersRepository.findMembersByLeaderUserId(loginUserPkId);
    }

}
