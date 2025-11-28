package com.site.elohim.service;

import com.site.elohim.dto.UpdateMemberRequest;
import com.site.elohim.model.Members;
import com.site.elohim.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberDetailsService {

    private final MembersRepository membersRepository;

    public Optional<Members> findByMemberId(Long memberId) {
        return membersRepository.findById(memberId);
    }

    public List<Members> getMembersCellLeader() {
        return membersRepository.findByCellLeaderStatusOrderByMemberNameAsc(true);
    }

    /**
     * 멤버 정보 수정
     */
    @Transactional
    public boolean updateMember(UpdateMemberRequest req) {

        Long id = req.getId();
        if (id == null || req.getMemberName() == null || req.getMemberName().isBlank())
            return false;

        // 셀 리더로 설정하는 경우
        if (Boolean.TRUE.equals(req.getCellLeaderStatus())) {

            // 자기 자신을 셀 리더로 지정
            req.setCellKey(id);

            // 기존 셀 이름이 없는 경우 -> 새로 생성
            if (req.getCellName() == null || req.getCellName().isBlank()) {
                String base = buildCellBaseName(req.getMemberName());
                String newName = base + "셀";

                if (!membersRepository.existsByCellName(newName)) {
                    // 중복 없음 → 바로 사용
                    req.setCellName(newName);
                } else {
                    // 중복되는 기존 리더들의 셀 이름까지 년도 기반으로 리네임 (ex. 03태준셀, 99태준셀)
                    List<Members> duplicates = membersRepository.findByCellLeaderStatusTrueAndCellName(newName);

                    for (Members m : duplicates) {
                        String yy = getYearSuffix(m.getMemberBirth());
                        String updated = (yy != null ? yy : "") + base + "셀";

                        m.setCellName(updated);
                        membersRepository.updateCellNameByCellKeyExceptSelf(m.getId(), updated);
                    }

                    // 본인의 셀 이름
                    String yy = getYearSuffix(req.getMemberBirth());
                    req.setCellName((yy != null ? yy : "") + base + "셀");
                }
            }

            // 같은 cellKey 를 가진 다른 멤버들의 cellName 도 같이 업데이트
            membersRepository.updateCellNameByCellKeyExceptSelf(id, req.getCellName());
        }

        // 본인 정보 업데이트
        int updatedCount = membersRepository.updateMemberById(
                req.getId(),
                req.getMemberName(),
                req.getMemberBirth(),
                req.getMemberPhoneNumber(),
                req.getMemberAddress(),
                req.getBaptismStatus(),
                req.getNurtureYear(),
                req.getNurtureSemester(),
                req.getGrowthYear(),
                req.getGrowthSemester(),
                req.getMemberMemo(),
                Boolean.TRUE.equals(req.getCellLeaderStatus()),
                req.getCellKey(),
                req.getCellName()
        );

        return updatedCount > 0;
    }

    private String extractKoreanChars(String name) {
        if (name == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isKorean(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean isKorean(char c) {
        return (c >= '가' && c <= '힣');
    }

    /**
     * "윤태준"  -> "태준"
     * 외자면 한 글자만
     */
    private String buildCellBaseName(String memberName) {
        String korean = extractKoreanChars(memberName);

        // 한글이 아예 없으면 그냥 전체 이름
        if (korean.isEmpty()) {
            return memberName;
        }

        int len = korean.length();

        // 성 제거, 나머지 전체
        if (len >= 2) {
            return korean.substring(1);
        } else {
            return korean; // 한 글자 이름
        }
    }

    private String getYearSuffix(LocalDate birth) {
        if (birth == null) return null;
        int year = birth.getYear();         // 2003
        String y = String.valueOf(year);    // "2003"
        if (y.length() >= 2)
            return y.substring(y.length() - 2); // "03"
        return null;
    }

    /**
     * 멤버 삭제
     */
    @Transactional
    public boolean deleteMember(Long memberId) {
        Optional<Members> optional = membersRepository.findById(memberId);
        if (optional.isEmpty())
            return false;

        Members member = optional.get();

        // 1) 셀 리더이면 셀원들의 cell 정보 초기화
        if (Boolean.TRUE.equals(member.isCellLeaderStatus()))
            membersRepository.resetCellNameByCellKey(memberId);

        // 2) 멤버 삭제
        membersRepository.deleteById(memberId);

        // 3) 삭제 확인
        return !membersRepository.existsById(memberId);
    }

}
