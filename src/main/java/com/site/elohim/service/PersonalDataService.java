package com.site.elohim.service;

import com.site.elohim.dto.CreateMemberRequest;
import com.site.elohim.dto.PersonalDataSearchRequest;
import com.site.elohim.model.Members;
import com.site.elohim.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalDataService {

    private final MembersRepository membersRepository;

    // ====== 공통 유틸 ======
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Long parseLongOrNull(String value) {
        if (isBlank(value))
            return null;

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // --- category 별 내부 전용 헬퍼들 ----------------------------------

    private List<Members> searchByBirth(String birthYear, String birthMonth) {

        // 연도/월이 모두 비어 있으면 의미 없는 요청이므로 빈 리스트 반환 (예외 방지)
        if (isBlank(birthYear) && isBlank(birthMonth))
            return List.of();

        if (isBlank(birthMonth))
            return membersRepository.findByBirthYear(Integer.parseInt(birthYear));

        if (isBlank(birthYear))
            return membersRepository.findByBirthMonth(Integer.parseInt(birthMonth));

        return membersRepository.findByBirthYearAndMonth(Integer.parseInt(birthYear), Integer.parseInt(birthMonth));
    }

    private List<Members> searchByNurture(String nurtureYear, String nurtureSemester) {

        if (isBlank(nurtureYear) && isBlank(nurtureSemester))
            return List.of();

        if (isBlank(nurtureYear))
            return membersRepository.findByNurtureSemesterOrderByMemberNameAsc(nurtureSemester);

        if (isBlank(nurtureSemester))
            return membersRepository.findByNurtureYearContainingOrderByMemberNameAsc(nurtureYear);

        return membersRepository.findByNurtureYearContainingAndNurtureSemesterOrderByMemberNameAsc(nurtureYear, nurtureSemester);
    }

    private List<Members> searchByGrowth(String growthYear, String growthSemester) {

        if (isBlank(growthYear) && isBlank(growthSemester))
            return List.of();

        if (isBlank(growthYear))
            return membersRepository.findByGrowthSemesterOrderByMemberNameAsc(growthSemester);

        if (isBlank(growthSemester))
            return membersRepository.findByGrowthYearContainingOrderByMemberNameAsc(growthYear);

        return membersRepository.findByGrowthYearContainingAndGrowthSemesterOrderByMemberNameAsc(growthYear, growthSemester);
    }

    private List<Members> sortByName(String sortIdStr) {
        Long sortId = parseLongOrNull(sortIdStr);
        // sortId가 0이면 오름차순, 1이면 내림차순 (null 포함 0 취급)
        if (sortId == null || sortId == 0L)
            return membersRepository.findAllByOrderByMemberNameAsc();
        return membersRepository.findAllByOrderByMemberNameDesc();
    }

    private List<Members> sortByBirth(String sortIdStr) {
        Long sortId = parseLongOrNull(sortIdStr);
        // sortId가 0이면 오름차순, 1이면 내림차순 (null 포함 0 취급)
        if (sortId == null || sortId == 0L)
            return membersRepository.findAllByOrderByMemberBirthAsc();
        return membersRepository.findAllByOrderByMemberBirthDesc();
    }

    // ===== 외부 호출 메서드 =====
    public List<Members> getAllMember() {
        return membersRepository.findAll();
    }

    public List<Members> getMembersCellLeader() {
        return membersRepository.findByCellLeaderStatusOrderByMemberNameAsc(true);
    }

    public List<Members> searchMembers(PersonalDataSearchRequest req) {

        String category = req.getFindCategory();
        // 카테고리 없으면 전체 조회
        if (isBlank(category))
            return getAllMember();

        return switch (category) {
            case "name" -> membersRepository.findByMemberNameContainingOrderByMemberNameAsc(req.getFindName());

            case "birth" -> searchByBirth(req.getFindBirth_year(), req.getFindBirth_month());

            case "allCellLeader" -> getMembersCellLeader();

            case "cellLeader" -> {
                Long cellKey = parseLongOrNull(req.getFindCellLeader());
                if (cellKey == null)
                    yield List.of();
                yield membersRepository.findByCellKeyOrderByMemberNameAsc(cellKey);
            }

            case "baptism" -> {
                String baptism = req.getFindBaptism();
                if (isBlank(baptism))
                    yield List.of();
                yield membersRepository.findByBaptismStatusOrderByMemberNameAsc(baptism);
            }

            case "nurture" -> searchByNurture(req.getFindNurture_year(), req.getFindNurture_semester());

            case "growth" -> searchByGrowth(req.getFindGrowth_year(), req.getFindGrowth_semester());

            case "nameSort" -> sortByName(req.getSortId());

            case "birthSort" -> sortByBirth(req.getSortId());

            default -> getAllMember();   // "ALL" 혹은 알 수 없는 카테고리
        };
    }

    @Transactional
    public boolean createMember(CreateMemberRequest req) {

        // 필수값 이름 체크
        if (isBlank(req.getMemberName()))
            return false;

        Members member = Members.builder()
                .memberName(req.getMemberName())
                .memberBirth(req.getMemberBirth())
                .memberPhoneNumber(req.getMemberPhoneNumber())
                .baptismStatus(req.getMemberBaptism())
                .nurtureYear(req.getNurtureYear())
                .nurtureSemester(req.getNurtureSemester())
                .growthYear(req.getGrowthYear())
                .growthSemester(req.getGrowthSemester())
                .cellKey(req.getCellKey())
                .cellName(req.getCellName())
                .cellLeaderStatus(false)   // 신규 생성 시 기본 false 유지
                .build();

        membersRepository.save(member);
        return true;
    }

    /**
     * 있다면 true, 없다면 false
     */
    public boolean checkMember(String name, LocalDate birth) {
        if (birth == null)
            return false;
        return membersRepository.existsMembersByMemberNameAndMemberBirth(name, birth);
    }

}
