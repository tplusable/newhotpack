// 삭제 기능
const deleteButton = document.getElementById('delete-btn');

if (deleteButton) {
    deleteButton.addEventListener('click', event => {
        let id = document.getElementById('article-id').value;
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

const logoutButton = document.getElementById('logout-btn');
if (logoutButton) {
    logoutButton.addEventListener('click', event => {
        function success() {
            // 로컬 스토리지에 저장된 액세스 토큰 삭제
            localStorage.removeItem('access_token');

            // 쿠키에 저장된 리프레시 토큰을 삭제
            deleteCookie('refresh_token');
            alert('로그아웃 성공')
            location.replace('/login');
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
