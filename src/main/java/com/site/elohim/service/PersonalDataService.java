package com.site.elohim.service;

import com.site.elohim.model.Members;
import com.site.elohim.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalDataService {

    private final MembersRepository membersRepository;

    public List<Members> getAllMember () {
        return membersRepository.findAll();
    }

    public List<Members> getMembersCellLeader() {
        return membersRepository.findByCellLeaderStatusOrderByMemberNameAsc(true);
    }

    public List<Members> getMembersByName(String name) {
        return membersRepository.findByMemberNameContainingOrderByMemberNameAsc(name);
    }

    public List<Members> getMembersByBirth(String birthYear, String birthMonth) {
        if (birthMonth == null || birthMonth.isBlank())
            return membersRepository.findByBirthYear(Integer.parseInt(birthYear));

        if (birthYear == null || birthYear.isBlank())
            return membersRepository.findByBirthMonth(Integer.parseInt(birthMonth));

        return membersRepository.findByBirthYearAndMonth(Integer.parseInt(birthYear), Integer.parseInt(birthMonth));
    }

    public List<Members> getMembersByCellKey(Long cellKey) {
        return membersRepository.findByCellKeyOrderByMemberNameAsc(cellKey);
    }

    public List<Members> getMembersByBaptism(String baptism) {
        return membersRepository.findByBaptismStatusOrderByMemberNameAsc(baptism);
    }

    public List<Members> getMembersByNurtureYear(String nurtureYear) {
        return membersRepository.findByNurtureYearContainingOrderByMemberNameAsc(nurtureYear);
    }

    public List<Members> getMembersByNurtureSemester(String nurtureSemester) {
        return membersRepository.findByNurtureSemesterOrderByMemberNameAsc(nurtureSemester);
    }

    public List<Members> getMembersByNurtureYearAndNurtureSemester(String nurtureYear, String nurtureSemester) {
        return membersRepository.findByNurtureYearContainingAndNurtureSemesterOrderByMemberNameAsc(nurtureYear, nurtureSemester);
    }

    public List<Members> getMembersByGrowthYear(String GrowthYear) {
        return membersRepository.findByGrowthYearContainingOrderByMemberNameAsc(GrowthYear);
    }

    public List<Members> getMembersByGrowthSemester(String GrowthSemester) {
        return membersRepository.findByGrowthSemesterOrderByMemberNameAsc(GrowthSemester);
    }

    public List<Members> getMembersByGrowthYearAndGrowthSemester(String GrowthYear, String GrowthSemester) {
        return membersRepository.findByGrowthYearContainingAndGrowthSemesterOrderByMemberNameAsc(GrowthYear, GrowthSemester);
    }

    public List<Members> getMembersNameSort(Long sortId) {
        // sortId가 0이면 오름차순, 1이면 내림차순
        if(sortId == 0)
            return membersRepository.findAllByOrderByMemberNameAsc();
        else
            return membersRepository.findAllByOrderByMemberNameDesc();
    }

    public List<Members> getMembersBirthSort(Long sortId) {
        // sortId가 0이면 오름차순, 1이면 내림차순
        if(sortId == 0)
            return membersRepository.findAllByOrderByMemberBirthAsc();
        else
            return membersRepository.findAllByOrderByMemberBirthDesc();
    }

    public boolean createMember(Members members) {
        membersRepository.save(members);
        return true;
    }

    // 있다면 true 없다면 false
    public boolean checkMember(String name, LocalDate birth) {
        if (birth == null)
            return false;

        return membersRepository.existsMembersByMemberNameAndMemberBirth(name, birth);
    }

}
