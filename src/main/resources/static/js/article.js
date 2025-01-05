// 공통 HTTP 요청 함수 정의
function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method: method,
        headers: {
            // 로컬 스토리지에서 액세스 토큰 값을 가져와 헤더에 추가
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json'
        },
        body: body
    }).then(response => {
        if (response.status === 200 || response.status === 201 || response.status === 204) {
            return success();
        }
        const refresh_token = getCookie('refresh_token');
        if (response.status === 401 && refresh_token) {
            // 토큰 재발급 시도
            fetch('/api/token', {
                method: 'POST',
                headers: {
                    Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    refreshToken: refresh_token
                })
            })
            .then(res => {
                if (res.ok) {
                    return res.json();
                }
            })
            .then(result => {
                // 재발급 성공 시 로컬 스토리지 교체 후 요청 재시도
                localStorage.setItem('access_token', result.accessToken);
                httpRequest(method, url, body, success, fail);
            })
            .catch(error => fail());
        } else {
            return fail();
        }
    });
}

// 쿠키 조회
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

// 날짜 형식을 원하는 형태로 변환하는 함수 (예: 2024-05-01 → 2024.05.01)
function formatDate(input) {
    if (!input) return "정보 없음";
    if (typeof input === 'string' && !isNaN(Date.parse(input))) {
        const date = new Date(input);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}.${month}.${day}`;
    }
    return input;
}

// 여행 계획 정보를 불러오는 함수
function fetchContentDetails(contentIdsByDate) {
    const contentDetailsContainer = document.getElementById('trip-details-list');
    if (!contentDetailsContainer) {
        console.error('Element with id "trip-details-list" not found.');
        return; // 요소가 없으면 실행 중단
    }

    Object.entries(contentIdsByDate).forEach(([day, contentIds]) => {
        const daySection = document.createElement('div');
        daySection.classList.add('day-section');
        daySection.innerHTML = `<h4>${day}</h4>`;

        const contentList = document.createElement('div');
        contentList.classList.add('content-list');

        contentIds.forEach(contentId => {
            fetch(`/content/${contentId}`, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
                    'Content-Type': 'application/json',
                }
            })
            .then(response => response.json())
            .then(content => {
                const contentDiv = document.createElement('div');
                contentDiv.classList.add('content-item');
                contentDiv.innerHTML = `
                    <img src="${content.firstimage || '/img/logo.png'}" alt="${content.title}" style="width: 100px; height: auto;">
                    <h5>${content.title}</h5>
                    <p>${content.addr1 || "정보 없음"}</p>
                    <a href="${content.homepage || '#'}" target="_blank">홈페이지</a>
                    <button class="btn btn-primary btn-sm" onclick="showContentDetails('${content.contentid}')">관광지 정보 상세 보기</button>
                `;
                contentList.appendChild(contentDiv);
            })
            .catch(error => {
                console.error(`Failed to fetch content details for ID: ${contentId}`, error);
            });
        });

        daySection.appendChild(contentList);
        contentDetailsContainer.appendChild(daySection);
    });
}


function showContentDetails(contentId) {
    fetch(`/content/${contentId}`, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(content => {
        const modalBody = document.getElementById('modal-body');
        modalBody.innerHTML = `
            <h5>${content.title}</h5>
            <p>${content.overview || "정보 없음"}</p>
            <img src="${content.firstimage || '/img/logo.png'}" alt="${content.title}" style="width: 100%;">
            <p>주소: ${content.addr1 || "정보 없음"}</p>
            <p>전화번호: ${content.tel || "정보 없음"}</p>
            <a href="${content.homepage || '#'}" target="_blank">홈페이지</a>
        `;
        const modal = new bootstrap.Modal(document.getElementById('contentDetailsModal'));
        modal.show();
    })
    .catch(error => {
        console.error('Failed to fetch content details:', error);
    });
}




// 페이지 로드 시 여행 계획 불러오기
window.addEventListener('DOMContentLoaded', () => {
    let id = document.getElementById('article-id').value;

    fetch(`/api/articles/${id}/tripinfo`, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.contentIdsByDate) {
            fetchContentDetails(data.contentIdsByDate);
        } else {
            console.error('No contentIdsByDate found in response');
        }
    })
    .catch(error => {
        console.error('Failed to fetch trip info:', error);
    });
});


// 작성일 계산 함수
const timeElements = document.querySelectorAll(".relative-time");
timeElements.forEach((el) => {
    const createdAt = new Date(el.getAttribute("data-created-at"));
    const now = new Date();
    const diffInMinutes = Math.floor((now - createdAt) / 60000); // 차이를 분 단위로 계산

    let relativeTime;
    if (diffInMinutes < 1) {
        relativeTime = "방금 전";
    } else if (diffInMinutes < 60) {
        relativeTime = `${diffInMinutes}분 전`;
    } else if (diffInMinutes < 1440) {
        relativeTime = `${Math.floor(diffInMinutes / 60)}시간 전`;
    } else if (diffInMinutes < 10080) {
        relativeTime = `${Math.floor(diffInMinutes / 1440)}일 전`;
    } else {
        relativeTime = createdAt.toLocaleDateString(); // 날짜로 표시
    }

    el.textContent = relativeTime;
});

// 삭제 기능
const deleteButton = document.getElementById('delete-btn');
if (deleteButton) {
    deleteButton.addEventListener('click', event => {
        let id = document.getElementById('article-id').value;
        if (!confirm('정말 삭제하시겠습니까?')) {
            return;
        }
        function success() {
            alert('삭제가 완료되었습니다.');
            location.replace('/articles');
        }

        function fail() {
            alert('삭제 실패했습니다.');
            location.replace('/articles');
        }

        httpRequest('DELETE', `/api/articles/${id}`, null, success, fail);
    });
}

// 수정 기능
const modifyButton = document.getElementById('modify-btn');
if (modifyButton) {
    modifyButton.addEventListener('click', event => {
        // 수정할 내용 가져오기
        const id = document.getElementById('article-id').value;  // 글 ID 가져오기
        const title = document.getElementById('title').value;  // 제목
        const content = document.getElementById('content').value;  // 내용
        const tripInfoId = document.getElementById('tripInfo').value; // tripInfo 값 가져오기

        // 수정할 내용이 없다면, 경고를 띄우고 종료
        if (!title || !content || !tripInfoId) {
            alert('제목, 내용, 여행 정보(ID)를 입력해주세요.');
            return;
        }

        const body = JSON.stringify({
            title: title,
            content: content,
            tripInfoId: tripInfoId // tripInfoId도 함께 보내기
        });

        // success, fail 함수 정의
        function success() {
            alert('수정 완료되었습니다.');
            location.replace(`/articles/${id}`);  // 수정 완료 후 해당 게시글 페이지로 이동
        }

        function fail() {
            alert('수정 실패했습니다.');
        }

        // 공통 HTTP 요청 함수로 수정 요청
        httpRequest('PUT', `/api/articles/${id}`, body, success, fail);
    });
}

// 로그인을 안하면 글 등록 버튼을 누르면 로그인 페이지로 이동
const createButton = document.getElementById('create-btn');
if (createButton) {
    createButton.addEventListener('click', event => {
        // 로그인 여부 확인
        const token = localStorage.getItem('access_token');
        if (!token) {
            alert('로그인이 필요합니다.');
            location.href = '/login'; // 로그인 페이지로 이동
            return;
        }

        // 로그인 되어 있는 경우, 새 글 작성 페이지로 이동
        location.href = '/new-article';
    });
}

// 생성 기능
const createArticleButton = document.getElementById('createArticle-btn');
if (createArticleButton) {
    // 등록 버튼을 클릭하면 /api/articles로 요청
    createArticleButton.addEventListener('click', event => {
        let body = JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value,
            tripInfoId: parseInt(document.getElementById('tripInfo').value, 10) // 숫자로 변환
        });

        function success() {
            alert('등록 완료되었습니다.');
            location.replace('/articles');
        }

        function fail() {
            alert('등록 실패했습니다.');
            location.replace('/articles');
        }

        httpRequest('POST', '/api/articles', body, success, fail);
    });
}

// 추천 기능
const recommendButton = document.getElementById('recommend-btn');
const recommendCount = document.getElementById('recommend-count');
let articleId;

window.addEventListener('DOMContentLoaded', () => {
    articleId = document.getElementById('article-id').value;
    const authorEmail = document.querySelector('#author').value;

    // 로그인 토큰 확인
    const token = localStorage.getItem('access_token');

    let currentUserEmail = null;
    // 토큰이 있으면 이메일 추출
    if (token) {
        const decodedToken = JSON.parse(atob(token.split('.')[1]));
        currentUserEmail = decodedToken.sub; // JWT의 sub 필드를 사용해 이메일 추출
    }

    // 수정/삭제 버튼 처리
    const modifyButton = document.getElementById('modify-btn');
    const deleteButton = document.getElementById('delete-btn');

    // 작성자일 경우만 버튼 표시
    if (authorEmail === currentUserEmail) {
            modifyButton.style.display = 'block';
            deleteButton.style.display = 'block';
        } else {
            modifyButton.style.display = 'none';
            deleteButton.style.display = 'none';
        }
    // GET /api/articles/{id} 요청 시, 토큰이 없으면 Authorization 헤더 생략
    let headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // 이미 추천했는지 여부와 현재 추천수를 받아옴
    fetch(`/api/articles/${articleId}`, {
        method: 'GET',
        headers: headers
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(msg => { throw new Error(msg); });
        }
        return response.json();
    })
    .then(data => {
        if (data.recommendCount !== undefined) {
            recommendCount.textContent = data.recommendCount;
        }
        if (data.recommended) {
            recommendButton.classList.add('recommended');
        } else {
            recommendButton.classList.remove('recommended');
        }
    })
    .catch(error => {
        console.error('추천 상태 불러오기 실패:', error);
    });
});

// 버튼이 존재할 경우 이벤트 리스너 등록
if (recommendButton) {
    recommendButton.addEventListener('click', () => {
        if (!localStorage.getItem('access_token')) {
            alert('로그인이 필요합니다.');
            return; // 비로그인 시에는 추천 기능 동작 안 함
        }

        fetch(`/api/articles/${articleId}/recommend`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem("access_token")}`
            },
            body: null
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(msg => { throw new Error(msg); });
            }
            return response.json();
        })
        .then(data => {
            recommendCount.textContent = data.recommendCount;
            if (data.recommended) {
                recommendButton.classList.add('recommended');
            } else {
                recommendButton.classList.remove('recommended');
            }
        })
        .catch(error => {
            console.error('추천/취소 요청 실패:', error);
            alert('추천/취소에 실패했습니다.');
        });
    });
}

// 로그아웃
const logoutButton = document.getElementById('logout-btn');
if (logoutButton) {
    logoutButton.addEventListener('click', event => {
        function success() {
            localStorage.removeItem('access_token');
            deleteCookie('refresh_token');
            alert('로그아웃 성공');
            location.replace('/');
        }
        function fail() {
            alert('로그아웃 실패했습니다.');
        }
        httpRequest('DELETE', '/api/refresh-token', null, success, fail);
    });
}

// 쿠키 삭제
function deleteCookie(name) {
    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}
