document.addEventListener('DOMContentLoaded', function () {
    const authButtons = document.getElementById('auth-buttons');

    fetch('/api/user', {
        method: 'GET',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
        },
    })
    .then(response => {
        if (response.status === 401) { // 비로그인 상태
            authButtons.innerHTML = `
                <a href="/signup" class="btn">회원가입</a>
                <a href="/login" class="btn">로그인</a>
            `;
            return;
        }
        return response.json();
    })
    .then(data => {
        if (data) { // 로그인 상태
            authButtons.innerHTML = `
                <span>${data.nickname}님, 환영합니다.</span>
                <a href="/logout" class="btn">로그아웃</a>
            `;
        }
    })
    .catch(error => {
        console.error('Error fetching user info:', error);
        authButtons.innerHTML = `
            <a href="/signup" class="btn">회원가입</a>
            <a href="/login" class="btn">로그인</a>
        `;
    });
});
