(function () {
    // 상단 네비게이터 / 헤더 / 바디
    const yearLabel   = document.getElementById("yearLabel");
    const monthLabel  = document.getElementById("monthLabel");
    const dayLabel    = document.getElementById("dayLabel");
    const prevBtn     = document.getElementById("prevMonth");
    const nextBtn     = document.getElementById("nextMonth");
    const headerRow1  = document.getElementById("headerRow1");
    const headerRow2  = document.getElementById("headerRow2");
    const tbody       = document.getElementById("attendanceBody");
    const saveBtn     = document.getElementById("saveAttendanceBtn");

    // 요약 카드 요소
    const worshipCountEl = document.getElementById("worshipCount");
    const cellCountEl    = document.getElementById("cellCount");
    const absentCountEl  = document.getElementById("absentCount");

    // 필수 요소가 없으면 종료
    if (!yearLabel || !monthLabel || !headerRow1 || !headerRow2 || !tbody) return;

    // ===== 날짜 유틸 =====
    const pad2 = (n) => (n < 10 ? "0" + n : String(n));

    // Date -> "YYYY-MM-DD"
    function toDateStr(date) {
        const y = date.getFullYear();
        const m = pad2(date.getMonth() + 1);
        const d = pad2(date.getDate());
        return `${y}-${m}-${d}`;
    }

    // ===== 현재 "주일" 상태 =====
    const today = new Date();
    let currentSunday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    currentSunday.setHours(0, 0, 0, 0);

    const dow = currentSunday.getDay();   // 0(일) ~ 6(토)
    currentSunday.setDate(currentSunday.getDate() - dow); // 해당 주의 일요일

    // 상단 라벨(년도/월/일) 갱신
    function updateYearMonthLabel() {
        yearLabel.textContent  = String(currentSunday.getFullYear());
        monthLabel.textContent = String(currentSunday.getMonth() + 1);
        if (dayLabel)
            dayLabel.textContent = String(currentSunday.getDate());
    }

    // ===== 변경된 셀만 추적하는 Map =====
    // key: `${memberId}|${dateStr}`
    // value: { memberId, attendanceDate, worshipStatus, cellStatus, attendanceMemo }
    const editedMap = new Map();

    function markEdited(memberId, dateStr, updater) {
        if (!memberId || !dateStr) return;

        const key = memberId + "|" + dateStr;
        let item = editedMap.get(key);

        if (!item) {
            item = {
                memberId: memberId,
                attendanceDate: dateStr,
                worshipStatus: false,
                cellStatus: false,
                attendanceMemo: null
            };
        }

        updater(item);
        editedMap.set(key, item);
    }

    // ===== 요약 카드 계산 (셀모임 / 예배 / 결석) =====
    function computeAndRenderSummary() {
        if (!worshipCountEl || !cellCountEl || !absentCountEl) return;

        let worshipCnt = 0;
        let cellCnt    = 0;
        let absentCnt  = 0;

        const rows = tbody.querySelectorAll("tr");
        rows.forEach((tr) => {
            const worshipChk = tr.querySelector("input.attend-checkbox.worship");
            const cellChk    = tr.querySelector("input.attend-checkbox.cell");

            const w = !!(worshipChk && worshipChk.checked);
            const c = !!(cellChk && cellChk.checked);

            // 규칙
            // 1) 둘 다 체크(o,o)   → 셀모임만 +1
            // 2) 예배만 체크(o,x)  → 예배만 +1
            // 3) 셀만 체크(x,o)    → 셀모임만 +1
            // 4) 둘 다 미체크      → 결석 +1
            if (c)
                cellCnt++;
            else if (w)
                worshipCnt++;
            else
                absentCnt++;
        });

        worshipCountEl.textContent = String(worshipCnt);
        cellCountEl.textContent    = String(cellCnt);
        absentCountEl.textContent  = String(absentCnt);
    }

    // ===== 서버에서 해당 날짜 출석 데이터 불러오기 =====
    async function loadAttendance(dateStr) {
        try {
            // 현재 테이블에 표시된 멤버들의 id 모으기
            const rows = Array.from(tbody.querySelectorAll("tr"));
            const attendingMemberIdList = rows
                .map(tr => tr.dataset.memberId)
                .filter(id => id !== undefined && id !== null && id.trim() !== "")
                .map(id => Number(id))
                .filter(id => Number.isInteger(id) && id > 0);

            const res = await fetch("/user/loadAttendance", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    attendanceDate: dateStr,
                    attendingMemberIdList : attendingMemberIdList
                })
            });

            if (!res.ok) {
                console.error("출석 데이터 조회 실패 :", res.status);
                computeAndRenderSummary();
                return;
            }

            // 서버에서 Map<Long, List<AttendanceItemDto>> 형태로 return이 들어옴.
            const data = await res.json();
            if (!data || typeof data !== "object") {
                computeAndRenderSummary();
                return;
            }

            // 각 memberId에 대해 값 세팅
            Object.entries(data).forEach(([memberIdStr, dto]) => {
                const tr = tbody.querySelector(`tr[data-member-id="${memberIdStr}"]`);
                if (!tr) return;

                const chkW = tr.querySelector(".attend-checkbox.worship");
                const chkC = tr.querySelector(".attend-checkbox.cell");
                const memoInput = tr.querySelector(".memo-input");

                // dto가 null이면 (출석 기록 없음)
                if (!dto) {
                    if (chkW) chkW.checked = false;
                    if (chkC) chkC.checked = false;
                    if (memoInput) memoInput.value = "";
                    return;
                }

                if (chkW) chkW.checked = !!dto.worshipStatus;
                if (chkC) chkC.checked = !!dto.cellStatus;
                if (memoInput) memoInput.value = dto.attendanceMemo || "";
            });

            // 서버 데이터 기준으로 요약 박스 갱신
            computeAndRenderSummary();
        } catch (err) {
            console.error("출석 데이터 로드 중 오류 :", err);
            computeAndRenderSummary();
        }
    }

    // ===== '출석 저장' 클릭 시: 변경 내역만 POST =====
    async function saveAttendance() {
        if (editedMap.size === 0) {
            alert("변경된 출석 정보가 없습니다.");
            return;
        }

        const allItems = Array.from(editedMap.values());

        try {
            const res = await fetch("/user/updateAttendance", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ items: allItems })
            });

            const ok = await res.json();

            if (ok) {
                alert("출석이 정상적으로 저장되었습니다.");
                editedMap.clear();  // 저장 성공 → 변경 내역 비우기
            } else {
                alert("출석 저장에 실패했습니다. 다시 시도해주세요.");
            }
        } catch (err) {
            console.error("출석 저장 실패 : ", err);
            alert("네트워크 오류로 출석 저장에 실패했습니다.");
        }
    }

    // ===== 테이블 렌더링 (currentSunday 기준, 한 주일만 표시) =====
    function renderAttendanceTable() {
        updateYearMonthLabel();

        // 헤더 초기화 (이름 th 제외)
        while (headerRow1.children.length > 1)
            headerRow1.removeChild(headerRow1.lastElementChild);

        while (headerRow2.firstChild)
            headerRow2.removeChild(headerRow2.firstChild);

        // 현재 주일 날짜 문자열
        const dateStr = toDateStr(currentSunday);

        // "예배 / 셀 / 메모" 표시
        const thWorship = document.createElement("th");
        thWorship.textContent = "예배";

        const thCell = document.createElement("th");
        thCell.textContent = "셀";

        const thMemo = document.createElement("th");
        thMemo.textContent = "메모";

        headerRow2.appendChild(thWorship);
        headerRow2.appendChild(thCell);
        headerRow2.appendChild(thMemo);

        // 바디(각 멤버 행) 생성
        const rows = tbody.querySelectorAll("tr");
        rows.forEach((tr) => {
            // 이름 셀 제외하고 전부 삭제
            while (tr.children.length > 1)
                tr.removeChild(tr.lastElementChild);

            const memberId = tr.dataset.memberId || "";

            // 예배 체크박스
            const tdW = document.createElement("td");
            tdW.className = "attend-cell";
            const chkW = document.createElement("input");
            chkW.type = "checkbox";
            chkW.className = "attend-checkbox worship";
            chkW.dataset.memberId = memberId;
            chkW.dataset.date     = dateStr;
            chkW.dataset.type     = "worship";
            tdW.appendChild(chkW);

            // 셀 체크박스
            const tdC = document.createElement("td");
            tdC.className = "attend-cell";
            const chkC = document.createElement("input");
            chkC.type = "checkbox";
            chkC.className = "attend-checkbox cell";
            chkC.dataset.memberId = memberId;
            chkC.dataset.date     = dateStr;
            chkC.dataset.type     = "cell";
            tdC.appendChild(chkC);

            // 메모 입력
            const tdM = document.createElement("td");
            tdM.className = "memo-cell";
            const memoInput = document.createElement("input");
            memoInput.type = "text";
            memoInput.className = "memo-input";
            memoInput.placeholder = "메모";
            memoInput.dataset.memberId = memberId;
            memoInput.dataset.date     = dateStr;
            tdM.appendChild(memoInput);

            tr.appendChild(tdW);
            tr.appendChild(tdC);
            tr.appendChild(tdM);

            // 이벤트 바인딩
            chkW.addEventListener("change", () => {
                markEdited(memberId, dateStr, (item) => {
                    item.worshipStatus = chkW.checked;
                });
                computeAndRenderSummary();
            });

            chkC.addEventListener("change", () => {
                markEdited(memberId, dateStr, (item) => {
                    item.cellStatus = chkC.checked;
                });
                computeAndRenderSummary();
            });

            memoInput.addEventListener("blur", () => {
                if (!memberId) return;
                markEdited(memberId, dateStr, (item) => {
                    item.attendanceMemo = memoInput.value ?? null;
                });
            });
        });

        // 서버에서 해당 날짜 출석 데이터 불러오기
        loadAttendance(dateStr);
    }

    // ===== 주(Week) 네비게이션 =====
    prevBtn?.addEventListener("click", () => {
        currentSunday.setDate(currentSunday.getDate() - 7); // 이전 주
        editedMap.clear(); // 주 바뀔 때 변경 목록 초기화
        renderAttendanceTable();
    });

    nextBtn?.addEventListener("click", () => {
        currentSunday.setDate(currentSunday.getDate() + 7); // 다음 주
        editedMap.clear(); // 주 바뀔 때 변경 목록 초기화
        renderAttendanceTable();
    });

    // ===== 저장 버튼 클릭 이벤트 =====
    saveBtn?.addEventListener("click", saveAttendance);

    // 초기 렌더링
    renderAttendanceTable();
})();
