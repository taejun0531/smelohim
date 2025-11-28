// /js/userAttendancePage.js
// 월/년도 네비게이션 + 주일만 표시하는 출석 테이블 생성
// ✔ 체크/메모 변경 사항만 메모해 두었다가
// ✔ '출석 저장' 버튼을 누를 때 날짜별로 POST

(function () {
    const yearLabel   = document.getElementById("yearLabel");
    const monthLabel  = document.getElementById("monthLabel");
    const prevBtn     = document.getElementById("prevMonth");
    const nextBtn     = document.getElementById("nextMonth");
    const headerRow1  = document.getElementById("headerRow1");
    const headerRow2  = document.getElementById("headerRow2");
    const tbody       = document.getElementById("attendanceBody");
    const saveBtn     = document.getElementById("saveAttendanceBtn");

    if (!yearLabel || !monthLabel || !headerRow1 || !headerRow2 || !tbody) return;

    // ===== 날짜 유틸 =====
    const pad2 = (n) => (n < 10 ? "0" + n : String(n));

    // 주일만 구하기 (한 번 첫 주일 찾고 7일씩 더하기)
    function getSundays(year, monthZeroBased) {
        const result = [];
        const d = new Date(year, monthZeroBased, 1);

        while (d.getDay() !== 0)
            d.setDate(d.getDate() + 1);

        while (d.getMonth() === monthZeroBased) {
            result.push(d.getDate());
            d.setDate(d.getDate() + 7);
        }

        return result;
    }

    // ===== 현재 년/월 상태 =====
    const today = new Date();
    let currentYear  = today.getFullYear();
    let currentMonth = today.getMonth(); // 0~11

    function updateYearMonthLabel() {
        yearLabel.textContent  = String(currentYear);
        monthLabel.textContent = String(currentMonth + 1);
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

    // ===== '출석 저장' 클릭 시: 날짜별로 묶어서 POST =====
    async function saveAttendance() {
        if (editedMap.size === 0) {
            alert("변경된 출석 정보가 없습니다.");
            return;
        }

        const allItems = Array.from(editedMap.values());

        try {
            const res = await fetch("/user/attendance", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    items: allItems
                })
            });

            const ok = await res.json();

            if (ok) {
                alert("출석이 정상적으로 저장되었습니다.");
                editedMap.clear();  // 저장 성공 → 변경 내역 비우기
            } else
                alert("출석 저장에 실패했습니다. 다시 시도해주세요.");

        } catch (err) {
            console.error("출석 저장 실패 : ", err);
            alert("네트워크 오류로 출석 저장에 실패했습니다.");
        }
    }

    // ===== 테이블 렌더링 =====
    function renderAttendanceTable() {
        updateYearMonthLabel();

        const sundays = getSundays(currentYear, currentMonth);

        // 1) 헤더 초기화 (이름 th 제외)
        while (headerRow1.children.length > 1)
            headerRow1.removeChild(headerRow1.lastElementChild);

        while (headerRow2.firstChild)
            headerRow2.removeChild(headerRow2.firstChild);

        // 2) 헤더 생성 (날짜: colspan=3 => 예배/셀/메모)
        sundays.forEach((day) => {
            const thDate = document.createElement("th");
            thDate.colSpan = 3;
            thDate.textContent = `${day}일(주일)`;
            headerRow1.appendChild(thDate);

            const thWorship = document.createElement("th");
            thWorship.textContent = "예배";

            const thCell = document.createElement("th");
            thCell.textContent = "셀";

            const thMemo = document.createElement("th");
            thMemo.textContent = "메모";

            headerRow2.appendChild(thWorship);
            headerRow2.appendChild(thCell);
            headerRow2.appendChild(thMemo);
        });

        // 3) 바디(각 멤버 행)
        const rows = tbody.querySelectorAll("tr");
        rows.forEach((tr) => {
            // 이름 셀 제외하고 전부 지우기
            while (tr.children.length > 1)
                tr.removeChild(tr.lastElementChild);

            const memberId   = tr.dataset.memberId || "";

            sundays.forEach((day) => {
                const dateStr = `${currentYear}-${pad2(currentMonth + 1)}-${pad2(day)}`;

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

                chkW.addEventListener("change", () => {
                    markEdited(memberId, dateStr, (item) => {
                        item.worshipStatus = chkW.checked;
                    });
                });

                chkC.addEventListener("change", () => {
                    markEdited(memberId, dateStr, (item) => {
                        item.cellStatus = chkC.checked;
                    });
                });

                memoInput.addEventListener("blur", () => {
                    if (!memberId) return;
                    markEdited(memberId, dateStr, (item) => {
                        item.attendanceMemo = memoInput.value ?? null;
                    });
                });
            });
        });

        // ※ 나중에 서버에서 해당 월 출석 데이터를 JSON으로 내려주면,
        //    여기서 fetch로 받아와서 chkW/chkC/memoInput의 초기값을 세팅하고,
        //    editedMap은 "초기값 기준"으로 비워둔 상태에서 변경만 추적하면 됨.
    }

    // ===== 월 네비게이션 =====
    prevBtn?.addEventListener("click", () => {
        currentMonth -= 1;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear -= 1;
        }
        renderAttendanceTable();
        editedMap.clear(); // 월 바뀔 때 변경 목록 초기화
    });

    nextBtn?.addEventListener("click", () => {
        currentMonth += 1;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear += 1;
        }
        renderAttendanceTable();
        editedMap.clear(); // 월 바뀔 때 변경 목록 초기화
    });

    // ===== 저장 버튼 클릭 이벤트 =====
    saveBtn?.addEventListener("click", saveAttendance());

    // 초기 렌더링
    renderAttendanceTable();

})();
