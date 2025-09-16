function submit_post(){
    // 회원가입 제출 담당
    const userName = document.getElementById("userName").value;
    const userId = document.getElementById("userId").value;
    const userPassword = document.getElementById("userPassword").value;
    const userCheckPassword = document.getElementById("userCheckPassword").value;
    const userNameCheckHidden = document.getElementById("userNameCheckHidden").value;
    const userIdCheckHidden = document.getElementById("userIdCheckHidden").value;

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

    document.querySelector("form").submit();
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