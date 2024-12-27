document.getElementById('checkNicknameButton').addEventListener('click', function () {
    const nickname = document.getElementById('nickname').value;
    const feedback = document.getElementById('nicknameFeedback');

    feedback.textContent = ''; // 메시지 초기화

    if (!nickname) {
        feedback.textContent = '닉네임을 입력해주세요.';
        feedback.classList.add('text-danger');
        return;
    }

    fetch(`/mypage/check-nickname?nickname=${encodeURIComponent(nickname)}`)
        .then(response => response.json())
        .then(data => {
            if (data.exists) {
                feedback.textContent = '이미 사용 중인 닉네임입니다.';
                feedback.classList.remove('text-success');
                feedback.classList.add('text-danger');
            } else {
                feedback.textContent = '사용 가능한 닉네임입니다.';
                feedback.classList.remove('text-danger');
                feedback.classList.add('text-success');
            }
        })
        .catch(error => {
            feedback.textContent = '오류가 발생했습니다. 다시 시도해주세요.';
            feedback.classList.remove('text-success');
            feedback.classList.add('text-danger');
        });
});

//const password1Input = document.getElementById('password1');
//const password2Input = document.getElementById('password2');
//const feedback = document.getElementById('passwordFeedback');
//
//// 실시간 검증 이벤트 추가
//function validatePasswords() {
//    const password1 = password1Input.value;
//    const password2 = password2Input.value;
//
//    if (!password1 || !password2) {
//        feedback.textContent = ''; // 하나롣 비어 있으면 메시지 초기화
//        feedback.classList.remove('text-danger', 'text-success');
//        return;
//    }
//
//    if (password1 !== password2) {
//        feedback.textContent = '비밀번호가 서로 일치하지 않습니다.';
//        feedback.classList.add('text-danger');
//        feedback.classList.remove('text-success');
//    } else {
//        feedback.textContent = '비밀번호가 일치합니다.';
//        feedback.classList.add('text-success');
//        feedback.classList.remove('text-danger');
//    }
//}
//
//password1Input.addEventListener('input', validatePasswords);
//password2Input.addEventListener('input', validatePasswords);

// 쿠키를 가져오는 함수
function getCookie(key) {
    var result = null;
    var cookie = document.cookie.split(';');
    cookie.some(function (item) {
        item = item.replace(' ', '');

        var dic = item.split('=');

        if (key === dic[0]) {
            result = dic[1];
            return true;
        }
    });

    return result;
}

// HTTP 요청을 보내는 함수
function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method: method,
        headers: { // 로컬 스토리지에서 액세스 토큰 값을 가져와 헤더에 추가
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json',
        },
        body: body,
    }).then(response => {
        if (response.status === 200 || response.status === 201) {
            return success();
        }
        const refresh_token = getCookie('refresh_token');
        if (response.status === 401 && refresh_token) {
            fetch('/api/token', {
                method: 'POST',
                headers: {
                    Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    refreshToken: getCookie('refresh_token'),
                }),
            })
                .then(res => {
                    if (res.ok) {
                        return res.json();
                    }
                })
                .then(result => { // 재발급이 성공하면 로컬 스토리지값을 새로운 액세스 토큰으로 교체
                    localStorage.setItem('access_token', result.accessToken);
                    httpRequest(method, url, body, success, fail);
                })
                .catch(error => fail());
        } else {
            return fail();
        }
    });
}