// 버튼 클릭 이벤트 설정
document.getElementById("getMyReplies-btn").addEventListener("click", fetchMyReplies);

/** 내가 쓴 댓글 목록을 가져와 화면에 렌더링 */
function fetchMyReplies() {
    function success() {
        fetch('/api/user/my-replies', {
            method: 'GET',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            },
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load replies');
            }
            return response.json();
        })
        .then(data => {
            const container = document.querySelector('.app-view-content'); // 변경된 부분

            // 댓글이 없을 경우 메시지 표시
            if (data.length === 0) {
                container.innerHTML = `<p>작성한 댓글이 없습니다.</p>`;
                return;
            }

            // 댓글 목록 렌더링
            let repliesHTML = '';
            data.forEach(reply => {
                repliesHTML += `
                    <div class="card mb-3">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span>댓글 ID: ${reply.replyId}</span>
                            <a href="/articles/${reply.articleId}" class="btn btn-link btn-sm">게시물 보기</a> <!-- 게시물로 이동 -->
                        </div>
                        <div class="card-body">
                            <p class="card-text">${reply.reply}</p>
                            <p class="card-text"><small class="text-muted">작성일: ${new Date(reply.createdAt).toLocaleString()}</small></p>
                            <p class="card-text"><small class="text-muted">추천 수: ${reply.totalLikes}</small></p>
                        </div>
                    </div>
                `;
            });

            container.innerHTML = repliesHTML; // 렌더링된 댓글 목록을 app-view-content에 삽입
        })
        .catch(error => {
            console.error('Error loading replies:', error);
            alert('로그인이 필요합니다.');
            location.replace('/login');
        });
    }

    function fail() {
        alert("데이터를 가져올 수 없습니다. 로그인 페이지로 이동합니다.");
        location.replace('/login');
    }

    // 먼저 httpRequest로 토큰 유효성 검사/재발급 처리
    httpRequest('GET', '/api/user/my-replies', null, success, fail);
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