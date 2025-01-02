document.addEventListener('DOMContentLoaded', function () {
    const authButtons = document.getElementById('auth-buttons');

    // 사용자 정보 요청
    function fetchUserInfo() {
        function success() {
            fetch('/api/user', {
                method: 'GET',
                headers: {
                    Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                },
            })
            .then(response => {
                if (response.status === 401) { // 비로그인 상태
                    authButtons.innerHTML = `
                        <a href="/signup" class="signup-btn">회원가입</a>
                        <a href="/login" class="login-btn">로그인</a>
                    `;
                    return null;
                }
                return response.json();
            })
            .then(data => {
                if (data) { // 로그인 상태
                    authButtons.innerHTML = `
                        <a href="/mypage" class="mypage-btn">마이페이지</a>
                        <span>${data.nickname}님, 환영합니다.</span>
                        <button id="logout-btn">로그아웃</button>
                    `;
                    attachLogoutEvent(); // 로그아웃 이벤트 등록
                }
            })
            .catch(error => {
                console.error('Error fetching user info:', error);
                authButtons.innerHTML = `
                    <a href="/signup" class="signup-btn">회원가입</a>
                    <a href="/login" class="login-btn">로그인</a>
                `;
            });
        }

        function fail() {
            authButtons.innerHTML = `
                <a href="/signup" class="signup-btn">회원가입</a>
                <a href="/login" class="login-btn">로그인</a>
            `;
        }

        httpRequest('GET', '/api/user', null, success, fail);
    }

    // 로그아웃 이벤트 등록 함수
    function attachLogoutEvent() {
        const logoutButton = document.getElementById('logout-btn');
        if (logoutButton) {
            logoutButton.addEventListener('click', function () {
                function success() {
                    localStorage.removeItem('access_token'); // 액세스 토큰 삭제
                    deleteCookie('refresh_token'); // 리프레시 토큰 삭제
                    alert('로그아웃 성공');
                    location.replace('/');
                }

                function fail() {
                    alert('로그아웃 실패');
                }

                httpRequest('DELETE', '/api/refresh-token', null, success, fail);
            });
        }
    }

    // 쿠키 삭제 함수
    function deleteCookie(name) {
        document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    }

    // 사용자 정보 가져오기 호출
    fetchUserInfo();
});

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
