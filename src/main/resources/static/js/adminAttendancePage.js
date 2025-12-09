(function () {
    // ===== 공통 DOM 요소 =====
    const yearLabel   = document.getElementById("yearLabel");
    const monthLabel  = document.getElementById("monthLabel");
    const prevBtn     = document.getElementById("prevMonth");
    const nextBtn     = document.getElementById("nextMonth");
    const headerRow1  = document.getElementById("headerRow1");
    const headerRow2  = document.getElementById("headerRow2");
    const tbody       = document.getElementById("attendanceBody");
    const saveBtn     = document.getElementById("saveAttendanceBtn");
    const attendanceTitleText = document.getElementById("attendanceTitleText");

    // 셀 선택 관련
    const cellCards = document.querySelectorAll(".cell-card");

    // 달 선택 드롭다운
    const dateDropdownToggle = document.getElementById("dateDropdownToggle");
    const dateDropdownPanel  = document.getElementById("dateDropdownPanel");

    // 통계 관련
    const statsStartInput = document.getElementById("statsStartDate");
    const statsEndInput   = document.getElementById("statsEndDate");
    const loadStatsBtn    = document.getElementById("loadStatsBtn");
    const statsBody       = document.getElementById("statsBody");

    if (!yearLabel || !monthLabel || !headerRow1 || !headerRow2 || !tbody) return;

    // ===== 상태값 =====
    let selectedCellKey   = null;
    let selectedCellName  = null;

    const today = new Date();
    const baseYear = today.getFullYear();

    // 선택된 달(1일 기준)
    let currentMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    currentMonth.setHours(0, 0, 0, 0);
    let selectedMonthStr = null;

    // 현재 달에 속하는 주일 리스트
    let currentMonthSundays = [];

    // 날짜별 요약 박스 요소 저장용 (dateStr -> {absentEl, worshipEl, cellEl})
    const summaryMap = new Map();

    // 변경 내역 (업서트용)
    // key: memberId|YYYY-MM-DD
    const editedMap = new Map();

    // 초기에는 어떤 셀도 선택되지 않도록 active 제거
    cellCards.forEach((btn) => btn.classList.remove("active"));
    if (attendanceTitleText)
        attendanceTitleText.textContent = "출석 현황";

    // ===== 유틸 =====
    const pad2 = (n) => (n < 10 ? "0" + n : String(n));

    function toDateStr(date) {
        const y = date.getFullYear();
        const m = pad2(date.getMonth() + 1);
        const d = pad2(date.getDate());
        return `${y}-${m}-${d}`;
    }

    function toMonthStr(date) {
        const y = date.getFullYear();
        const m = pad2(date.getMonth() + 1);
        return `${y}-${m}`;
    }

    function formatMonthKorean(date) {
        return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
    }

    function formatSundayShort(date) {
        return `${date.getMonth() + 1}월 ${date.getDate()}일(주일)`;
    }

    function updateYearMonthLabel() {
        yearLabel.textContent  = String(currentMonth.getFullYear());
        monthLabel.textContent = String(currentMonth.getMonth() + 1);
    }

    function generateMonthList(baseYear) {
        const list = [];
        const start = new Date(baseYear - 1, 0, 1); // 작년 1월
        const end   = new Date(baseYear, 11, 1);    // 올해 12월

        let d = new Date(start.getFullYear(), start.getMonth(), 1);
        d.setHours(0, 0, 0, 0);

        while (d <= end) {
            list.push(new Date(d.getTime()));
            d.setMonth(d.getMonth() + 1);
        }
        return list;
    }

    const monthList = generateMonthList(baseYear);

    function getSundaysInMonth(date) {
        const year  = date.getFullYear();
        const month = date.getMonth();

        const firstDay = new Date(year, month, 1);
        const lastDay  = new Date(year, month + 1, 0);

        let d = new Date(firstDay.getFullYear(), firstDay.getMonth(), firstDay.getDate());
        d.setHours(0, 0, 0, 0);

        // 첫 주일
        const dow = d.getDay();
        if (dow !== 0) {
            d.setDate(d.getDate() + (7 - dow));
        }

        const result = [];
        while (d <= lastDay) {
            result.push(new Date(d.getTime()));
            d.setDate(d.getDate() + 7);
        }
        return result;
    }

    // ===== 월 선택 드롭다운 =====
    let isDateDropdownOpen = false;

    function closeDateDropdown() {
        if (!dateDropdownPanel) return;
        dateDropdownPanel.classList.add("hidden");
        if (dateDropdownToggle) {
            dateDropdownToggle.setAttribute("aria-expanded", "false");
        }
        isDateDropdownOpen = false;
    }

    function updateSelectedMonthInDropdown() {
        if (!dateDropdownPanel) return;

        selectedMonthStr = toMonthStr(currentMonth);
        const options = dateDropdownPanel.querySelectorAll(".date-option");

        options.forEach((btn) => {
            const isSelected = btn.dataset.month === selectedMonthStr;
            btn.classList.toggle("selected", isSelected);
            btn.setAttribute("aria-selected", isSelected ? "true" : "false");
        });
    }

    function buildMonthDropdown() {
        if (!dateDropdownPanel) return;

        dateDropdownPanel.innerHTML = "";
        const currentStr = toMonthStr(currentMonth);

        monthList.forEach((d) => {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "date-option";

            const monthStr = toMonthStr(d);
            btn.dataset.month = monthStr;
            btn.textContent   = formatMonthKorean(d);

            if (monthStr === currentStr) {
                btn.classList.add("selected");
                btn.setAttribute("aria-selected", "true");
            }

            dateDropdownPanel.appendChild(btn);
        });
    }

    function openDateDropdown() {
        if (!dateDropdownPanel) return;
        updateSelectedMonthInDropdown();
        dateDropdownPanel.classList.remove("hidden");
        if (dateDropdownToggle) {
            dateDropdownToggle.setAttribute("aria-expanded", "true");
        }
        isDateDropdownOpen = true;

        const selectedEl = dateDropdownPanel.querySelector(".date-option.selected");
        if (selectedEl) {
            const panel = dateDropdownPanel;
            const targetTop =
                selectedEl.offsetTop - (panel.clientHeight / 2 - selectedEl.offsetHeight / 2);
            panel.scrollTop = Math.max(targetTop, 0);
        }
    }

    dateDropdownToggle?.addEventListener("click", (e) => {
        e.stopPropagation();
        if (isDateDropdownOpen) {
            closeDateDropdown();
        } else {
            openDateDropdown();
        }
    });

    dateDropdownPanel?.addEventListener("click", (e) => {
        const target = e.target.closest(".date-option");
        if (!target) return;

        const monthStr = target.dataset.month;
        if (!monthStr) return;

        const [yStr, mStr] = monthStr.split("-");
        const y = Number(yStr);
        const m = Number(mStr);

        const selected = new Date(y, m - 1, 1);
        selected.setHours(0, 0, 0, 0);

        currentMonth = selected;
        editedMap.clear();

        if (selectedCellKey) {
            renderAttendanceTable();
        } else {
            clearAttendanceView();
        }
        updateSelectedMonthInDropdown();
        closeDateDropdown();
    });

    document.addEventListener("click", (e) => {
        if (!isDateDropdownOpen) return;
        if (!dateDropdownPanel || !dateDropdownToggle) return;

        const withinPanel  = dateDropdownPanel.contains(e.target);
        const withinButton = dateDropdownToggle.contains(e.target);
        if (!withinPanel && !withinButton) {
            closeDateDropdown();
        }
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            closeDateDropdown();
        }
    });

    // ===== 공통: 출석 화면 초기화(셀 미선택 / 변경 후용) ====================
    function clearAttendanceView() {
        // headerRow1: 이름 컬럼만 남기고 삭제
        while (headerRow1.children.length > 1)
            headerRow1.removeChild(headerRow1.lastElementChild);
        // headerRow2 전체 삭제
        while (headerRow2.firstChild)
            headerRow2.removeChild(headerRow2.firstChild);
        // 바디/요약/변경 내역 초기화
        tbody.innerHTML = "";
        summaryMap.clear();
        currentMonthSundays = [];
        editedMap.clear();
        // 통계 테이블도 비우기
        if (statsBody)
            statsBody.innerHTML = "";
    }

    // ===== 출석 변경 내역 관리 =============================================
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

    // ===== 요약 계산: headerRow1 안 작은 박스 업데이트 =======================
    function computeAndRenderSummary() {
        if (!currentMonthSundays || currentMonthSundays.length === 0)
            return;

        const rows = Array.from(tbody.querySelectorAll("tr"));

        currentMonthSundays.forEach((d) => {
            const dateStr = toDateStr(d);

            let worshipCnt = 0;
            let cellCnt    = 0;
            let absentCnt  = 0;

            rows.forEach((tr) => {
                const memberId = tr.dataset.memberId;
                if (!memberId) return;

                const chkW = tr.querySelector(`.attend-checkbox.worship[data-date="${dateStr}"]`);
                const chkC = tr.querySelector(`.attend-checkbox.cell[data-date="${dateStr}"]`);

                const w = !!(chkW && chkW.checked);
                const c = !!(chkC && chkC.checked);

                if (c)
                    cellCnt++;
                else if (w)
                    worshipCnt++;
                else
                    absentCnt++;
            });

            const entry = summaryMap.get(dateStr);
            if (entry) {
                entry.absentEl.textContent  = String(absentCnt);
                entry.worshipEl.textContent = String(worshipCnt);
                entry.cellEl.textContent    = String(cellCnt);
            }
        });
    }

    // ===== 서버: 하루 기준 출석 조회 ======================================
    async function loadAttendanceForDate(dateStr) {
        try {
            const rows = Array.from(tbody.querySelectorAll("tr"));
            const memberIdList = rows
                .map(tr => tr.dataset.memberId)
                .filter(id => id && id.trim() !== "")
                .map(id => Number(id))
                .filter(id => Number.isInteger(id) && id > 0);

            if (memberIdList.length === 0) {
                return;
            }

            const res = await fetch("/admin/loadAttendance", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    attendanceDate: dateStr,
                    attendingMemberIdList : memberIdList
                })
            });

            if (!res.ok) {
                console.error("출석 데이터 조회 실패(관리자) :", res.status);
                return;
            }

            const data = await res.json();
            if (!data || typeof data !== "object") {
                return;
            }

            Object.entries(data).forEach(([memberIdStr, dto]) => {
                const tr = tbody.querySelector(`tr[data-member-id="${memberIdStr}"]`);
                if (!tr) return;

                const chkW = tr.querySelector(`.attend-checkbox.worship[data-date="${dateStr}"]`);
                const chkC = tr.querySelector(`.attend-checkbox.cell[data-date="${dateStr}"]`);
                const memoInput = tr.querySelector(`.memo-input[data-date="${dateStr}"]`);

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
        } catch (err) {
            console.error("출석 데이터 로드 중 오류(관리자) :", err);
        }
    }

    // 해당 달에 속한 모든 주일 출석 조회 후 요약 반영
    async function loadAttendanceForMonth() {
        if (!currentMonthSundays || currentMonthSundays.length === 0) {
            computeAndRenderSummary();
            return;
        }

        const dateStrList = currentMonthSundays.map((d) => toDateStr(d));

        try {
            await Promise.all(dateStrList.map((ds) => loadAttendanceForDate(ds)));
        } finally {
            computeAndRenderSummary();
        }
    }

    // ===== 출석 저장 =======================================================
    async function saveAttendance() {
        if (editedMap.size === 0) {
            alert("변경된 출석 정보가 없습니다.");
            return;
        }

        const allItems = Array.from(editedMap.values());

        try {
            const res = await fetch("/admin/updateAttendance", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ items: allItems })
            });

            const ok = await res.json();
            if (ok) {
                alert("출석이 정상적으로 저장되었습니다.");
                editedMap.clear();
            } else {
                alert("출석 저장에 실패했습니다. 다시 시도해주세요.");
            }
        } catch (err) {
            console.error("출석 저장 실패(관리자) : ", err);
            alert("네트워크 오류로 출석 저장에 실패했습니다.");
        }
    }

    // ===== 출석 테이블 렌더링 (선택된 셀 + 선택된 달) =======================
    function renderAttendanceTable() {
        if (!selectedCellKey) {
            clearAttendanceView();
            return;
        }

        updateYearMonthLabel();

        // 헤더 초기화
        while (headerRow1.children.length > 1)
            headerRow1.removeChild(headerRow1.lastElementChild);
        while (headerRow2.firstChild)
            headerRow2.removeChild(headerRow2.firstChild);

        summaryMap.clear();

        currentMonthSundays = getSundaysInMonth(currentMonth);
        if (currentMonthSundays.length === 0) {
            computeAndRenderSummary();
            return;
        }

        // 1행 헤더: 날짜 + 작은 요약 박스
        currentMonthSundays.forEach((d) => {
            const th = document.createElement("th");
            th.colSpan = 3;

            const box = document.createElement("div");
            box.className = "header-summary-box";

            const dateEl = document.createElement("div");
            dateEl.className = "header-summary-date";
            dateEl.textContent = formatSundayShort(d);

            const row = document.createElement("div");
            row.className = "header-summary-row";

            const makeChip = (label, extraClass) => {
                const chip = document.createElement("div");
                chip.className = `header-summary-chip ${extraClass}`;
                const labelEl = document.createElement("span");
                labelEl.className = "header-summary-label";
                labelEl.textContent = label;
                const valueEl = document.createElement("span");
                valueEl.className = "header-summary-value";
                valueEl.textContent = "0";
                chip.appendChild(labelEl);
                chip.appendChild(valueEl);
                return { chip, valueEl };
            };

            const absent  = makeChip("결석",  "absent");
            const worship = makeChip("예배",  "worship");
            const cell    = makeChip("셀모임", "cell");

            row.appendChild(absent.chip);
            row.appendChild(worship.chip);
            row.appendChild(cell.chip);

            box.appendChild(dateEl);
            box.appendChild(row);
            th.appendChild(box);
            headerRow1.appendChild(th);

            const dateStr = toDateStr(d);
            summaryMap.set(dateStr, {
                absentEl:  absent.valueEl,
                worshipEl: worship.valueEl,
                cellEl:    cell.valueEl
            });
        });

        // 2행 헤더: 예배 / 셀 / 메모 * 주일 수
        currentMonthSundays.forEach(() => {
            const thW = document.createElement("th");
            thW.textContent = "예배";
            const thC = document.createElement("th");
            thC.textContent = "셀";
            const thM = document.createElement("th");
            thM.textContent = "메모";
            headerRow2.appendChild(thW);
            headerRow2.appendChild(thC);
            headerRow2.appendChild(thM);
        });

        // 바디: 기존 tr(이름만 있는 상태)에 예배/셀/메모 컬럼 추가
        const rows = tbody.querySelectorAll("tr");
        rows.forEach((tr) => {
            while (tr.children.length > 1)
                tr.removeChild(tr.lastElementChild);

            const memberId = tr.dataset.memberId || "";

            currentMonthSundays.forEach((d) => {
                const dateStr = toDateStr(d);

                // 예배
                const tdW = document.createElement("td");
                tdW.className = "attend-cell";
                const chkW = document.createElement("input");
                chkW.type = "checkbox";
                chkW.className = "attend-checkbox worship";
                chkW.dataset.memberId = memberId;
                chkW.dataset.date     = dateStr;
                chkW.dataset.type     = "worship";
                tdW.appendChild(chkW);

                // 셀
                const tdC = document.createElement("td");
                tdC.className = "attend-cell";
                const chkC = document.createElement("input");
                chkC.type = "checkbox";
                chkC.className = "attend-checkbox cell";
                chkC.dataset.memberId = memberId;
                chkC.dataset.date     = dateStr;
                chkC.dataset.type     = "cell";
                tdC.appendChild(chkC);

                // 메모
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
        });

        // 서버에서 실제 출석 데이터 로딩 후 요약 반영
        loadAttendanceForMonth();
    }

    // ===== 셀 선택 시: 셀원 목록 다시 불러오기 ============================
    async function loadCellMembersAndRender(cellKey, cellName) {
        if (!cellKey) return;

        try {
            const res = await fetch(`/admin/cellMembers?cellKey=${cellKey}`, {
                method: "GET"
            });
            if (!res.ok) {
                alert("셀원 목록을 불러오지 못했습니다.");
                return;
            }

            const members = await res.json(); // [{id, memberName}, ...]

            tbody.innerHTML = "";
            members.forEach((m) => {
                const tr = document.createElement("tr");
                tr.dataset.memberId = m.id;
                tr.dataset.memberName = m.memberName;

                const tdName = document.createElement("td");
                tdName.className = "member-name";
                tdName.textContent = m.memberName;

                tr.appendChild(tdName);
                tbody.appendChild(tr);
            });

            editedMap.clear();
            renderAttendanceTable();

            if (statsBody) {
                statsBody.innerHTML = "";
            }
        } catch (err) {
            console.error("셀원 목록 로드 실패 :", err);
            alert("셀원 목록을 불러오는 중 오류가 발생했습니다.");
        }
    }

    cellCards.forEach((btn) => {
        btn.addEventListener("click", () => {
            const cellKey  = Number(btn.dataset.cellKey);
            const cellName = btn.dataset.cellName || "";

            if (!cellKey || cellKey === selectedCellKey) return;

            cellCards.forEach((b) => b.classList.remove("active"));
            btn.classList.add("active");

            selectedCellKey  = cellKey;
            selectedCellName = cellName;

            if (attendanceTitleText) {
                attendanceTitleText.textContent = selectedCellName
                    ? `출석 현황 (${selectedCellName})`
                    : "출석 현황";
            }

            loadCellMembersAndRender(selectedCellKey, selectedCellName);
        });
    });

    // ===== 달 네비게이션 ================================================
    prevBtn?.addEventListener("click", () => {
        currentMonth.setMonth(currentMonth.getMonth() - 1);
        currentMonth.setDate(1);
        editedMap.clear();

        if (selectedCellKey) {
            renderAttendanceTable();
        } else {
            updateYearMonthLabel();
            clearAttendanceView();
        }
        updateSelectedMonthInDropdown();
    });

    nextBtn?.addEventListener("click", () => {
        currentMonth.setMonth(currentMonth.getMonth() + 1);
        currentMonth.setDate(1);
        editedMap.clear();

        if (selectedCellKey) {
            renderAttendanceTable();
        } else {
            updateYearMonthLabel();
            clearAttendanceView();
        }
        updateSelectedMonthInDropdown();
    });

    // ===== 출석 저장 버튼 ================================================
    saveBtn?.addEventListener("click", saveAttendance);

    // ===== 개별 통계 조회 ================================================
    async function loadStats() {
        if (!selectedCellKey) {
            alert("먼저 셀을 선택해주세요.");
            return;
        }
        const start = statsStartInput?.value;
        const end   = statsEndInput?.value;

        if (!start || !end) {
            alert("통계 기간(시작일/종료일)을 모두 선택해주세요.");
            return;
        }
        if (start > end) {
            alert("시작일이 종료일보다 늦을 수 없습니다.");
            return;
        }

        try {
            const res = await fetch("/admin/loadAttendanceStats", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    cellKey: selectedCellKey,
                    startDate: start,
                    endDate: end
                })
            });

            if (!res.ok) {
                alert("통계 데이터를 불러오지 못했습니다.");
                return;
            }

            const list = await res.json(); // [{memberId, memberName, absentCount, worshipCount, cellCount}, ...]

            if (!statsBody) return;
            statsBody.innerHTML = "";

            if (!Array.isArray(list) || list.length === 0) {
                const tr = document.createElement("tr");
                const td = document.createElement("td");
                td.colSpan = 4;
                td.textContent = "해당 기간에 출석 데이터가 없습니다.";
                td.style.textAlign = "center";
                tr.appendChild(td);
                statsBody.appendChild(tr);
                return;
            }

            list.forEach((row) => {
                const tr = document.createElement("tr");

                const tdName = document.createElement("td");
                tdName.textContent = row.memberName || "";
                tr.appendChild(tdName);

                const tdAbsent = document.createElement("td");
                tdAbsent.textContent = row.absentCount ?? 0;
                tr.appendChild(tdAbsent);

                const tdWorship = document.createElement("td");
                tdWorship.textContent = row.worshipCount ?? 0;
                tr.appendChild(tdWorship);

                const tdCell = document.createElement("td");
                tdCell.textContent = row.cellCount ?? 0;
                tr.appendChild(tdCell);

                statsBody.appendChild(tr);
            });
        } catch (err) {
            console.error("통계 데이터 로드 실패 :", err);
            alert("통계 데이터를 불러오는 중 오류가 발생했습니다.");
        }
    }

    loadStatsBtn?.addEventListener("click", loadStats);

    // ===== 통계 날짜 입력: 키보드 입력/붙여넣기 방지 ======================
    function disableTypingOnDateInput(input) {
        if (!input) return;
        input.addEventListener("keydown", (e) => e.preventDefault());
        input.addEventListener("paste", (e) => e.preventDefault());
    }
    disableTypingOnDateInput(statsStartInput);
    disableTypingOnDateInput(statsEndInput);

    // ===== 초기 설정 ======================================================
    updateYearMonthLabel();
    buildMonthDropdown();
    updateSelectedMonthInDropdown();
    clearAttendanceView();   // 처음 화면은 비어 있는 상태
})();
