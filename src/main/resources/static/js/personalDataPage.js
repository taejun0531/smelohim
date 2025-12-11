// ================================
//    /js/personalDataPage.js
// ================================

// null → '' 변환
const n = (v) => v == null ? '' : String(v);

// '' → null 변환 (DB 저장용)
const strOrNull = (v) => {
    const s = String(v ?? '').trim();
    return s === '' ? null : s;
};

function isNull(element){
    return element === "" || element == null;
}

function findMember(element){
    const findName             = n(document.getElementById("find_name").value);
    const findBirth_year       = n(document.getElementById("find_birth_year").value);
    const findBirth_month      = n(document.getElementById("find_birth_month").value);
    const findCellLeader       = n(document.getElementById("find_cellLeader").value);
    const findBaptism          = n(document.getElementById("find_baptism").value);
    const findNurture_year     = n(document.getElementById("find_nurture_year").value);
    const findNurture_semester = n(document.getElementById("find_nurture_semester").value);
    const findGrowth_year      = n(document.getElementById("find_growth_year").value);
    const findGrowth_semester  = n(document.getElementById("find_growth_semester").value);

    let findCnt = 0;
    let findCategory = '';

    if(!isNull(findBirth_year) && !isNull(findBirth_month)) findCnt--;
    if(!isNull(findNurture_year) && !isNull(findNurture_semester)) findCnt--;
    if(!isNull(findGrowth_year) && !isNull(findGrowth_semester)) findCnt--;

    if(!isNull(findName)) {
        findCnt++;
        findCategory = 'name';
    }
    if(!isNull(findBirth_year)) {
        findCnt++;
        findCategory = 'birth';
    }
    if(!isNull(findBirth_month)) {
        findCnt++;
        findCategory = 'birth';
    }
    if(!isNull(findCellLeader)) {
        findCnt++;
        if (findCellLeader === 'allCellLeader') findCategory = 'allCellLeader'
        else findCategory = 'cellLeader';
    }
    if(!isNull(findBaptism)) {
        findCnt++;
        findCategory = 'baptism';
    }
    if(!isNull(findNurture_year)) {
        findCnt++;
        findCategory = 'nurture';
    }
    if(!isNull(findNurture_semester)) {
        findCnt++;
        findCategory = 'nurture';
    }
    if(!isNull(findGrowth_year)) {
        findCnt++;
        findCategory = 'growth';
    }
    if(!isNull(findGrowth_semester)) {
        findCnt++;
        findCategory = 'growth';
    }

    console.log(findCnt);
    console.log(findCategory);

    if(findCnt > 1){
        alert("검색할 카테고리를 한 가지만 입력해주세요.");
        return;
    }
    if(findCnt === 0)
        findCategory = "ALL";

    fetch("/admin/personalDataPage", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            findName:             strOrNull(findName),
            findBirth_year:       strOrNull(findBirth_year),
            findBirth_month:      strOrNull(findBirth_month),
            findCellLeader:       strOrNull(findCellLeader),
            findBaptism:          strOrNull(findBaptism),
            findNurture_year:     strOrNull(findNurture_year),
            findNurture_semester: strOrNull(findNurture_semester),
            findGrowth_year:      strOrNull(findGrowth_year),
            findGrowth_semester:  strOrNull(findGrowth_semester),
            findCategory:         findCategory
        })
    })
        .then(response => response.text()) // HTML로 응답 받음
        .then(html => {
            const doc = new DOMParser().parseFromString(html, "text/html");

            // 멤버 테이블 갱신
            const newTbody = doc.querySelector("#memberTable");
            const tbody = document.querySelector("#memberTable");
            if (newTbody && tbody) {
                tbody.innerHTML = n(newTbody.innerHTML);
            }

            // 총원 라벨 갱신
            const newCount = doc.querySelector("#memberTotalSize");
            const countLabel = document.querySelector("#memberTotalSize");
            if (newCount && countLabel) {
                countLabel.textContent = n(newCount.textContent);
            }

            // 검색 결과 0명일 경우 메시지 표시
            if (tbody && tbody.children.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" style="text-align:center; padding:20px; opacity:0.7;">
                            검색된 결과가 없습니다.
                        </td>
                    </tr>
                `;
            }
        });

}

// === [추가] 모든 검색 입력에서 Enter 누르면 findMember 실행 ===
document.addEventListener('DOMContentLoaded', () => {
    const inputs = [
        '#find_name',
        '#find_birth_year',
        '#find_birth_month',
        '#find_cellLeader',
        '#find_baptism',
        '#find_nurture_year',
        '#find_nurture_semester',
        '#find_growth_year',
        '#find_growth_semester'
    ].map(sel => document.querySelector(sel)).filter(Boolean);

    const searchBtn = document.getElementById('searchBtn');
    if (!searchBtn) return;

    const onEnter = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();        // 불필요한 기본 동작 방지
            findMember(searchBtn);     // 기존 함수 재사용
        }
    };

    inputs.forEach(el => el.addEventListener('keydown', onEnter));

    // 🔢 검색 연도 입력 필드는 숫자 4자리만 허용
    ["find_birth_year","find_nurture_year","find_growth_year"]
        .forEach(id=>{
            const el = document.getElementById(id);
            if(el){
                el.addEventListener("input", () => {
                    el.value = el.value.replace(/[^0-9]/g,"").slice(0,4);
                });
            }
        });

});

let nameSortState = 0;
let birthSortState = 0;

// 전화번호 auto-format 유틸
function autoPhoneFormat(v){
    v = String(v ?? '').replace(/[^0-9]/g, "");
    if (v.length < 4) return v;
    if (v.length < 8) return v.slice(0,3) + "-" + v.slice(3);
    return v.slice(0,3) + "-" + v.slice(3,7) + "-" + v.slice(7,11);
}

function nameSort(element) {
    // sortId가 0이면 오름차순, 1이면 내림차순 정렬
    const sortId = nameSortState;
    nameSortState = (nameSortState + 1) % 2;

    fetch("/admin/personalDataPage", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            sortId: sortId,
            findCategory: "nameSort"
        })
    })
        .then(response => response.text()) // HTML로 응답 받음
        .then(html => {
            const doc = new DOMParser().parseFromString(html, "text/html");

            const newTbody = doc.querySelector("#memberTable");
            const tbody = document.querySelector("#memberTable");
            if (newTbody && tbody) {
                tbody.innerHTML = n(newTbody.innerHTML);
            }

            const newCount = doc.querySelector("#memberTotalSize");
            const countLabel = document.querySelector("#memberTotalSize");
            if (newCount && countLabel) {
                countLabel.textContent = n(newCount.textContent);
            }

            // 검색 결과 0명일 경우 메시지 표시
            if (tbody && tbody.children.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" style="text-align:center; padding:20px; opacity:0.7;">
                            검색된 결과가 없습니다.
                        </td>
                    </tr>
                `;
            }
        });

}

