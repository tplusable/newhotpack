function fetchMyTourArticle() {
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
                const select = document.getElementById('tripInfo'); // select 요소 가져오기
                // select 초기화
                select.innerHTML = '<option value="" disabled selected>선택하세요</option>';

            // 데이터가 없을 경우 처리
            if (data.length === 0) {
                const option = document.createElement('option');
                option.value = "";
                option.textContent = "여행 정보가 없습니다.";
                option.disabled = true;
                select.appendChild(option);
                return;
            }

            // 데이터 순회하며 옵션 생성
            data.forEach(tripInfoDto => {
                const option = document.createElement('option');
                option.value = tripInfoDto.id;
                option.textContent = `나의 ${tripInfoDto.areaName} 여행 ${tripInfoDto.startDate} - ${tripInfoDto.endDate}`;
                select.appendChild(option);
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

window.onload = fetchMyTourArticle;

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

// 공통 HTTP 요청 함수
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