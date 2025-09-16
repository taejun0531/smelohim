/* /js/userAcceptPage.js — 팝오버 + 권한변경 (기존 유지)
   ▶ 변경점: openEditPopup 제거 X, 대신 deleteUser 추가
*/

const API_URL_ACCEPT = "/admin/accept_user";
const API_URL_LEADERS = "/admin/getCellLeaderInfo";
const API_URL_DELETE = "/admin/delete_user";

let secAwait = null, secAdmin = null, secLeader = null;
let cellLeaderListCache = null;
let cellLeaderListLoading = null;

const _runningByEl = new WeakMap();
function isReentrant(el, windowMs = 400){
    const now = Date.now();
    const last = _runningByEl.get(el) || 0;
    if (now - last < windowMs) return true;
    _runningByEl.set(el, now);
    setTimeout(()=>_runningByEl.delete(el), windowMs);
    return false;
}
const isActivePill = (el) => !!(el && el.classList && el.classList.contains("active"));

function findSectionByTitle(prefixText){
    const cards = document.querySelectorAll(".card");
    for (const card of cards){
        const h3 = card.querySelector("h3");
        if (!h3) continue;
        const title = (h3.childNodes[0]?.textContent || h3.textContent || "").trim();
        if (title.startsWith(prefixText)){
            return { card, tbody: card.querySelector("tbody"), countSpan: h3.querySelector("span") };
        }
    }
    return null;
}
function updateCounts(){
    if (secAwait?.countSpan)  secAwait.countSpan.textContent  = `${secAwait.tbody?.children.length || 0}명`;
    if (secAdmin?.countSpan)  secAdmin.countSpan.textContent  = `${secAdmin.tbody?.children.length || 0}명`;
    if (secLeader?.countSpan) secLeader.countSpan.textContent = `${secLeader.tbody?.children.length || 0}명`;
}
function getAdminCount(){ return secAdmin?.tbody ? secAdmin.tbody.children.length : 0; }
function roleTextToCode(t){
    t=(t||"").trim(); if(t==="관리자")return"ADMIN"; if(t==="셀리더")return"USER"; return"AWAIT";
}
function codeToSection(code){ return code==="ADMIN"?secAdmin : code==="USER"?secLeader : secAwait; }
function setActivePillInRow(tr, roleCode){
    tr.querySelectorAll(".role-btn").forEach(el=>el.classList.remove("active"));
    const txt = roleCode==="ADMIN"?"관리자":roleCode==="USER"?"셀리더":"신청";
    const target = Array.from(tr.querySelectorAll(".role-btn"))
        .find(el => (el.textContent||"").trim()===txt);
    if (target) target.classList.add("active");
}
function moveRowToSection(tr, roleCode){
    const sec = codeToSection(roleCode); if(!sec?.tbody) return;
    sec.tbody.appendChild(tr); setActivePillInRow(tr, roleCode); updateCounts();
}

async function postChangeRole(userId, userRole, extra={}){
    try{
        const res = await fetch(API_URL_ACCEPT, {
            method:"POST", headers:{ "Content-Type":"application/json" },
            body: JSON.stringify({ id:String(userId), userRole:userRole, ...extra })
        });
        return await res.json();
    }catch(e){ console.error(e); return false; }
}

async function getCellLeaderInfo(){
    if (Array.isArray(cellLeaderListCache)) return cellLeaderListCache;
    if (cellLeaderListLoading) return cellLeaderListLoading;
    cellLeaderListLoading = (async ()=>{
        try{
            const res = await fetch(API_URL_LEADERS, {
                method:"POST", headers:{ "Content-Type":"application/json" },
                body: JSON.stringify({})
            });
            const data = await res.json();
            cellLeaderListCache = Array.isArray(data) ? data : [];
            return cellLeaderListCache;
        }catch(e){
            console.error(e); cellLeaderListCache=[]; return cellLeaderListCache;
        }finally{ cellLeaderListLoading=null; }
    })();
    return cellLeaderListLoading;
}

/* ===== Popover ============================================================ */
let currentPopover = null;
function closeLeaderPopover(){
    if (currentPopover && currentPopover.parentNode){
        currentPopover.parentNode.removeChild(currentPopover);
    }
    currentPopover = null;
}
function buildLeaderPopover(pill, leaders, onChoose){
    closeLeaderPopover();
    const pop = document.createElement("div");
    pop.className = "leader-popover";

    const list = document.createElement("ul");
    list.className = "leader-popover__list";

    if (!leaders.length){
        const li = document.createElement("li");
        li.className = "leader-popover__item";
        li.textContent = "리더 목록이 없습니다.";
        list.appendChild(li);
    }else{
        leaders.forEach(ld=>{
            const li = document.createElement("li");
            li.className = "leader-popover__item";
            li.dataset.leaderId = ld.id;
            li.textContent = ld.memberName || ld.name || ("리더#" + ld.id);
            li.addEventListener("click", ()=> onChoose({ id: ld.id, name: li.textContent }));
            list.appendChild(li);
        });
    }
    pop.appendChild(list);

    const rect = pill.getBoundingClientRect();
    const scrollY = window.scrollY || document.documentElement.scrollTop;
    const scrollX = window.scrollX || document.documentElement.scrollLeft;
    pop.style.top  = (rect.bottom + scrollY + 8) + "px";
    pop.style.left = (rect.left   + scrollX)     + "px";

    document.body.appendChild(pop);
    currentPopover = pop;

    const onOutside = (e)=>{ if(!currentPopover) return cleanup(); if(!currentPopover.contains(e.target)) cleanup(); };
    const onScroll  = ()=> cleanup();
    const onResize  = ()=> cleanup();
    const cleanup = ()=>{
        document.removeEventListener("mousedown", onOutside);
        window.removeEventListener("scroll", onScroll, true);
        window.removeEventListener("resize", onResize);
        closeLeaderPopover();
    };
    setTimeout(()=>{
        document.addEventListener("mousedown", onOutside);
        window.addEventListener("scroll", onScroll, true);
        window.addEventListener("resize", onResize);
    }, 0);
}

