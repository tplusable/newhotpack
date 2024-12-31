// 페이지 로드시
window.onload = fetchMyRecommendedArticles;

/** 내가 추천한 글 목록을 가져와 화면에 렌더링 */
function fetchMyRecommendedArticles() {
    function success() {
        // 여기서 실제로 목록을 가져옴
        fetch('/api/user/recommended-articles', {
            method: 'GET',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('access_token')
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load recommended articles');
            }
            return response.json(); // [{id, title, content}, ...]
        })
        .then(data => {
            const container = document.getElementById('recommended-articles-container');

            if (data.length === 0) {
                container.innerHTML = `<p>추천한 글이 없습니다.</p>`;
                return;
            }

            // 글 목록 렌더링
            data.forEach(article => {
                const card = document.createElement('div');
                card.className = 'card mb-3';

                // 긴 내용 잘라내기
                const maxLength = 100;
                const truncatedContent = (article.content.length > maxLength)
                    ? article.content.substring(0, maxLength) + '...'
                    : article.content;

                card.innerHTML = `
                    <div class="card-header">${article.id}</div>
                    <div class="card-body">
                        <h5 class="card-title">${article.title}</h5>
                        <p class="card-text">${truncatedContent}</p>
                        <a href="/articles/${article.id}"
                           class="btn btn-primary">보러가기</a>
                    </div>
                `;
                container.appendChild(card);
            });
        })
        .catch(error => {
            console.error('Error loading recommended articles:', error);
            alert('로그인이 필요합니다.');
            location.replace('/login');
        });
    }

    function fail() {
        alert("데이터를 가져올 수 없습니다. 로그인 페이지로 이동합니다.");
        location.replace('/login');
    }

    // 먼저 httpRequest로 토큰 유효성 검사/재발급 처리
    httpRequest('GET', '/api/user/recommended-articles', null, success, fail);
}

function getCookie(key) {
    let result = null;
    const cookieArr = document.cookie.split(';');
    cookieArr.some(function(item) {
        item = item.trim();
        let dic = item.split('=');
        if (key === dic[0]) {
            result = dic[1];
            return true;
        }
    });
    return result;
}

function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method: method,
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json'
        },
        body: body
    })
    .then(response => {
        if (response.status === 200 || response.status === 201) {
            return success();
        }
        const refresh_token = getCookie('refresh_token');
        if (response.status === 401 && refresh_token) {
            // 토큰 재발급 시도
            return fetch('/api/token', {
                method: 'POST',
                headers: {
                    Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ refreshToken: refresh_token })
            })
            .then(res => {
                if (!res.ok) {
                    throw new Error('Failed to refresh token');
                }
                return res.json();
            })
            .then(result => {
                // 새 토큰 교체
                localStorage.setItem('access_token', result.accessToken);
                // 재귀 호출 → 원래 요청 다시 시도
                return httpRequest(method, url, body, success, fail);
            })
            .catch(error => {
                console.error(error);
                return fail();
            });
        } else {
            return fail();
        }
    })
    .catch(error => {
        console.error(error);
        return fail();
    });
}