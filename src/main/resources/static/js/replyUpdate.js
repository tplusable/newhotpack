const repliesList = document.getElementById('replies-list');
const pagination = document.getElementById('pagination');
const replyContent = document.getElementById('reply-content');

let currentPage = 0;

// 댓글 렌더링 함수
function renderReply(reply, containerId) {
    const container =document.getElementById(containerId);
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
            <span id="like-count-${reply.replyId}">추천 수: ${reply.totalLikes || 0 }</span>
            <button class="btn btn-link btn-sm text-info" onclick="showLikers(${reply.replyId})">추천자 목록</button>
        </div>
        ${reply.isAuthor ? `
        <div>
            <button class="btn btn-link btn-sm text-primary" onclick="editReply(${reply.replyId}, '${reply.reply}', '${containerId}')">수정</button>
            <button class="btn btn-link btn-sm text-danger" onclick="deleteReply(${reply.replyId})">삭제</button>
        </div>` : ''}
    `;
    container.appendChild(replyElement);
}


// 댓글 로드 함수
function loadReplies(page = 0) {
    articleId = document.getElementById('article-id').value;
    fetch(`/replies/article/${articleId}?page=${page}&size=10`, {
        method: 'GET',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
        },
    })
    .then(response => response.json())
    .then(data => {
        // 댓글 렌더링
        const repliesList = document.getElementById('replies-list');
        repliesList.innerHTML = '';
        data.content.forEach(reply => {
            renderReply(reply, 'replies-list');
        });

        // 페이지네이션 렌더링
        pagination.innerHTML = '';
        const paginationList =document.createElement('ul');
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
loadReplies();

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
    const editButton = document.querySelector(`button[onclick="editReply(${replyId}, '${currentContent}', '${containerId}')"]`);
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

//추천 상위 댓글을 가져와서 표시하는 함수
function loadTopReplies() {
    const articleId = document.getElementById('article-id').value;
    fetch(`/replies/article/${articleId}/top-replies?limit=1`, {
        method: 'GET',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
        },
    })
    .then(response => response.json())
    .then(data => {
        console.log("data:", data);
        const topRepliesSection = document.getElementById('top-replies');
        topRepliesSection.innerHTML = '<h4>추천 많은 댓글</h4>';

        data.forEach(reply => {
            renderReply(reply, 'top-replies');
        });
    })
    .catch(error => console.error('Error fetching top replies:', error));
}

// 페이지 로드 시 상위 댓글 불러오기
document.addEventListener('DOMContentLoaded', () => {
    loadReplies();
    loadTopReplies();
});