// /js/memberDetailsPage.js
// 읽기 ↔ 수정 모드 토글, 세례/학기 선택 동기화, 셀 이름 셀렉트 전환, 연도 입력 보정
(function () {
    const form = document.getElementById('memberForm');
    if (!form) return;

    // ===== null → '' 유틸 =====
    const n = (v) => v == null ? '' : String(v);

    // 상단 UI
    const badge     = document.querySelector('.badge');
    const editBtn   = document.getElementById('editBtn');
    const deleteBtn = document.getElementById('deleteBtn');
    const saveBtn   = document.getElementById('saveBtn');
    const cancelBtn = document.getElementById('cancelBtn');

    // 신앙/참석
    const baptismText   = document.getElementById('baptismText');
    const baptismSelect = document.getElementById('baptismSelect');

    const refreshWorshipBtn = document.getElementById('refreshWorshipBtn');
    const refreshCellBtn    = document.getElementById('refreshCellBtn');

    // 리더 여부
    const leaderCheckbox = document.getElementById('cellLeaderStatus');

    // 셀 이름 (읽기/수정 전환)
    const cellNameText   = document.getElementById('cellNameText');
    const cellNameSelect = document.getElementById('cellNameSelect');

    // 양육반 (년도/학기)
    const nurtureYearText  = document.getElementById('nurtureYearText');
    const nurtureYearInput = document.getElementById('nurtureYearInput');
    const nurtureSemText   = document.getElementById('nurtureSemText');
    const nurtureSemSelect = document.getElementById('nurtureSemSelect');

    // 성장반 (년도/학기)
    const growthYearText  = document.getElementById('growthYearText');
    const growthYearInput = document.getElementById('growthYearInput');
    const growthSemText   = document.getElementById('growthSemText');
    const growthSemSelect = document.getElementById('growthSemSelect');

    // 모든 입력(hidden 제외)
    const inputsAll = form.querySelectorAll('input, textarea, select');

    // --- 더미 액션(나중에 API 연결 예정) ---
    refreshWorshipBtn?.addEventListener('click', () => {
        console.log(collectUpdatePayload());
        alert('예배 빈도 갱신 요청은 나중에 연결됩니다.');
    });
    refreshCellBtn?.addEventListener('click', () => {
        alert('셀모임 빈도 갱신 요청은 나중에 연결됩니다.');
    });

    // --- 초기 선택값 동기화 ---
    // 세례 셀렉트: 읽기 텍스트(DB 값)와 동일한 옵션을 선택
    if (baptismSelect && baptismText) {
        const dbVal = n(baptismText.textContent).trim();
        Array.from(baptismSelect.options).forEach(opt => {
            opt.selected = (opt.value === dbVal);
        });
    }

    // 학기 셀렉트: 읽기 텍스트(DB 값: '상반기'/'하반기')와 동일한 옵션 선택
    const syncSemesterSelect = (selectEl, textEl) => {
        if (!selectEl || !textEl) return;
        const dbVal = n(textEl.textContent).trim();
        Array.from(selectEl.options).forEach(opt => {
            opt.selected = (opt.value === dbVal);
        });
    };
    syncSemesterSelect(nurtureSemSelect, nurtureSemText);
    syncSemesterSelect(growthSemSelect,  growthSemText);

    // --- 유틸: 읽기 ↔ 입력 스왑 ---
    const swap = (textEl, inputEl, on) => {
        if (!textEl || !inputEl) return;
        textEl.classList.toggle('hidden', on);
        inputEl.classList.toggle('hidden', !on);
        inputEl.disabled = !on;
    };

    // --- 연도 입력 보정 ---
    // 수정 모드 진입 시, 텍스트에 연도(1900~2999)가 있고 input 값이 비어있다면 채워줌
    const fillYearIfEmpty = (inputEl, textEl) => {
        if (!inputEl || !textEl) return;
        if (String(inputEl.value || '').trim() !== '') return; // 이미 값 있으면 패스
        const txt = n(textEl.textContent).trim();
        const m = txt.match(/\b(19|2[0-9])\d{2}\b/); // 1900~2999
        if (m) inputEl.value = m[0];
    };

    // --- 편집 모드 토글 ---
    let editing = false;
    const setEditing = (on) => {
        editing = on;
        document.body.classList.toggle('editing', on);

        // 전체 입력 활성/비활성 (hidden 제외)
        inputsAll.forEach(el => {
            if (el.type === 'hidden') return;
            el.disabled = !on;
        });

        // 읽기/수정 전환
        swap(baptismText,       baptismSelect,    on);

        swap(nurtureYearText,   nurtureYearInput, on);
        swap(nurtureSemText,    nurtureSemSelect, on);

        swap(growthYearText,    growthYearInput,  on);
        swap(growthSemText,     growthSemSelect,  on);

        swap(cellNameText,      cellNameSelect,   on);

        // 스위치 커서
        const switchUI = document.querySelector('.switch-ui');
        if (switchUI) switchUI.style.cursor = on ? 'pointer' : 'not-allowed';

        // 수정 모드 진입 시 연도 보정
        if (on) {
            fillYearIfEmpty(nurtureYearInput, nurtureYearText);
            fillYearIfEmpty(growthYearInput,  growthYearText);
        }

        // 버튼/뱃지
        saveBtn.classList.toggle('hidden', !on);
        cancelBtn.classList.toggle('hidden', !on);
        editBtn.classList.toggle('hidden', on);
        deleteBtn.classList.toggle('hidden', on);
        if (badge) badge.textContent = on ? '수정 모드' : '읽기 모드';
    };

    // 초기: 읽기 모드
    setEditing(false);

    // 버튼 바인딩
    editBtn?.addEventListener('click', () => setEditing(true));
    cancelBtn?.addEventListener('click', () => location.reload());

    deleteBtn?.addEventListener('click', () => {
        const memberId   = deleteBtn.dataset.id;
        const memberName = deleteBtn.dataset.name;
        const memberYear = deleteBtn.dataset.year;

        if (!memberId) {
            alert("삭제할 청년의 ID를 찾지 못했습니다.");
            return;
        }

        const hasYear = memberYear && memberYear.trim() !== "" && memberYear !== "null";
        const message = hasYear
            ? `정말 "${memberYear}년생 ${memberName}" 청년을 삭제하시겠습니까?\n삭제 후 되돌릴 수 없습니다.`
            : `정말 "${memberName}" 청년을 삭제하시겠습니까?\n삭제 후 되돌릴 수 없습니다.`;

        if (!confirm("🚨 " + message))
            return;

        if (!confirm(`❗ "${memberName}" 청년의 인적사항, 출석체크 등 모든 정보가 모두 삭제됩니다. ❗\n정말로 삭제하시겠습니까?`))
            return;

        fetch("/admin/deleteMember", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ deleteMemberId: memberId })
        })
            .then(res => res.json())
            .then(res => {
                if (res) {
                    alert("삭제되었습니다.");

                    // 🔥 부모 페이지 새로고침 + 팝업 닫기
                    if (window.opener){
                        window.opener.location.reload();
                        window.close();
                    } else // 일반 페이지면 목록 페이지로 이동
                        window.location.href = "/admin/personalDataPage";
                } else {
                    alert("삭제에 실패했거나 권한이 없습니다.");
                }
            })
            .catch(() => alert("서버 오류가 발생했습니다."));
    });



    // ====== 여기서부터 "저장 클릭 시 데이터 수집만" 구현 ======

    const strOrNull = (v) => {
        const s = String(v ?? '').trim();
        return s === '' ? null : s;   // 빈 문자열 → null (DB 저장용)
    };
    const intOrNull = (v) => {
        const s = String(v ?? '').trim();
        if (s === '') return null;
        const nNum = Number(s);
        return Number.isFinite(nNum) ? nNum : null;
    };

    const collectUpdatePayload = () => {
        const fd = new FormData(form);

        // checkbox는 FormData만으로는 신뢰하기 어려우므로 DOM에서 직접 읽음
        const cellLeaderStatusChecked = !!leaderCheckbox?.checked;

        // 현재 선택된 option의 id가 cellKey
        const selectedOption = cellNameSelect?.options[cellNameSelect.selectedIndex];
        const cellKeyValue = selectedOption ? selectedOption.id : null;

        const payload = {
            id: intOrNull(fd.get('id')),
            memberName: strOrNull(fd.get('memberName')),
            memberBirth: strOrNull(fd.get('memberBirth')),
            memberPhoneNumber: strOrNull(fd.get('memberPhoneNumber')),
            memberAddress: strOrNull(fd.get('memberAddress')),
            baptismStatus: strOrNull(fd.get('baptismStatus')),

            // 예배/셀 빈도는 이 페이지에서 편집하지 않으므로 제외(나중에 연동)
            cellLeaderStatus: cellLeaderStatusChecked,

            nurtureYear: ((y) => y == null ? null : String(y))(intOrNull(fd.get('nurtureYear'))),           // 1900~2999
            nurtureSemester: strOrNull(fd.get('nurtureSemester')),   // '상반기' | '하반기' | null

            growthYear: ((y) => y == null ? null : String(y))(intOrNull(fd.get('growthYear'))),
            growthSemester: strOrNull(fd.get('growthSemester')),

            cellKey: intOrNull(cellKeyValue),
            cellName: strOrNull(fd.get('cellName')),

            memberMemo: strOrNull(fd.get('memberMemo')),
        };

        return payload;
    };

    form.addEventListener('submit', (e) => {
        // 저장 버튼은 수정 모드에서만 노출되므로, 안전하게 한 번 더 확인
        if (editing) {
            e.preventDefault();
            const payload = collectUpdatePayload();

            fetch("/admin/updateMember", {
                method:"POST",
                headers:{ "Content-Type":"application/json" },
                body: JSON.stringify({
                    id: payload.id,
                    memberName: payload.memberName,
                    memberBirth: payload.memberBirth,
                    memberPhoneNumber: payload.memberPhoneNumber,
                    memberAddress: payload.memberAddress,
                    baptismStatus: payload.baptismStatus,
                    cellLeaderStatus: payload.cellLeaderStatus,
                    nurtureYear: payload.nurtureYear,
                    nurtureSemester: payload.nurtureSemester,
                    growthYear: payload.growthYear,
                    growthSemester: payload.growthSemester,
                    cellKey: payload.cellKey,
                    cellName: payload.cellName,
                    memberMemo: payload.memberMemo
                })
            })
                .then((res) => {
                    return res.json();
                })
                .then((res) => {
                    if(res) {
                        alert("정보 수정이 완료 되었습니다.");
                        location.reload(); // ✅ 서버에서 DB 최신값으로 다시 렌더링
                    }
                    else
                        alert("수정을 실패했습니다.");
                })
                .catch(() => {
                    alert("서버 요청 중 오류가 발생했습니다.");
                });
        }
    });

    // ====== 리더 옆 인라인 셀 이름 입력 제어 ======
    const cellNameInlineInput = document.getElementById('cellNameInlineInput');
    const cellNameHidden      = document.getElementById('cellNameHidden');

    // 기존 요소 재사용(이미 선언됐으면 그걸 씀)
    const _cellNameText   = document.getElementById('cellNameText') || null;
    const _cellNameSelect = document.getElementById('cellNameSelect') || null;

    // 인라인 편집 모드 토글
    const toggleInlineEditor = () => {
        if (!cellNameInlineInput || !cellNameHidden) return;

        const isLeader = !!leaderCheckbox?.checked;

        // 1️⃣ 읽기 모드
        if (!editing) {
            // 인라인 입력 숨기기
            cellNameInlineInput.classList.add('hidden');
            cellNameInlineInput.disabled = true;
            cellNameInlineInput.readOnly = true;
            cellNameHidden.disabled = true;
            cellNameHidden.value = '';

            // 읽기 모드: 텍스트만 보이기
            if (_cellNameText) _cellNameText.classList.remove('hidden');
            if (_cellNameSelect) _cellNameSelect.classList.add('hidden');
            return;
        }

        // 2️⃣ 수정 모드
        const canInline = isLeader && editing;   // 리더일 때 인라인 입력 활성
        const canSelect = editing && !isLeader;  // 비리더일 때 셀렉트 활성

        // 인라인 입력 표시/활성
        cellNameInlineInput.classList.toggle('hidden', !canInline);
        cellNameInlineInput.disabled = !canInline;
        cellNameInlineInput.readOnly = !canInline;

        // hidden input은 인라인 활성 시에만 전송
        cellNameHidden.disabled = !canInline;

        if (canInline) {
            // 인풋에 이미 뭔가 써놨으면 그걸 우선, 아니면 텍스트/셀렉트 값
            const current = cellNameInlineInput.value.trim() || '';
            cellNameInlineInput.value = current;
            cellNameHidden.value      = current;
        } else {
            cellNameHidden.value = '';
        }

        // 셀 정보 섹션 표시 제어
        if (_cellNameText) {
            // 수정 모드에서는 항상 숨김 (편집은 셀렉트나 인라인으로 함)
            _cellNameText.classList.add('hidden');
        }
        if (_cellNameSelect) {
            // 리더면 숨김, 비리더면 보이기 + 활성화
            _cellNameSelect.classList.toggle('hidden', isLeader);
            _cellNameSelect.disabled = !canSelect;
        }
    };

    // 인풋 변경 시 hidden 값 동기화
    cellNameInlineInput?.addEventListener('input', () => {
        if (!cellNameHidden) return;
        cellNameHidden.value = cellNameInlineInput.value.trim();
    });

    // 리더 체크 변경 시 반영
    let lastLeaderChecked = leaderCheckbox?.checked ?? false;

    leaderCheckbox?.addEventListener('change', () => {
        const wasLeader    = lastLeaderChecked;
        const isLeaderNow  = !!leaderCheckbox.checked;
        lastLeaderChecked  = isLeaderNow;

        // 우선 UI 토글
        toggleInlineEditor();

        // ⭐ 리더 → 비리더로 바꾼 "순간"에만 셀 정보 '없음'으로 초기화
        if (editing && wasLeader && !isLeaderNow) {
            // select 에서 value="" 옵션 선택 (없음)
            if (_cellNameSelect) _cellNameSelect.value = "";
        }
    });

    // === 편집 상태(.editing) 클래스 변화를 관찰해서 자동 반영 ===
    const editStateObserver = new MutationObserver(() => {
        toggleInlineEditor();
    });
    editStateObserver.observe(document.body, { attributes: true, attributeFilter: ['class'] });

    // 초기 1회 반영
    toggleInlineEditor();

    // 제출 시 안전하게 최종 동기화
    form.addEventListener('submit', (e) => {
        if (!editing) return; // 읽기 모드면 원래 흐름 유지
        if (leaderCheckbox?.checked && cellNameInlineInput && cellNameHidden) {
            cellNameHidden.value = cellNameInlineInput.value.trim();
        }
    });

})();
