function fetchMyArticle() {
    function success() {
        fetch('/api/user/articles', {
            method: 'GET',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            },
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load articles');
            }
            return response.json();
        })
        .then(data => {
            const container = document.getElementById('articles-container');

            // 글이 없을 경우 메시지 표시
            if (data.length === 0) {
                container.innerHTML = `<p>작성한 글이 없습니다.</p>`;
                return;
            }

            // 글 목록 렌더링
            data.forEach(article => {
                const card = document.createElement('div');
                card.className = 'card mb-3';

                // 긴 내용 잘라내기
                const maxLength = 100;
                const truncatedContent = article.content.length > maxLength
                    ? article.content.substring(0, maxLength) + '...'
                    : article.content;

                card.innerHTML = `
                    <div class="card-header">${article.id}</div>
                    <div class="card-body">
                        <h5 class="card-title">${article.title}</h5>
                        <p class="card-text">${truncatedContent}</p>
                        <a href="/articles/${article.id}" class="btn btn-primary">보러가기</a>
                    </div>
                `;
                container.appendChild(card);
            });
        })
        .catch(error => {
            console.error('Error loading articles:', error);
            alert('로그인이 필요합니다.');
            location.replace('/login');
        });
    }
    function fail() {
        alert("데이터를 가져올 수 없습니다. 로그인 페이지로 이동합니다.");
        location.replace('/login');
    }
    httpRequest('GET', '/api/user/articles', null, success, fail);
}

// HTML 페이지 로드 후 데이터 가져오기
window.onload = fetchMyArticle;

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