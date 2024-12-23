function isValidEmail(email) {
    const emailRegex = /^[A-Za-z0-9+_.-]+@(.+)$/;
    return emailRegex.test(email);
}

document.getElementById('checkEmailButton').addEventListener('click', function () {
    const email = document.getElementById('email').value;
    const feedback = document.getElementById('emailFeedback');

    feedback.textContent = ''; // 메시지 초기화

    if (!email) {
        feedback.textContent = '이메일을 입력해주세요.';
        feedback.classList.add('text-danger');
        return;
    }

    if (!isValidEmail(email)) {
        feedback.textContent = '유효하지 않은 이메일 형식입니다.';
        feedback.classList.add('text-danger');
        return;
    }

    // 서버 요청
    fetch(`check-email?email=${encodeURIComponent(email)}`)
        .then(response => response.json())
        .then(data => {
        console.log("Response Data:", data);
            if (data.exists) {
                feedback.textContent = '이미 사용 중인 이메일입니다.';
                feedback.classList.remove('text-success');
                feedback.classList.add('text-danger');
            } else {
                feedback.textContent = '사용 가능한 이메일입니다.';
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

document.getElementById('checkNicknameButton').addEventListener('click', function () {
    const nickname = document.getElementById('nickname').value;
    const feedback = document.getElementById('nicknameFeedback');

    feedback.textContent = ''; // 메시지 초기화

    if (!nickname) {
        feedback.textContent = '닉네임을 입력해주세요.';
        feedback.classList.add('text-danger');
        return;
    }

    fetch(`check-nickname?nickname=${encodeURIComponent(nickname)}`)
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

const password1Input = document.getElementById('password1');
const password2Input = document.getElementById('password2');
const feedback = document.getElementById('passwordFeedback');

// 실시간 검증 이벤트 추가
function validatePasswords() {
    const password1 = password1Input.value;
    const password2 = password2Input.value;

    if (!password1 || !password2) {
        feedback.textContent = ''; // 하나롣 비어 있으면 메시지 초기화
        feedback.classList.remove('text-danger', 'text-success');
        return;
    }

    if (password1 !== password2) {
        feedback.textContent = '비밀번호가 서로 일치하지 않습니다.';
        feedback.classList.add('text-danger');
        feedback.classList.remove('text-success');
    } else {
        feedback.textContent = '비밀번호가 일치합니다.';
        feedback.classList.add('text-success');
        feedback.classList.remove('text-danger');
    }
}

password1Input.addEventListener('input', validatePasswords);
password2Input.addEventListener('input', validatePasswords);