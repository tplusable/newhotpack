document.addEventListener('DOMContentLoaded', () => {
    // 버튼 클릭 이벤트 설정
    const myRepliesButton = document.getElementById('getMyReplies-btn');
    if (myRepliesButton) {
        myRepliesButton.addEventListener('click', fetchMyReplies);
    } else {
        console.error("Button with ID 'getMyReplies-btn' not found.");
    }
});

/**
 * Fetch 요청을 보내며 토큰 유효성 검사를 수행
 */
function fetchWithToken(url, method, body = null) {
    return new Promise((resolve, reject) => {
        httpRequest(method, url, body, resolve, reject);
    });
}

/**
 * 공통 HTTP 요청 함수
 */
function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method: method,
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json',
        },
        body: body ? JSON.stringify(body) : null,
    })
    .then(response => {
        if (response.ok) {
            return response.json().then(success);
        }
        if (response.status === 401) {
            const refreshToken = getCookie('refresh_token');
            if (refreshToken) {
                // 토큰 재발급 요청
                return fetch('/api/token', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ refreshToken }),
                })
                .then(res => {
                    if (!res.ok) {
                        throw new Error('Failed to refresh token');
                    }
                    return res.json();
                })
                .then(result => {
                    // 새 액세스 토큰 저장
                    localStorage.setItem('access_token', result.accessToken);
                    // 재시도
                    return httpRequest(method, url, body, success, fail);
                })
                .catch(error => {
                    console.error('Token refresh failed:', error);
                    alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
                    location.replace('/login');
                });
            } else {
                alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
                location.replace('/login');
                fail(new Error('Unauthorized: 로그인 필요'));
            }
        } else if (response.status >= 500) {
            alert('서버 오류가 발생했습니다. 관리자에게 문의하세요.');
            fail(new Error('서버 오류'));
        } else {
            response.text().then(text => fail(new Error(text || 'Unknown error')));
        }
    })
    .catch(error => fail(error));
}

/**
 * 내가 쓴 댓글을 가져와 화면에 렌더링
 */
function fetchMyReplies() {
    fetchWithToken('/api/user/my-replies', 'GET', null)
        .then(data => {
            renderReplies(data);
        })
        .catch(error => {
            console.error('Error loading replies:', error.message);
            if (error.message.includes('Unauthorized')) {
                alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
                location.replace('/login');
            } else {
                alert(error.message || '댓글을 불러오는데 실패했습니다. 다시 시도해주세요.');
            }
        });
}

/**
 * 댓글 데이터를 화면에 렌더링
 */
function renderReplies(replies) {
    const container = document.querySelector('.app-view-content');

    if (!replies || replies.length === 0) {
        container.innerHTML = `<p>작성한 댓글이 없습니다.</p>`;
        return;
    }

    const repliesHTML = replies.map(reply => `
        <div class="card mb-3">
            <div class="card-header">댓글 ID: ${reply.replyId}</div>
            <div class="card-body">
                <p class="card-text">${reply.reply}</p>
                <p class="card-text"><small class="text-muted">작성일: ${new Date(reply.createdAt).toLocaleString()}</small></p>
                <p class="card-text"><small class="text-muted">추천 수: ${reply.totalLikes}</small></p>
            </div>
        </div>
    `).join('');
    container.innerHTML = repliesHTML;
}

/**
 * 쿠키 값 가져오기
 */
function getCookie(key) {
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
        const [k, v] = cookie.trim().split('=');
        if (k === key) return v;
    }
    return null;
}