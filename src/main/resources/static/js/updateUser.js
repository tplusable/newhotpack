// 사용자 데이터 로드
function loadUserData() {
    function success() {
        fetch('/api/user', {
            method: 'GET',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            },
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch user data');
            }
            return response.json();
        })
        .then(data => {
            document.getElementById('email').value = data.email;
            document.getElementById('name').value = data.name;
            document.getElementById('nickname').value = data.nickname;
            document.getElementById('nickname').setAttribute('data-current-nickname', data.nickname); // data-current-nickname 속성 설정
        })
        .catch(error => {
            console.error('Error loading user data:', error);
            alert('로그인이 필요합니다.');
            location.replace('/login');
        });
    }

    function fail() {
        alert("데이터를 가져올 수 없습니다. 로그인 페이지로 이동합니다.");
        location.replace('/login');
    }
    httpRequest('GET', '/api/user', null, success, fail);
}

// HTML 페이지 로드 후 데이터 가져오기
window.onload = loadUserData;

// 수정 기능
const modifyButton = document.getElementById('updateUser-btn');
const nicknameFeedback = document.getElementById('nicknameFeedback');
const passwordFeedback = document.getElementById('passwordFeedback');

// 상태 체크 함수
function checkFormValidity() {
    // 닉네임이나 비밀번호 관련 오류 메시지가 있는지 확인
    const nicknameError = nicknameFeedback.classList.contains('text-danger');
    const passwordError = passwordFeedback.classList.contains('text-danger');

    // 비밀번호 확인 메시지가 비어 있는지 확인
    const isPasswordFeedbackEmpty = passwordFeedback.textContent.trim() === '';

    // **모든 조건을 만족해야 버튼 활성화**
    if (!nicknameError && !passwordError && !isPasswordFeedbackEmpty) {
        modifyButton.disabled = false; // **활성화**
    } else {
        modifyButton.disabled = true; // **비활성화**
    }
}

if (modifyButton) {
    modifyButton.addEventListener('click', event => {

        const body = JSON.stringify({
            name: document.getElementById('name').value,
            nickname: document.getElementById('nickname').value,
            password1: password1Input.value
        })

        function success() {
            alert('수정 완료되었습니다.');
            location.replace(`/mypage`);
        }

        function fail() {
            alert('수정 실패했습니다.');
            location.replace('/updateUser');
        }
        httpRequest('PUT', '/api/user', body, success, fail);
    });
}

// 닉네임 중복 확인
document.getElementById('checkNicknameButton').addEventListener('click', function () {
    const nickname = document.getElementById('nickname').value;
    const currentNickname = document.getElementById('nickname').getAttribute('data-current-nickname');

    // 메시지 초기화
    nicknameFeedback.textContent = '';
    nicknameFeedback.classList.remove('text-danger', 'text-success', 'text-info');


    if (!nickname) {
            nicknameFeedback.textContent = '닉네임을 입력해주세요.';
            nicknameFeedback.classList.add('text-danger'); // **오류 메시지**
            checkFormValidity(); // **유효성 확인**
            return;
    }

    // 현재 닉네임과 입력된 닉네임 비교
    if (nickname === currentNickname) {
        nicknameFeedback.textContent = '현재 닉네임과 동일합니다.';
        nicknameFeedback.classList.add('text-info'); // 현재 닉네임과 동일
        checkFormValidity();
        return;
    }

    fetch(`/updateUser/check-nickname?nickname=${encodeURIComponent(nickname)}`)
        .then(response => response.json())
        .then(data => {
             if (data.exists) {
                nicknameFeedback.textContent = '이미 사용 중인 닉네임입니다.';
                nicknameFeedback.classList.add('text-danger');
            } else {
                nicknameFeedback.textContent = '사용 가능한 닉네임입니다.';
                nicknameFeedback.classList.add('text-success');
            }
            checkFormValidity();
        })
        .catch(error => {
            nicknameFeedback.textContent = '오류가 발생했습니다. 다시 시도해주세요.';
            nicknameFeedback.classList.add('text-danger');
            checkFormValidity();
        });
});

const password1Input = document.getElementById('password1');
const password2Input = document.getElementById('password2');

// 실시간 검증 이벤트 추가
function validatePasswords() {
    const password1 = password1Input.value;
    const password2 = password2Input.value;

    if (!password1 || !password2) {
        passwordFeedback.textContent = ''; // 하나롣 비어 있으면 메시지 초기화
        passwordFeedback.classList.remove('text-danger', 'text-success');
        checkFormValidity();
        return;
    }

    if (password1 !== password2) {
        passwordFeedback.textContent = '비밀번호가 서로 일치하지 않습니다.';
        passwordFeedback.classList.add('text-danger');
        passwordFeedback.classList.remove('text-success');
    } else {
        passwordFeedback.textContent = '비밀번호가 일치합니다.';
        passwordFeedback.classList.add('text-success');
        passwordFeedback.classList.remove('text-danger');
    }
    checkFormValidity();
}

password1Input.addEventListener('input', validatePasswords);
password2Input.addEventListener('input', validatePasswords);

checkFormValidity();

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