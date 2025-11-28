async function submit_post(){
    // 회원가입 제출 담당
    const userName = document.getElementById("userName").value;
    const userId = document.getElementById("userId").value;
    const userPassword = document.getElementById("userPassword").value;
    const userCheckPassword = document.getElementById("userCheckPassword").value;
    const userNameCheckHidden = document.getElementById("userNameCheckHidden").value;
    const userIdCheckHidden = document.getElementById("userIdCheckHidden").value;

    // 1. 클라이언트 검증
    if(userName === ""){
        alert("이름을 입력해주세요.");
        return;
    }
    if(userId === ""){
        alert("아이디를 입력해주세요.");
        return;
    }
    if(userPassword === ""){
        alert("비밀번호를 입력해주세요.");
        return;
    }
    if(userPassword !== userCheckPassword){
        alert("비밀번호가 맞지 않습니다.");
        return;
    }
    if(userNameCheckHidden === "false"){
        alert("이름 중복 확인이 필요합니다.");
        return;
    }
    if(userIdCheckHidden === "false"){
        alert("아이디 중복 확인이 필요합니다.");
        return;
    }

    try {
        // 3. fetch로 직접 POST 요청
        const res = await fetch('/process/createAccount', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userName: userName,
                userId: userId,
                userPassword: userPassword,
                userCheckPassword: userCheckPassword
            })
        });

        if (!res.ok) {
            // HTTP 4xx/5xx 같은 경우
            alert("회원가입 요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        // 서버에서 JSON 응답을 돌려준다고 가정
        const data = await res.json();

        if (data) {
            alert("회원가입이 완료되었습니다.");
            window.location.href = "/loginPage";
        } else
            alert("회원가입에 실패했습니다.\n실패가 계속된다면 관리자에게 문의하세요.");

    } catch (error) {
        console.error("createAccount error:", error);
        alert("요청 처리 중 예기치 못한 오류가 발생했습니다.");
    }
}

function userIdCheckPost(){
    const userId = document.getElementById("userId").value;

    fetch('/process/userIdCheck', {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            userId: userId
        })
    })
        .then((res) => {
            return res.json();
        })
        .then((res) => {
            if(res){
                alert("아이디 중복 확인 되었습니다.")
                document.getElementById("userIdCheckHidden").value = "true";
            }else {
                alert("중복된 아이디가 존재합니다.")
                document.getElementById("userIdCheckHidden").value = "false";
            }
        });
}

function userNameCheckPost(){
    const userName = document.getElementById("userName").value;

    fetch('/process/userNameCheck', {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            userName: userName
        })
    })
        .then((res) => {
            return res.json();
        })
        .then((res) => {
            if(res){
                alert("이름 중복 확인 되었습니다.")
                document.getElementById("userNameCheckHidden").value = "true";
            }else {
                alert("중복된 이름이 존재합니다.")
                document.getElementById("userNameCheckHidden").value = "false";
            }
        });
}

function userIdKeyDown(){
    document.getElementById("userIdCheckHidden").value = "false";
}

function userNameKeyDown(){
    document.getElementById("userNameCheckHidden").value = "false";
}

// === Enter 키로 회원가입 실행 ===
document.addEventListener("DOMContentLoaded", () => {
    const inputs = [
        document.getElementById("userName"),
        document.getElementById("userId"),
        document.getElementById("userPassword"),
        document.getElementById("userCheckPassword")
    ].filter(Boolean);

    const onEnter = (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            submit_post();   // 🔥 엔터로 회원가입 실행
        }
    };

    inputs.forEach(el => el.addEventListener("keydown", onEnter));
});