function birthSort(element) {
    // sortId가 0이면 오름차순, 1이면 내림차순 정렬
    const sortId = birthSortState;
    birthSortState = (birthSortState + 1) % 2;

    fetch("/admin/personalDataPage", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            sortId: sortId,
            findCategory: "birthSort"
        })
    })
        .then(response => response.text()) // HTML로 응답 받음
        .then(html => {
            const doc = new DOMParser().parseFromString(html, "text/html");

            const newTbody = doc.querySelector("#memberTable");
            const tbody = document.querySelector("#memberTable");
            if (newTbody && tbody) {
                tbody.innerHTML = n(newTbody.innerHTML);
            }

            const newCount = doc.querySelector("#memberTotalSize");
            const countLabel = document.querySelector("#memberTotalSize");
            if (newCount && countLabel) {
                countLabel.textContent = n(newCount.textContent);
            }

            // 검색 결과 0명일 경우 메시지 표시
            if (tbody && tbody.children.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" style="text-align:center; padding:20px; opacity:0.7;">
                            검색된 결과가 없습니다.
                        </td>
                    </tr>
                `;
            }
        });

}

/**
 * 청년 추가 버튼 클릭 시: 결과 테이블에서 입력용 행 토글
 * - 첫 클릭: 입력 <tr> 생성
 * - 다시 클릭: 해당 <tr> 제거
 * HTML: <input type="button" id="addMemberBtn" onclick="addMember(this)" value="청년 추가"/>
 */
function addMember(btn){
    const tbody = document.getElementById("memberTable");
    if (!tbody) return;

    // 이미 입력 행이 있으면 → 제거(토글 OFF)
    const existing = document.getElementById("newMemberRow");
    if (existing) {
        existing.remove();
        return;
    }

    // 셀 리더 옵션 템플릿에서 options만 복사
    const templateSelect = document.getElementById("cellLeaderTemplate");
    const cellOptionsHtml = templateSelect ? templateSelect.innerHTML : '<option value="">없음</option>';

    const newTr = document.createElement("tr");
    newTr.id = "newMemberRow";

    newTr.innerHTML = `
        <td>
            <button type="button"
                    id="saveMemberBtn"
                    class="save-member-btn"
                    onclick="createMember(this)">
                청년 저장
            </button>
        </td>

        <!-- 이름 -->
        <td>
            <input type="text"
                   id="input_name"
                   placeholder="이름" />
        </td>

        <!-- 생년월일: flatpickr로 커스텀 달력 -->
        <td>
            <input
                type="text"
                id="input_birth"
                placeholder="YYYY-MM-DD"
                autocomplete="off"
            />
        </td>

        <!-- 전화번호 -->
        <td>
            <input type="text"
                   id="input_phoneNumber"
                   placeholder="예: 010-1234-5678" />
        </td>

        <!-- 셀 정보: 템플릿 옵션 복사 -->
        <td>
            <select id="input_cellInfo">
                ${cellOptionsHtml}
            </select>
        </td>

        <!-- 세례 여부: select -->
        <td>
            <select id="input_baptism">
                <option value="">없음</option>
                <option value="입교">입교</option>
                <option value="세례">세례</option>
                <option value="유아세례">유아세례</option>
                <option value="학습">학습</option>
            </select>
        </td>

        <!-- 양육반: 연도 + 학기 -->
        <td>
            <input type="number"
                   id="input_nurture_year"
                   placeholder="YYYY"
                   min="1900"
                   max="2999"
                   style="width:70px" />
            <select id="input_nurture_semester">
                <option value="">없음</option>
                <option value="상반기">상반기</option>
                <option value="하반기">하반기</option>
            </select>
        </td>

        <!-- 성장반: 연도 + 학기 -->
        <td>
            <input type="number"
                   id="input_growth_year"
                   placeholder="YYYY"
                   min="1900"
                   max="2999"
                   style="width:70px" />
            <select id="input_growth_semester">
                <option value="">없음</option>
                <option value="상반기">상반기</option>
                <option value="하반기">하반기</option>
            </select>
        </td>
    `;

    // 새 입력 행에서 Enter 누르면 "청년 저장" 실행
    const saveBtn = newTr.querySelector('#saveMemberBtn');
    const enterHandler = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            if (saveBtn) createMember(saveBtn);
        }
    };
    newTr.querySelectorAll('input, select').forEach(el => {
        el.addEventListener('keydown', enterHandler);
    });

    // === 생년월일 flatpickr 달력 연결 ===
    const birthInput = newTr.querySelector('#input_birth');
    if (birthInput && window.flatpickr) {
        flatpickr(birthInput, {
            dateFormat: "Y-m-d",
            locale: "ko",
            allowInput: false,   // 직접 타이핑 막고 달력만 사용
            disableMobile: true  // 모바일에서도 커스텀 달력 강제 사용
        });
    }

    // 헤더 바로 아래(리스트 맨 위)에 추가
    tbody.prepend(newTr);

    // === 날짜 입력창 클릭 시 바로 달력 열기 (지원 브라우저에서) ===
    const dateInputs = newTr.querySelectorAll('input[type="date"]');
    dateInputs.forEach(input => {
        if (input.showPicker) {
            // 클릭 / 포커스 둘 다 showPicker 시도
            const openPicker = () => input.showPicker();
            input.addEventListener('click', openPicker);
            input.addEventListener('focus', openPicker);
        }
    });

    // 전화번호 자동 포맷팅
    const phoneInput = document.getElementById("input_phoneNumber");
    if (phoneInput) {
        phoneInput.addEventListener("input", (e) => {
            e.target.value = autoPhoneFormat(e.target.value);
        });
    }

    // 양육/성장 연도 4자리 숫자만 허용
    ["input_nurture_year", "input_growth_year"].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener("input", () => {
                el.value = el.value.replace(/[^0-9]/g, "").slice(0, 4);
            });
        }
    });
}

async function createMember(element) {
    // === 엘리먼트
    const nameEl        = document.getElementById("input_name");
    const birthEl       = document.getElementById("input_birth");
    const phoneEl       = document.getElementById("input_phoneNumber");
    const cellInfoEl    = document.getElementById("input_cellInfo");
    const baptismEl     = document.getElementById("input_baptism");

    const nurtureYearEl   = document.getElementById("input_nurture_year");
    const nurtureSemEl    = document.getElementById("input_nurture_semester");
    const growthYearEl    = document.getElementById("input_growth_year");
    const growthSemEl     = document.getElementById("input_growth_semester");

    // === 값 읽기 (모두 optional)
    const memberName        = n(nameEl ? nameEl.value : '');
    const memberBirth       = n(birthEl ? birthEl.value : '');
    let   memberPhoneNumber = n(phoneEl ? phoneEl.value : '');

    let cellKey = null;
    let cellName = null;
    const memberBaptism     = baptismEl ? n(baptismEl.value) : null;

    const nurtureYear       = nurtureYearEl ? n(nurtureYearEl.value) : '';
    const nurtureSemester   = nurtureSemEl ? n(nurtureSemEl.value) : '';
    const growthYear        = growthYearEl ? n(growthYearEl.value) : '';
    const growthSemester    = growthSemEl ? n(growthSemEl.value) : '';

    if (cellInfoEl) {
        cellKey = n(cellInfoEl.value); // 선택된 ID
        if (cellKey != null && cellKey !== ''){
            const selectedOption = cellInfoEl.options[cellInfoEl.selectedIndex];
            cellName = selectedOption ? n(selectedOption.textContent) : null;
        }
    }

    // === 필수 입력 → 이름만 체크
    if (memberName == null || memberName === "") {
        alert("추가할 청년의 이름을 입력해주세요.");
        return;
    }

    // === 전화번호는 optional이지만, 값이 있다면 포맷팅
    if (memberPhoneNumber && memberPhoneNumber.trim() !== "")
        memberPhoneNumber = autoPhoneFormat(memberPhoneNumber);

    try {
        // === 중복 검사 (이름 + 생년월일) — 생년월일은 null 가능
        const checkResponse = await fetch('/admin/checkMember', {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                memberName: memberName,
                memberBirth: strOrNull(memberBirth)
            })
        });

        const checkmember = await checkResponse.json();

        if (checkmember) {
            alert("중복된 청년(이름/생일)이 있습니다.");
            return;
        }

        // === 서버로 보내는 최종 JSON
        const payload = {
            memberName: memberName,
            memberBirth: strOrNull(memberBirth),
            memberPhoneNumber: strOrNull(memberPhoneNumber),
            memberBaptism: strOrNull(memberBaptism),

            // 셀 정보
            cellKey: strOrNull(cellKey),
            cellName: strOrNull(cellName),

            // 양육/성장
            nurtureYear:      addYearSuffix(nurtureYear),
            nurtureSemester:  strOrNull(nurtureSemester),
            growthYear:       addYearSuffix(growthYear),
            growthSemester:   strOrNull(growthSemester),
        };

        const createResponse = await fetch('/admin/createMember', {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const createResult = await createResponse.json();

        if (createResult) {
            alert(memberName + " 청년이 추가 되었습니다.");
            location.reload();
        } else {
            alert("추가할 수 없습니다.\n관리자에게 문의하세요.");
        }

    } catch (err) {
        console.error(err);
        alert("오류가 발생했습니다.\n관리자에게 문의하세요.");
    }
}

function addYearSuffix(v) {
    const s = n(v);               // null → "" 정리
    if (s === "") return null;    // 비어있으면 null
    return s;              // 값 있으면 "2023"
}

function memberPopup(element){
    const URL = "../admin/memberDetailsPage?memberId=" + element.id;
    const Properties = "width=800,height=600,scrollbars=yes";

    window.open(URL, "memberDetailsPopup", Properties);
}


// === 정렬 아이콘(▲/▼) 표시 보조 스니펫: 기존 코드 '추가'만 ===
(function(){
    function getHeaders(){
        const ths = document.querySelectorAll('table:nth-of-type(2) tr th');
        let thName=null, thBirth=null;
        ths.forEach(th=>{
            const txt = (th.textContent||'').trim();
            if (!thName  && ((th.getAttribute('onclick')||'').includes('nameSort')  || txt.includes('이름'))) thName  = th;
            if (!thBirth && ((th.getAttribute('onclick')||'').includes('birthSort') || txt.includes('생년월일'))) thBirth = th;
        });
        return { thName, thBirth };
    }
    function reset(){
        const { thName, thBirth } = getHeaders();
        if (thName){ thName.classList.remove('sorted'); thName.dataset.sort='none'; }
        if (thBirth){ thBirth.classList.remove('sorted'); thBirth.dataset.sort='none'; }
    }
    function setIndicator(which, dir){
        const { thName, thBirth } = getHeaders();
        if (which==='name'){
            if (thBirth){ thBirth.classList.remove('sorted'); thBirth.dataset.sort='none'; }
            if (thName){ thName.classList.add('sorted'); thName.dataset.sort=dir; }
        }else if(which==='birth'){
            if (thName){ thName.classList.remove('sorted'); thName.dataset.sort='none'; }
            if (thBirth){ thBirth.classList.add('sorted'); thBirth.dataset.sort=dir; }
        }
    }

    // 사용자가 어떤 헤더를 방금 눌렀는지 추적
    let currentSort = null;
    document.addEventListener('click', (e)=>{
        const th = e.target.closest('table:nth-of-type(2) tr th[onclick]');
        if(!th) return;
        const cb = th.getAttribute('onclick')||'';
        if (cb.includes('nameSort')) currentSort = 'name';
        if (cb.includes('birthSort')) currentSort = 'birth';
    }, true);

    // 테이블 갱신을 감지해서 아이콘 갱신
    const target = document.getElementById('memberTable');
    if (!target) return;
    const observer = new MutationObserver(()=>{
        if (currentSort === 'name'){
            const last = (typeof nameSortState!=='undefined') ? (nameSortState + 1) % 2 : 0; // 요청 당시 sortId
            setIndicator('name', last===0 ? 'asc' : 'desc');
        } else if (currentSort === 'birth'){
            const last = (typeof birthSortState!=='undefined') ? (birthSortState + 1) % 2 : 0;
            setIndicator('birth', last===0 ? 'asc' : 'desc');
        } else {
            reset(); // 검색 등으로 갱신 시 초기화
        }
    });
    observer.observe(target, { childList:true, subtree:true });

    // 최초 초기화
    reset();
})();
