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
        Long id   = req.getId();
        String name = req.getMemberName();

        // 기본 검증
        if (id == null || name == null || name.isBlank())
            return false;

        // 기존 멤버 조회
        Members existing = membersRepository.findById(id).orElse(null);
        if (existing == null)
            return false;

        boolean wasLeader    = Boolean.TRUE.equals(existing.isCellLeaderStatus());
        boolean willBeLeader = Boolean.TRUE.equals(req.getCellLeaderStatus());

        // 리더로 지정되는 경우, cellKey 는 항상 자기 자신의 id 로 고정
        if (willBeLeader)
            req.setCellKey(id);

        // 1) 기존 셀리더가 계속 셀리더인 경우
        if (wasLeader && willBeLeader) {

            // 셀 이름을 입력하지 않으면 → 기존 셀 이름 유지
            if (req.getCellName() == null || req.getCellName().isBlank())
                req.setCellName(existing.getCellName());

            // 셀 이름이 최종 결정되었으면, 같은 cellKey 가진 셀원들의 셀 이름 동기화
            if (req.getCellName() != null && !req.getCellName().isBlank())
                membersRepository.updateCellNameByCellKeyExceptSelf(id, req.getCellName());

        // 2) 기존에는 리더가 아니었는데, 새로 리더가 되는 경우
        } else if (!wasLeader && willBeLeader) {

            // 셀 이름을 입력하지 않았을 때만 자동 생성 + 중복 처리
            if (req.getCellName() == null || req.getCellName().isBlank()) {
                String base        = buildCellBaseName(name);
                String defaultName = base + "셀";

                if (!membersRepository.existsByCellName(defaultName)) {
                    // 중복 없음 → 기본 이름 사용
                    req.setCellName(defaultName);
                } else {
                    // 같은 이름의 기존 리더들 셀명 년도 기반으로 리네임
                    List<Members> duplicates =
                            membersRepository.findByCellLeaderStatusTrueAndCellName(defaultName);

                    for (Members m : duplicates) {
                        String yy      = getYearSuffix(m.getMemberBirth());
                        String updated = (yy != null ? yy : "") + base + "셀";

                        m.setCellName(updated);
                        membersRepository.updateCellNameByCellKeyExceptSelf(m.getId(), updated);
                    }

                    // 본인 셀 이름도 년도 기반으로 설정
                    String yy = getYearSuffix(req.getMemberBirth());
                    req.setCellName((yy != null ? yy : "") + base + "셀");
                }
            }

            // 같은 cellKey(=본인 id)를 가진 셀원들의 셀 이름도 함께 업데이트
            membersRepository.updateCellNameByCellKeyExceptSelf(id, req.getCellName());

        // 3) 기존에는 리더였는데, 이번에 리더를 해제하는 경우
        } else if (wasLeader && !willBeLeader) {

            // 이 리더 밑에 있던 셀원들의 셀 정보 초기화 (deleteMember에서 쓰는 것 재사용)
            membersRepository.resetCellNameByCellKey(id);

            // 본인도 셀 정보 제거
            req.setCellKey(null);
            req.setCellName(null);
        }

        // 4) 공통 정보 업데이트
        int updatedCount = membersRepository.updateMemberById(
                id,
                name,
                req.getMemberBirth(),
                req.getMemberPhoneNumber(),
                req.getMemberAddress(),
                req.getBaptismStatus(),
                req.getNurtureYear(),
                req.getNurtureSemester(),
                req.getGrowthYear(),
                req.getGrowthSemester(),
                req.getMemberMemo(),
                willBeLeader,
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
        if (korean.isEmpty())
            return memberName;

        int len = korean.length();

        // 성 제거, 나머지 전체
        if (len >= 2)
            return korean.substring(1);
        else
            return korean; // 한 글자 이름
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
