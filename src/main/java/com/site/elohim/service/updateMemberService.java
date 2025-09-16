package com.site.elohim.service;

import com.site.elohim.model.Members;
import com.site.elohim.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class updateMemberService {

    private final MembersRepository membersRepository;

    public Members getMemberByMemberId(Long memberId){
        if(existsMemberById(memberId))
            return membersRepository.findById(memberId).get();
        else
            return null;
    }

    public boolean existsMemberById(Long memberId){
        return membersRepository.existsById(memberId);
    }

}
