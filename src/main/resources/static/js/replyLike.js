// 댓글 추천 토글
async function toggleLike(replyId) {
    try {
        const response = await fetch(`/api/replies/${replyId}/like`, {
            method: 'POST',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                'Content-Type': 'application/json',
            },
        });

        if (response.ok) {
            const data = await response.json();
            updateLikeUI(replyId, data.totalLikes, data.liked);
        } else {
            alert('추천 처리에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error toggling like:', error);
        alert('추천 처리 중 오류가 발생했습니다.');
    }
}

// 추천자 목록 조회
async function showLikers(replyId) {
    try {
        const response = await fetch(`/api/replies/${replyId}/likers`, {
            method: 'GET',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            },
        });

        if (response.ok) {
            const likers = await response.json();
            alert(`추천한 사용자 목록: \n${likers.join(', ')}`);
        } else {
            alert('추천자 목록 조회에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error fetching likers:', error);
        alert('추천자 목록 조회 중 오류가 발생했습니다.');
    }
}

// UI 업데이트 함수
function updateLikeUI(replyId, totalLikes, liked) {
    const likeButton = document.querySelector(`#like-button-${replyId}`);
    const likeCount = document.querySelector(`#like-count-${replyId}`);

    likeButton.textContent = liked ? '❤️ 추천 취소' : '🤍 추천';
    likeCount.textContent = `추천 수: ${totalLikes}`;
}

// 댓글 렌더링 함수에서 추천 버튼 추가
function renderReply(reply) {
    const replyElement = document.createElement('div');
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
    `;

    return replyElement;
}