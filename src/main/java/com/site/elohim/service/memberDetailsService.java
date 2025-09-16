package com.site.elohim.service;

import com.site.elohim.model.Members;
import com.site.elohim.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class memberDetailsService {

    private final MembersRepository membersRepository;

    public Optional<Members> findByMemberId(Long memberId) {
        return membersRepository.findById(memberId);
    }

    public List<Members> getMembersCellLeader() {
        return membersRepository.findByCellLeaderStatusOrderByMemberNameAsc(true);
    }

}
