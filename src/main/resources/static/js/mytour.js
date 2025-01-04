function fetchMyTour() {
    let currentPage = 1;
    const itemsPerPage = 6;

    function renderPage(data, page) {
        const container = document.getElementById('myTrip-container');
        const pagination = document.getElementById('pagination');

        container.innerHTML = '';
        pagination.innerHTML = '';

        const start = (page - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        const paginatedData = data.slice(start, end);

        // 글이 없을 경우 메시지 표시
        if (data.length === 0) {
            container.innerHTML = `<p>내 여행 일정이 없습니다.</p>`;
            return;
        }

        paginatedData.forEach(tripInfoDtos => {
            const card = document.createElement('div');
            card.className = 'card mb-3 col-md-4';

            card.innerHTML = `
                <div class="card-body">
                    <h5 class="card-title">${tripInfoDtos.areaName}</h5>
                    <p class="card-text">시작일: ${tripInfoDtos.startDate}</p>
                    <p class="card-text">종료일: ${tripInfoDtos.endDate}</p>
                </div>
                <div class="card-footer">
                    <a href="/trip/view/${tripInfoDtos.id}" class="btn btn-primary custom-style">자세히 보기</a>
                </div>
            `;
            container.appendChild(card);
        });

        // Pagination
        const totalPages = Math.ceil(data.length / itemsPerPage);
        for (let i = 1; i <= totalPages; i++) {
            const li = document.createElement('li');
            li.className = `page-item ${i === page ? 'active' : ''}`;
            li.innerHTML = `<a class="page-link" href="#">${i}</a>`;
            li.addEventListener('click', () => {
                renderPage(data, i);
            });
            pagination.appendChild(li);
        }
    }

    fetch('/trip/myTrip', {
        method: 'GET',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
        },
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load myTrip');
        }
        return response.json();
    })
    .then(data => {
        renderPage(data, currentPage);
    })
    .catch(error => {
        console.error('Error loading myTrip:', error);
        alert('로그인이 필요합니다.');
        location.replace('/login');
    });
}

window.onload = fetchMyTour;