/* ===== 메인 처리 ========================================================== */
async function handleRoleChange(tr, pill){
    if (!tr || !pill) return;
    if (isActivePill(pill)) return;                // 활성 pill 무시
    const userId = tr.dataset.userId;
    if (!userId){ alert("userId를 찾을 수 없습니다."); return; }
    if (isReentrant(pill)) return;

    const roleText = (pill.textContent||"").trim();

    if (roleText === "셀리더"){
        const leaders = await getCellLeaderInfo();
        buildLeaderPopover(pill, leaders, async ({id, name})=>{
            if (!confirm(`정말로 셀리더(${name})로 권한을 변경하시겠습니까?`)) return;
            const ok = await postChangeRole(userId, "USER", { leaderId:id });
            if (!ok){ alert("수정 실패했습니다."); return; }
            alert("수정되었습니다.");
            moveRowToSection(tr, "USER");
            closeLeaderPopover();
        });
        return;
    }

    const nextRole = roleTextToCode(roleText);

    // 관리자 최소 1명 보장
    const currentIsAdmin = tr.closest(".card") === secAdmin?.card;
    if (currentIsAdmin && nextRole !== "ADMIN"){
        if (getAdminCount() <= 1){ alert("관리자는 최소 1명 이상 있어야 합니다."); return; }
    }

    if (!confirm("권한을 변경하시겠습니까?")) return;

    const ok = await postChangeRole(userId, nextRole);
    if (!ok){ alert("수정 실패했습니다."); return; }
    moveRowToSection(tr, nextRole);
    alert("수정되었습니다.");
}

/* ===== 삭제 기능 ========================================================== */
async function deleteUser(id){
    const tr = document.querySelector(`tr[data-user-id="${id}"]`);
    const userName = tr ? tr.querySelector("td")?.textContent.trim() : "";

    if (!confirm(`정말로 '${userName || "이 사용자"}'을/를 삭제하시겠습니까?`)) return;
    try{
        const res = await fetch(API_URL_DELETE, {
            method: "POST",
            headers: { "Content-Type":"application/json" },
            body: JSON.stringify({ deleteId: String(id) })
        });
        const ok = await res.json();
        if (!ok){
            alert(`'${userName || "사용자"}'을/를 삭제 실패했습니다.`);
            return;
        }

        // DOM에서 행 제거 + 카운트 갱신
        if (tr && tr.parentNode) tr.parentNode.removeChild(tr);
        updateCounts();
        alert(`'${userName || "사용자"}'이/가 삭제되었습니다.`);
    }catch(e){
        console.error(e);
        alert("삭제 요청 중 오류가 발생했습니다.");
    }
}

window.deleteUser = deleteUser;

/* ===== 이벤트 바인딩 ====================================================== */
function onDocumentClick(e){
    const pill = e.target.closest(".role-btn");
    if (!pill) return;
    if (isActivePill(pill)) return;      // 활성 pill 무시
    if (pill.dataset.inlineInvoke === "1") return;
    const tr = pill.closest("tr");
    handleRoleChange(tr, pill);
}

/* ===== 공개 함수 (HTML onclick 호환) ===================================== */
function openEditPopup(userId){
    // 남겨둠(호환). 현재는 사용하지 않지만 기존 코드와 충돌 없도록 유지.
    const url = "/admin/updateUserPage?userId=" + encodeURIComponent(userId);
    const props = "width=800,height=600,scrollbars=yes";
    window.open(url, "userEditPopup", props);
}
window.openEditPopup = openEditPopup;

function checkOnlyOne(userKey, element){
    try{
        if (isActivePill(element)) return;
        element.dataset.inlineInvoke = "1";
        const uid = String(userKey||"").replace("checkbox_","");
        const tr = element.closest("tr");
        if (tr) tr.dataset.userId = uid;
        handleRoleChange(tr, element);
    }finally{
        setTimeout(()=>{ if (element) delete element.dataset.inlineInvoke; }, 0);
    }
}
window.checkOnlyOne = checkOnlyOne;

/* ===== 초기화 ============================================================= */
function initUserAcceptPage(){
    secAwait  = findSectionByTitle("신청자");
    secAdmin  = findSectionByTitle("관리자");
    secLeader = findSectionByTitle("셀리더");

    document.querySelectorAll("tbody tr").forEach(tr=>{
        if (!tr.dataset.userId){
            // 버튼이 '삭제'로 변경되었으므로 둘 다 탐색
            const btn = tr.querySelector(".btn-delete, .btn-edit");
            const raw = btn?.getAttribute("onclick") || "";
            const m = raw.match(/\(\s*([0-9]+)\s*\)/);
            if (m) tr.dataset.userId = m[1];
        }
    });

    getCellLeaderInfo().catch(()=>{});
    updateCounts();
    document.addEventListener("click", onDocumentClick);
}
document.addEventListener("DOMContentLoaded", initUserAcceptPage);
