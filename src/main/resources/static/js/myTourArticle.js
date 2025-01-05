// 페이지 로드 시 여행 목록 가져오기
document.addEventListener('DOMContentLoaded', fetchMyTourList);

// 쿠키 조회 함수 (토큰 재발급 시 필요)
function getCookie(key) {
    let result = null;
    const cookies = document.cookie.split(';');
    cookies.some(cookie => {
        const [k, v] = cookie.trim().split('=');
        if (k === key) {
            result = decodeURIComponent(v);
            return true;
        }
        return false;
    });
    return result;
}

// 공통 HTTP 요청 함수
function httpRequestTour(method, url, body, success, fail) {
    const headers = {
        'Content-Type': 'application/json',
    };

    const accessToken = localStorage.getItem('access_token');
    if (accessToken) {
        headers['Authorization'] = 'Bearer ' + accessToken;
    }

    fetch(url, {
        method: method,
        headers: headers,
        body: body ? JSON.stringify(body) : null
    })
        .then(async response => {
            if (response.status === 401) {
                const refreshToken = getCookie('refresh_token');
                if (refreshToken) {
                    const tokenResponse = await fetch('/api/token', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ refreshToken: refreshToken })
                    });

                    if (tokenResponse.ok) {
                        const tokenData = await tokenResponse.json();
                        localStorage.setItem('access_token', tokenData.accessToken);
                        headers['Authorization'] = 'Bearer ' + tokenData.accessToken;
                        return fetch(url, {
                            method: method,
                            headers: headers,
                            body: body ? JSON.stringify(body) : null
                        });
                    } else {
                        fail(new Error('Failed to refresh token'));
                        return;
                    }
                } else {
                    fail(new Error('No refresh token available'));
                    return;
                }
            }

            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
        })
        .then(data => {
            if (data) {
                success(data);
            }
        })
        .catch(error => {
            fail(error);
        });
}

// 여행 목록 가져오기
function fetchMyTourList() {
    httpRequestTour('GET', '/trip/myTrip', null, successLoadMyTrip, failLoadMyTrip);
}

function successLoadMyTrip(data) {
    const select = document.getElementById('tripInfo');
    select.innerHTML = '<option value="" disabled selected>여행을 선택하세요</option>';

    data.forEach(tripInfoDto => {
        const option = document.createElement('option');
        option.value = tripInfoDto.id;
        option.textContent = `나의 ${tripInfoDto.areaName} 여행 (${tripInfoDto.startDate} ~ ${tripInfoDto.endDate})`;
        select.appendChild(option);
    });

    select.addEventListener('change', function () {
        const selectedId = this.value;
        if (selectedId) {
            fetchMyTripDetails(selectedId);
        }
    });
}

function failLoadMyTrip(error) {
    console.error('Error loading myTrip:', error);
    alert('여행 목록을 불러오는 데 실패했습니다.');
}

// 특정 여행 정보 가져오기
function fetchMyTripDetails(id) {
    httpRequestTour('GET', `/trip/${id}`, null, successLoadMyTripDetails, failLoadMyTripDetails);
}

function successLoadMyTripDetails(tripInfo) {
    const detailsContainer = document.getElementById('trip-details');
    detailsContainer.innerHTML = ''; // 기존 내용 초기화

    if (!tripInfo) {
        detailsContainer.innerHTML = '<p>여행 정보를 불러올 수 없습니다.</p>';
        return;
    }

    detailsContainer.innerHTML = `
        <h2>${tripInfo.areaName} 여행</h2>
        <p>기간: ${formatDateMyTour(tripInfo.startDate)} - ${formatDateMyTour(tripInfo.endDate)}</p>
        <p>생성일: ${formatDateMyTour(tripInfo.createdAt)}</p>
        <h3>여행 일정</h3>
    `;

    // 콘텐츠 리스트 표시
    fetchMyContentDetails(tripInfo.contentIdsByDate);
}

function failLoadMyTripDetails(error) {
    console.error('Error loading trip details:', error);
    alert('여행 정보를 불러오는 데 실패했습니다.');
}

// 콘텐츠 상세 정보 가져오기 및 표시
function fetchMyContentDetails(contentIdsByDate) {
    const detailsContainer = document.getElementById('trip-details');

    Object.entries(contentIdsByDate).forEach(([day, contentIds]) => {
        const daySection = document.createElement('div');
        daySection.classList.add('day-section');
        daySection.innerHTML = `<h4>${day}</h4>`;

        const contentList = document.createElement('div');
        contentList.classList.add('content-list');

        contentIds.forEach(contentId => {
            httpRequestTour('GET', `/content/${contentId}`, null, content => {
                const contentDiv = document.createElement('div');
                contentDiv.classList.add('content-item');
                contentDiv.innerHTML = `
                    <h5>${content.title}</h5>
                    <p>${content.addr1 || '주소 정보 없음'}</p>
                    <button class="btn btn-primary btn-sm" onclick="showMyContentDetails('${content.contentid}')">상세 보기</button>
                `;
                contentList.appendChild(contentDiv);
            }, error => {
                console.error(`Error fetching content ID ${contentId}:`, error);
            });
        });

        daySection.appendChild(contentList);
        detailsContainer.appendChild(daySection);
    });
}

// 콘텐츠 상세 정보 보기
function showMyContentDetails(contentId) {
    httpRequestTour('GET', `/content/${contentId}`, null, content => {
        const modalBody = document.getElementById('modal-body');
        modalBody.innerHTML = `
            <h5>${content.title}</h5>
            <p>${content.overview || '정보 없음'}</p>
            <p>주소: ${content.addr1 || '정보 없음'}</p>
            <img src="${content.firstimage || '/img/logo.png'}" alt="${content.title}" style="width: 100%; height: auto;">
        `;
        const modal = new bootstrap.Modal(document.getElementById('contentDetailsModal'));
        modal.show();
    }, error => {
        console.error('Error loading content details:', error);
    });
}

// 날짜 포맷 함수
function formatDateMyTour (input) {
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