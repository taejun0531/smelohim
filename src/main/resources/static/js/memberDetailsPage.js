// /js/memberDetailsPage.js
// 읽기 ↔ 수정 모드 토글, 세례/학기 선택 동기화, 셀 이름 셀렉트 전환, 연도 입력 보정

(function () {
    const form = document.getElementById('memberForm');
    if (!form) return;

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
        alert('예배 빈도 갱신 요청은 나중에 연결됩니다.');
    });
    refreshCellBtn?.addEventListener('click', () => {
        alert('셀모임 빈도 갱신 요청은 나중에 연결됩니다.');
    });

    // --- 초기 선택값 동기화 ---
    // 세례 셀렉트: 읽기 텍스트(DB 값)와 동일한 옵션을 선택
    if (baptismSelect && baptismText) {
        const dbVal = (baptismText.textContent || '').trim();
        Array.from(baptismSelect.options).forEach(opt => {
            opt.selected = (opt.value === dbVal);
        });
    }

    // 학기 셀렉트: 읽기 텍스트(DB 값: '상반기'/'하반기')와 동일한 옵션 선택
    const syncSemesterSelect = (selectEl, textEl) => {
        if (!selectEl || !textEl) return;
        const dbVal = (textEl.textContent || '').trim();
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
        const txt = (textEl.textContent || '').trim();
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

    // 제출 전 추가 가공 없음
    // - 년도: number input 그대로 전송 (1900~2999)
    // - 학기: select 값(상반기/하반기) 그대로 전송
    form.addEventListener('submit', () => { /* no-op */ });
})();
