//// 버튼 클릭 이벤트 설정
//document.getElementById("getMyReplies-btn").addEventListener("click", fetchMyReplies);
//
//function fetchMyReplies() {
//    function success() {
//        fetch('/api/user/my-replies', {
//            method: 'GET',
//            headers: {
//                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
//                'Content-Type': 'application/json',
//            },
//        })
//        .then(response => {
//            if (!response.ok) {
//                if (response.status === 401) {
//                    throw new Error('Unauthorized: 로그인 필요');
//                }
//                throw new Error('Failed to load replies');
//            }
//            return response.json();
//        })
//        .then(data => {
//            const container = document.querySelector('.app-view-content');
//
//            if (data.length === 0) {
//                container.innerHTML = `<p>작성한 댓글이 없습니다.</p>`;
//                return;
//            }
//
//            let repliesHTML = '';
//            data.forEach(reply => {
//                repliesHTML += `
//                    <div class="card mb-3">
//                        <div class="card-header">댓글 ID: ${reply.replyId}</div>
//                        <div class="card-body">
//                            <p class="card-text">${reply.reply}</p>
//                            <p class="card-text"><small class="text-muted">작성일: ${new Date(reply.createdAt).toLocaleString()}</small></p>
//                            <p class="card-text"><small class="text-muted">추천 수: ${reply.totalLikes}</small></p>
//                        </div>
//                    </div>
//                `;
//            });
//
//            container.innerHTML = repliesHTML;
//        })
//        .catch(error => {
//            console.error('Error loading replies: ', error.message);
//            if (error.message.includes('Unauthorized')) {
//                alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
//                location.replace('/login');
//            } else {
//                alert(error.message || '댓글을 불러오는데 실패했습니다. 다시 시도해주세요.');
//            }
//        });
//    }
//
//    function fail() {
//        alert("데이터를 가져올 수 없습니다. 로그인 페이지로 이동합니다.");
//        location.replace('/login');
//    }
//
//    // HTTP 요청 전에 토큰 유효성 검사 및 재발급 처리
//    httpRequest('GET', '/api/user/my-replies', null, success, fail);
//}
//
//// 쿠키 조회 함수
//function getCookie(key) {
//    let result = null;
//    const cookies = document.cookie.split(';');
//    cookies.some(item => {
//        const pair = item.trim().split('=');
//        if (pair[0] === key) {
//            result = pair[1];
//            return true;
//        }
//    });
//    return result;
//}
//
//// 공통 HTTP 요청 함수
//function httpRequest(method, url, body, success, fail) {
//    fetch(url, {
//        method: method,
//        headers: {
//            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
//            'Content-Type': 'application/json',
//        },
//        body: body,
//    })
//    .then(response => {
//        if (response.status === 200 || response.status === 201) {
//            return success(response);
//        }
//        if (response.status === 401) {
//            const refreshToken = getCookie('refresh_token');
//            if (refreshToken) {
//                // 토큰 재발급 요청
//                return fetch('/api/token', {
//                    method: 'POST',
//                    headers: {
//                        'Content-Type': 'application/json',
//                    },
//                    body: JSON.stringify({ refreshToken: refreshToken }),
//                })
//                .then(res => {
//                    if (!res.ok) {
//                        throw new Error('Failed to refresh token');
//                    }
//                    return res.json();
//                })
//                .then(result => {
//                    localStorage.setItem('access_token', result.accessToken);
//                    return httpRequest(method, url, body, success, fail); // 재시도
//                })
//                .catch(error => {
//                    console.error('Token refresh failed: ', error);
//                    alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
//                    location.replace('/login');
//                });
//            } else {
//                alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
//                location.replace('/login');
//                return fail();
//            }
//        }
//        if (response.status >= 500) {
//            alert('서버 오류가 발생했습니다. 관리자에게 문의하세요.');
//        }
//        return fail();
//    })
//    .catch(error => {
//        console.error('Request failed: ', error);
//        return fail();
//    });
//}