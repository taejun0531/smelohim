function chekcboxClick(element){
    const checkboxBaptism = document.getElementsByName("find_baptism");
    let checkSelf = false;

    checkboxBaptism.forEach((cb) => {
        if(cb.checked)
            checkSelf = true;
        cb.checked = false;
    });

    if(checkSelf)
        element.checked = true;
}

function isNull(element){
    return element === "" || element == null;
}

function deleteMember(element){
    const memberId = element.id;
    const memberName = element.name;

    if(confirm("정말 " + memberName + " 청년 정보를 삭제하시겠습니까?")){
        fetch("/admin/deleteMember", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                deleteMemberId: memberId
            })
        })
            .then((res) => {
                return res.json();
            })
            .then((res) => {
                if(res){
                    alert(name + " 청년 정보를 삭제했습니다.");
                    location.reload();
                }
                else
                    alert("삭제 중 오류가 발생했습니다.");
            });
    }else {
        alert("취소했습니다.");
    }

}

function findMember(element){
    const findName = document.getElementById("find_name").value;
    const findBirth_year = document.getElementById("find_birth_year").value;
    const findBirth_month = document.getElementById("find_birth_month").value;
    const findCellLeader = document.getElementById("find_cellLeader").value;
    const findBaptism = document.getElementById("find_baptism").value;
    const findNurture_year = document.getElementById("find_nurture_year").value;
    const findNurture_semester = document.getElementById("find_nurture_semester").value;
    const findGrowth_year = document.getElementById("find_growth_year").value;
    const findGrowth_semester = document.getElementById("find_growth_semester").value;
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
        findCategory = 'cellLeader';
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
    if(findCnt === 0){
        alert("검색할 내용을 입력해주세요.");
        return;
    }

    fetch("/admin/parsonalDataPage", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            findName: findName,
            findBirth_year: findBirth_year,
            findBirth_month: findBirth_month,
            findCellLeader: findCellLeader,
            findBaptism: findBaptism,
            findNurture_year: findNurture_year,
            findNurture_semester: findNurture_semester,
            findGrowth_year: findGrowth_year,
            findGrowth_semester: findGrowth_semester,
            findCategory: findCategory
        })
    })
        .then(response => response.text()) // HTML로 응답 받음
        .then(html => {
            // 받은 HTML로 현재 페이지 덮어쓰기
            const doc = new DOMParser().parseFromString(html, "text/html");
            const newTbody = doc.querySelector("#memberTable").innerHTML;
            document.querySelector("#memberTable").innerHTML = newTbody;

            const newCount = doc.querySelector("#memberTotalSize");
            document.querySelector("#memberTotalSize").textContent = newCount.textContent;
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
});

let nameSortState = 0;
let birthSortState = 0;

function nameSort(element) {
    // sortId가 0이면 오름차순, 1이면 내림차순 정렬
    const sortId = nameSortState;
    nameSortState = (nameSortState + 1) % 2;

    fetch("/admin/parsonalDataPage", {
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
            // 받은 HTML로 현재 페이지 덮어쓰기
            const doc = new DOMParser().parseFromString(html, "text/html");
            const newTbody = doc.querySelector("#memberTable").innerHTML;
            document.querySelector("#memberTable").innerHTML = newTbody;

            const newCount = doc.querySelector("#memberTotalSize");
            document.querySelector("#memberTotalSize").textContent = newCount.textContent;
        });

}

function birthSort(element) {
    // sortId가 0이면 오름차순, 1이면 내림차순 정렬
    const sortId = birthSortState;
    birthSortState = (birthSortState + 1) % 2;

    fetch("/admin/parsonalDataPage", {
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
            // 받은 HTML로 현재 페이지 덮어쓰기
            const doc = new DOMParser().parseFromString(html, "text/html");
            const newTbody = doc.querySelector("#memberTable").innerHTML;
            document.querySelector("#memberTable").innerHTML = newTbody;

            const newCount = doc.querySelector("#memberTotalSize");
            document.querySelector("#memberTotalSize").textContent = newCount.textContent;
        });

}

function updateMember(element){
    const moveUpdatePage = document.createElement('a');
    const memberId = element.id;
    moveUpdatePage.setAttribute('href', '/admin/updateMemberPage?memberId=' + memberId);
    moveUpdatePage.click();
}

async function createMember(element) {
    const memberName = document.getElementById("input_name").value;
    const memberBirth = document.getElementById("input_birth").value;
    let memberPhoneNumber = document.getElementById("input_phoneNumber").value;
    const memberAddress = document.getElementById("input_address").value;
    const checkboxBaptism = document.getElementsByName("input_baptism");
    const memberNurture = document.getElementById("input_nurture").checked;
    const memberDateNurture = document.getElementById("date_nurture").value;
    const memberGrowth = document.getElementById("input_growth").checked;
    const memberDateGrowth = document.getElementById("date_growth").value;
    const memberMemo = document.getElementById("input_memo").value;
    const memberCellLeader = document.getElementById("input_cellLeader").checked;

    let memberNurtureInformation = "";
    let memberGrowthInformation = "";

    if (memberName === "") {
        alert("추가할 청년의 이름을 입력해주세요.");
        return;
    }
    if (memberBirth === "") {
        alert("추가할 청년의 생년월일을 입력해주세요.");
        return;
    }
    if (memberPhoneNumber === "") {
        alert("추가할 청년의 핸드폰 번호를 입력해주세요.");
        return;
    }
    if (memberNurture && memberDateNurture === "") {
        alert("추가할 청년의 양육반 수료 날짜를 입력해주세요.");
        return;
    }
    if (!memberNurture && memberDateNurture !== "") {
        alert("추가할 청년의 양육반 수료 체크박스를 체크해주세요.");
        return;
    }
    if (memberGrowth && memberDateGrowth === "") {
        alert("추가할 청년의 성장반 수료 날짜를 입력해주세요.");
        return;
    }
    if (!memberGrowth && memberDateGrowth !== "") {
        alert("추가할 청년의 성장반 수료 체크박스를 체크해주세요.");
        return;
    }

    let memberBaptism = "";

    checkboxBaptism.forEach((cb) => {
        if (cb.checked) {
            memberBaptism = cb.value;
        }
    });

    if (memberPhoneNumber.slice(3, 4) !== '-')
        memberPhoneNumber = memberPhoneNumber.slice(0, 3) + '-' + memberPhoneNumber.slice(3);
    if (memberPhoneNumber.slice(8, 9) !== '-')
        memberPhoneNumber = memberPhoneNumber.slice(0, 8) + '-' + memberPhoneNumber.slice(8);

    try {
        const checkResponse = await fetch('/admin/checkMember', {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                memberName: memberName,
                memberBirth: memberBirth
            })
        });

        const checkmember = await checkResponse.json();

        if (checkmember) {
            alert("중복된 청년(이름/생일)이 있습니다.");
            return;
        }

        console.log(checkmember);

        const createResponse = await fetch('/admin/createMember', {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                memberName: memberName,
                memberBirth: memberBirth,
                memberPhoneNumber: memberPhoneNumber,
                memberAddress: memberAddress,
                memberBaptism: memberBaptism,
                memberNurtureInformation: memberNurtureInformation,
                memberGrowthInformation: memberGrowthInformation,
                memberMemo: memberMemo,
                memberCellLeader: memberCellLeader
            })
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

function memberPopup(element){
    const URL = "../user/memberDetailsPage?memberId=" + element.id;
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
