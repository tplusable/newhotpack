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
  // GET /api/articles/{id} (또는 /recommend/status 같은 API)로
  // 이미 추천했는지 여부와 현재 추천수를 받아옴
  fetch(`/api/articles/${articleId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem("access_token")}`
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
        if (response.status === 200 || response.status === 201 || response.status === 204) {
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

//const articleId=document.getElementById('article-id').value;
//  const repliesList=document.getElementById('replies-list');
//  const pagination=document.getElementById('pagination');
//  const replyContent=document.getElementById('reply-content');
//
//  let currentPage =0;
//
//  // Load replies for the current page
//  function loadReplies(page=0) {
//    fetch(`/api/replies/articles/${articleId}?page=${page}&size=10`)
//      .then(response => response.json())
//      .then(data => {
//        //Render replies
//        repliesList.innerHTML='';
//        data.content.forEach(reply=>{
//          const replyElement=document.createElement('div');
//          replyElement.className='border p-3 mb-2';
//          replyElement.innerHTML=`
//            <p id="comment-text-${reply.replyId}">${reply.reply}</p>
//            <small class="text-muted">${reply.replyer} | ${new Date(reply.createAt).toLocaleString()}</small>
//            <div>
//              <button class="btn btn-link btn-sm text-primary" onclick="editReply(${reply.replyId},'${reply.reply}')">수정</button>
//              <button class="btn btn-link btn-sm text-danger" onclick="deleteReply(${reply.replyId})">삭제</button>
//            </div>
//          `;
//          repliesList.appendChild(replyElement);
//        });
//
//        //Render pagination
//        pagination.innerHTML='';
//        for (let i =0; i<data.totalPages; i++) {
//          const pageItem=document.createElement('li');
//          pageItem.className='page-item';
//          if (i===page) pageItem.classList.add('active');
//          pageItem.innerHTML=`<a class="page-link" href="#">${i+1}</a>`;
//          pageItem.addEventListener('click', (e) => {
//            e.preventDefault();
//            loadReplies(i);
//          });
//          pagination.querySelector('.pagination').appendChild(pageItem);
//        }
//      });
//  }
//
//  //Add a new comment
//  document.getElementById('submit-reply').addEventListener('click', () => {
//    const content=replyContent.value;
//    if(!content.trim()) {
//      alert('댓글 내용을 입력하세요.');
//      return;
//    }
//
//    //POST 요청의 URL
//    const url =`/api/replies/article/${articleId}`;
//    // 요청 본문 데이터
//    const body=JSON.stringify({ articleId: articleId, reply: content });
//
//    // 성공 시 처리 로직
//    const success=()=>{
//      replyContent.value='';  // Clear the textarea
//      loadReplies(); // Reload replies
//    }
//
//    // 실패시 처리 로직
//    const fail =() =>{
//      alert ('댓글 작성에 실패했습니다.');
//    };
//
//    //httpRequest를 사용하여 요청 보내기
//    httpRequest('POST', url, body, success, fail);
//  });
//
const repliesList = document.getElementById('replies-list');
const pagination = document.getElementById('pagination');
const replyContent = document.getElementById('reply-content');

let currentPage = 0;

// 댓글 로드 함수
function loadReplies(page = 0) {
    fetch(`/replies/article/${articleId}?page=${page}&size=10`, {
        method: 'GET',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
        },
    })
    .then(response => response.json())
    .then(data => {
        // 댓글 렌더링
        console.log(data);
        repliesList.innerHTML = '';
        data.content.forEach(reply => {
            const replyElement = document.createElement('div');
//            const formattedDate= reply.createAt ? new Date(reply.createdAt).toLocaleString() : "invalidDate"
            replyElement.className = 'border p-3 mb-2';
            replyElement.innerHTML = `
                <p id="reply-text-${reply.replyId}">${reply.reply}</p>
                <small class="text-muted">${reply.replyer} | ${new Date(reply.createdAt).toLocaleString()}</small>
                <div>
                    <button class="btn btn-link btn-sm text-primary" id="like-button-${reply.replyId}" onclick="toggleLike(${reply.replyId})">
                        ${reply.liked ? '❤️ 추천 취소' : '🤍 추천'}
                    </button>
                    <span id="like-count-${reply.replyId}">추천 수: ${reply.totalLikes}</span>
                    <button class="btn btn-link btn-sm text-info" onclick="showLikers(${reply.replyId})">추천자 목록</button>
                </div>
                <div>
                    <button class="btn btn-link btn-sm text-primary" onclick="editReply(${reply.replyId}, '${reply.reply}')">수정</button>
                    <button class="btn btn-link btn-sm text-danger" onclick="deleteReply(${reply.replyId})">삭제</button>
                </div>
            `;
            repliesList.appendChild(replyElement);
        });

        // 페이지네이션 렌더링
        pagination.innerHTML = '';
        paginationList =document.createElement('ul');
        paginationList.className= 'pagination';
        pagination.appendChild(paginationList);

        for (let i = 0; i < data.totalPages; i++) {
            const pageItem = document.createElement('li');
            pageItem.className = 'page-item';
            if (i === page) pageItem.classList.add('active');
            pageItem.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;
            pageItem.addEventListener('click', (e) => {
                e.preventDefault();
                loadReplies(i);
            });
            paginationList.appendChild(pageItem);
        }
    });
}

// 초기 댓글 로드
document.addEventListener('DOMContentLoaded', () => {
    articleId = document.getElementById('article-id').value;

    // 댓글 로드
    loadReplies();
});

// 댓글 추가 함수
document.getElementById('submit-reply').addEventListener('click', () => {
    const content = replyContent.value;
    if (!content.trim()) {
        alert('댓글 내용을 입력하세요.');
        return;
    }

    const body = JSON.stringify({ articleId: articleId, reply: content });
    const url = `/api/replies/article/${articleId}`;

    httpRequest('POST', url, body, () => {
        replyContent.value = ''; // 텍스트 박스 초기화
        loadReplies(); // 댓글 새로 로드
    }, () => {
        alert('댓글 작성에 실패했습니다.');
    });
});

// 댓글 수정 함수
window.editReply = (replyId, currentContent) => {
    const replyText = document.getElementById(`reply-text-${replyId}`);
    const editButton = document.querySelector(`button[onclick="editReply(${replyId}, '${currentContent}')"]`);
    const deleteButton = document.querySelector(`button[onclick="deleteReply(${replyId})"]`);

    // 이미 수정 중이면 return
    if (editButton.textContent === "수정완료") return;

    // 댓글 텍스트를 텍스트 박스로 변경
    const textarea = document.createElement('textarea');
    textarea.className = "form-control mb-2";
    textarea.value = currentContent;
    textarea.id = `textarea-${replyId}`;

    replyText.replaceWith(textarea);

    // "수정" 버튼을 "수정완료" 버튼으로 변경
    editButton.textContent = "수정완료";
    deleteButton.style.display = "none"; // 삭제 버튼 숨김

    // 수정 완료 버튼 클릭 이벤트 추가
    const onEditComplete = () => {
        const newContent = textarea.value.trim();
        if (!newContent) {
            alert("댓글 내용을 입력하세요.");
            return;
        }

        const body = JSON.stringify({ reply: newContent });
        const url = `/api/replies/${replyId}`;

        // HTTP 요청
        httpRequest('PUT', url, body, () => {
            alert('댓글이 수정되었습니다.');

            // 텍스트 영역을 원래 텍스트로 되돌림
            const updatedReply = document.createElement('p');
            updatedReply.id = `reply-text-${replyId}`;
            updatedReply.textContent = newContent;

            textarea.replaceWith(updatedReply);

            // 버튼 원래 상태로 복구
            editButton.textContent = "수정";
            deleteButton.style.display = "inline"; // 삭제 버튼 다시 표시

            // 수정 완료 후 이벤트 리스너 제거
            editButton.removeEventListener('click', onEditComplete);
        }, () => {
            alert('댓글 수정에 실패했습니다.');
        });
    };

    // 수정 완료 버튼 클릭 시 동작 추가
    editButton.addEventListener('click', onEditComplete);
};

//// 댓글 수정 함수
//window.editReply = (replyId, currentContent) => {
//    const newContent = prompt('댓글을 수정하세요:', currentContent);
//    if (newContent !== null && newContent.trim()) {
//        const body = JSON.stringify({ reply: newContent });
//        const url = `/api/replies/${replyId}`;
//
//        httpRequest('PUT', url, body, () => {
//            alert('댓글이 수정되었습니다.');
//            loadReplies(currentPage);
//        }, () => {
//            alert('댓글 수정에 실패했습니다.');
//        });
//    }
//};

// 댓글 삭제 함수
window.deleteReply = (replyId) => {
    if (confirm('댓글을 삭제하시겠습니까?')) {
        const url = `/api/replies/${replyId}`;

        httpRequest('DELETE', url, null, () => {
            alert('댓글이 삭제되었습니다.');
            loadReplies(currentPage);
        }, () => {
            alert('댓글 삭제에 실패했습니다.');
        });
    }
};