function fetchMyTour() {
    function success() {
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
            const container = document.getElementById('myTrip-container');

            // 글이 없을 경우 메시지 표시
            if (data.length === 0) {
                container.innerHTML = `<p>내 여행 일정이 없습니다.</p>`;
                return;
            }

            data.forEach(tripInfoDtos => {
                const card = document.createElement('div');
                card.className = 'card mb-3';

                card.innerHTML  = `
                    <div class="card-body">
                        <h5 class="card-title">${tripInfoDtos.areaName}</h5>
                        <p class="card-text">여행 지역: ${tripInfoDtos.areaName}</p>
                        <p class="card-text">시작일: ${tripInfoDtos.startDate}</p>
                        <p class="card-text">종료일: ${tripInfoDtos.endDate}</p>
                    </div>
                    <div class="card-footer">
                        <a href="/trip/view/${tripInfoDtos.id}" class="btn btn-primary">자세히 보기</a>
                    </div>
                `;
                container.appendChild(card);
            });
        })
        .catch(error => {
            console.error('Error loading myTrip:', error);
            alert('로그인이 필요합니다.');
            location.replace('/login');
        });
    }
    function fail() {
        alert("데이터를 가져올 수 없습니다. 로그인 페이지로 이동합니다.");
        location.replace('/login');
    }
    httpRequest('GET', '/trip/myTrip', null, success, fail);
}

window.onload = fetchMyTour;

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

