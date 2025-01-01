const articleId = document.getElementById('article-id').value;
const repliesList = document.getElementById('replies-list');
const pagination = document.getElementById('pagination');
const replyContent = document.getElementById('reply-content');

let currentPage = 0;

// 댓글 로드 함수
function loadReplies(page = 0) {
    fetch(`/api/replies/article/${articleId}?page=${page}&size=10`, {
        method: 'GET',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
        },
    })
    .then(response => response.json())
    .then(data => {
        // 댓글 렌더링
        repliesList.innerHTML = '';
        data.content.forEach(reply => {
            const replyElement = document.createElement('div');
//            const formattedDate= reply.createAt ? new Date(reply.createdAt).toLocaleString() : "invalidDate"
            replyElement.className = 'border p-3 mb-2';
            replyElement.innerHTML = `
                <p id="comment-text-${reply.replyId}">${reply.reply}</p>
                <small class="text-muted">${reply.replyer} | ${new Date(reply.createdAt).toLocaleString()}</small>
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
    const newContent = prompt('댓글을 수정하세요:', currentContent);
    if (newContent !== null && newContent.trim()) {
        const body = JSON.stringify({ reply: newContent });
        const url = `/api/replies/${replyId}`;

        httpRequest('PUT', url, body, () => {
            alert('댓글이 수정되었습니다.');
            loadReplies(currentPage);
        }, () => {
            alert('댓글 수정에 실패했습니다.');
        });
    }
};

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