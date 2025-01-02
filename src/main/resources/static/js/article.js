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

        httpRequest('DELETE',`/api/articles/${id}`, null, success, fail);
    });
}

// 수정 기능
const modifyButton = document.getElementById('modify-btn');

if (modifyButton) {
    modifyButton.addEventListener('click', event => {
        let params = new URLSearchParams(location.search);
        let id = params.get('id');

        body = JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        })

        function success() {
            alert('수정 완료되었습니다.');
            location.replace(`/articles/${id}`);
        }

        function fail() {
            alert('수정 실패했습니다.');
            location.replace(`/articles/${id}`);
        }

        httpRequest('PUT',`/api/articles/${id}`, body, success, fail);
    });
}

// 생성 기능
const createButton = document.getElementById('create-btn');

if (createButton) {
    // 등록 버튼을 클릭하면 /api/articles로 요청을 보낸다
    createButton.addEventListener('click', event => {
        body = JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        });
        function success() {
            alert('등록 완료되었습니다.');
            location.replace('/articles');
        };
        function fail() {
            alert('등록 실패했습니다.');
            location.replace('/articles');
        };

        httpRequest('POST','/api/articles', body, success, fail)
    });
}

// 추천기능
// 버튼, 표시 요소 가져오기
const recommendButton = document.getElementById('recommend-btn');
const recommendCount = document.getElementById('recommend-count');
let articleId;
//const articleId = document.getElementById('article-id').value;

window.addEventListener('DOMContentLoaded', () => {
    articleId = document.getElementById('article-id').value;
    const authorEmail = document.querySelector('#author').value;

    // JWT에서 현재 사용자 이메일 추출
    const token = localStorage.getItem('access_token');
    const decodedToken = JSON.parse(atob(token.split('.')[1]));
    const currentUserEmail = decodedToken.sub; // JWT의 sub 필드를 사용해 이메일 추출

    // 수정/삭제 버튼 처리
    const modifyButton = document.getElementById('modify-btn');
    const deleteButton = document.getElementById('delete-btn');

    if (authorEmail == currentUserEmail) { // 작성자가 아니면 버튼 숨기기
    modifyButton.style.display = 'block';
    deleteButton.style.display = 'block';
    }

    // GET /api/articles/{id}로
    // 이미 추천했는지 여부와 현재 추천수를 받아옴
    fetch(`/api/articles/${articleId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
          // 401 등 → 토큰 만료 or 미인증
          return response.text().then(msg => { throw new Error(msg); });
        }
    return response.json();
    })
    .then(data => {
        // 예: { id: 123, title: "...", recommendCount: 5, recommended: true/false }
        if (data.recommendCount !== undefined) {
          recommendCount.textContent = data.recommendCount;
        }
        if (data.recommended === true) {
          recommendButton.classList.add('recommended');
        } else {
          recommendButton.classList.remove('recommended');
        }
    })
    .catch(error => {
        console.error('추천 상태 불러오기 실패:', error);
        // 로그인 유도 or 에러 안내
    });
});

// 버튼이 존재할 경우 이벤트 리스너 등록
if (recommendButton) {
  recommendButton.addEventListener('click', () => {

    // 로그인 여부 확인
    if (!localStorage.getItem("access_token")) {
      alert('로그인이 필요합니다.');
      return;  // 함수 종료, fetch 요청을 하지 않음
    }

    // POST 요청: /api/articles/{id}/recommend
    fetch(`/api/articles/${articleId}/recommend`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem("access_token")}`
      },
      body: null
    })
    .then(response => {
      // 응답이 200~299 이외라면 에러 처리
      if (!response.ok) {
        // 예: 401, 403 등
        return response.text().then(msg => { throw new Error(msg); });
      }
      // 정상 응답이면 JSON 파싱
      return response.json();
    })
    .then(data => {
      // data = { recommendCount, recommended } 형태라고 가정
      console.log('서버 응답:', data);

      // 추천수 업데이트
      recommendCount.textContent = data.recommendCount;

      // recommended 상태면 버튼에 클래스를 추가
      if (data.recommended) {
        recommendButton.classList.add('recommended');
      } else {
        // 취소 상태면 클래스 제거
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
            // 로컬 스토리지에 저장된 액세스 토큰 삭제
            localStorage.removeItem('access_token');

            // 쿠키에 저장된 리프레시 토큰을 삭제
            deleteCookie('refresh_token');
            alert('로그아웃 성공')
            location.replace('/');
        }
        function fail() {
            alert('로그아웃 실패했습니다.');
        }
        httpRequest('DELETE', '/api/refresh-token', null, success, fail);
    });
}

// 쿠키를 삭제하는 함수
function deleteCookie(name) {
    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

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