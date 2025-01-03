// myTourArticle.js

// 페이지 로드 시 실행되는 함수
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

// 공통 HTTP 요청 함수 (콜백 기반)
function httpRequest(method, url, body, success, fail) {
    console.log(`API 요청: ${method} ${url}`);
    if (body) {
        console.log('Request Body:', body);
    }

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
                // 토큰 재발급 시도
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
                    // 재발급 받은 토큰으로 원래 요청 재시도
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
            // 응답이 JSON인지 확인
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.indexOf('application/json') !== -1) {
                return response.json();
            } else {
                // JSON이 아닌 경우 에러 처리
                const text = await response.text();
                throw new Error(`Unexpected response type: ${contentType}\nResponse Text: ${text}`);
            }
        } else {
            // 응답이 정상적이지 않은 경우
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status}\nResponse Text: ${errorText}`);
        }
    })
    .then(data => {
        console.log(data);
        if (data) {
            success(data);
        }
    })
    .catch(error => {
        fail(error);
    });
}

// 로딩 스피너 제어 함수
function showLoading() {
    document.getElementById('loading-spinner').style.display = 'block';
}

function hideLoading() {
    document.getElementById('loading-spinner').style.display = 'none';
}

// 날짜 형식을 원하는 형태로 변환하는 함수 (예: 2024-05-01 → 2024.05.01)
function formatDate(input) {
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

// 페이지 로드 시 사용자의 여행 목록을 가져와 select 요소에 추가
function fetchMyTourList() {
    showLoading();

    httpRequest('GET', '/trip/myTrip', null, successLoadMyTrip, failLoadMyTrip);
}

function successLoadMyTrip(data) {
    hideLoading();
    const select = document.getElementById('tripInfo');
    select.innerHTML = '<option value="" disabled selected>선택하세요</option>';

    if (data.length === 0) {
        const option = document.createElement('option');
        option.value = "";
        option.textContent = "여행 정보가 없습니다.";
        option.disabled = true;
        select.appendChild(option);
        return;
    }

    data.forEach(tripInfoDto => {
        const option = document.createElement('option');
        option.value = tripInfoDto.id;
        option.textContent = `나의 ${tripInfoDto.areaName} 여행 ${formatDate(tripInfoDto.startDate)} - ${formatDate(tripInfoDto.endDate)}`;
        select.appendChild(option);
    });

    // 선택 시 상세 정보를 표시하는 이벤트 리스너 추가
    select.addEventListener('change', function() {
        const selectedId = this.value;
        if (selectedId) {
            fetchTripDetails(selectedId);
        }
    });
}

function failLoadMyTrip(error) {
    hideLoading();
    console.error('Error loading myTrip:', error);
    alert('로그인이 필요합니다.');
    // 로그인 페이지로 리디렉션
    window.location.href = '/login';
}

// 에러 메시지 모달 표시 함수 (선택 사항)
function showError(message) {
    // 에러 모달이 존재하는지 확인
    const errorModal = $('#errorModal');
    if (errorModal.length) {
        document.getElementById('errorModalBody').textContent = message;
        errorModal.modal('show');
    } else {
        // 모달이 없으면 alert으로 대체
        alert(message);
    }
}

// 특정 여행 정보 상세 가져오기
function fetchTripDetails(id) {
    showLoading();

    httpRequest('GET', `/trip/${id}`, null, successLoadTripDetails, failLoadTripDetails);
}

function successLoadTripDetails(tripInfo) {
    hideLoading();
    const detailsContainer = document.getElementById('trip-details');

    // 기존 상세 정보 초기화
    detailsContainer.innerHTML = '';

    if (!tripInfo) {
        detailsContainer.innerHTML = '<p>여행 정보를 불러올 수 없습니다.</p>';
        return;
    }

    // 여행 상세 정보 표시 (예시)
    detailsContainer.innerHTML = `
        <h2>${tripInfo.areaName} 여행 상세</h2>
        <p>작성자: ${tripInfo.author}</p>
        <p>기간: ${formatDate(tripInfo.startDate)} - ${formatDate(tripInfo.endDate)}</p>
        <p>생성일: ${formatDate(tripInfo.createdAt)}</p>
    `;

    // 콘텐츠 상세 정보 가져오기
    fetchContentDetails(tripInfo.contentIdsByDate, contentMap => {
        displayContentDetails(contentMap, detailsContainer);
    }, error => {
        console.error('Error fetching content details:', error);
        alert('콘텐츠 정보를 가져오는 데 실패했습니다.');
    });
}

function failLoadTripDetails(error) {
    hideLoading();
    console.error('Error loading trip details:', error);
    alert('여행 정보를 가져오는 데 실패했습니다.');
}

// 콘텐츠 상세 정보 가져오기
function fetchContentDetails(contentIdsByDate, success, fail) {
    const contentIds = [];
    for (const day in contentIdsByDate) {
        contentIds.push(...contentIdsByDate[day]);
    }

    const contentMap = {};
    let completed = 0;
    const total = contentIds.length;

    if (total === 0) {
        success(contentMap);
        return;
    }

    contentIds.forEach(contentId => {
        httpRequest('GET', `/content/${contentId}`, null, data => {
            contentMap[contentId] = data;
            completed++;
            if (completed === total) {
                success(contentMap);
            }
        }, error => {
            console.error(`Error fetching content ID ${contentId}:`, error);
            contentMap[contentId] = null; // 또는 필요한 방식으로 처리
            completed++;
            if (completed === total) {
                success(contentMap);
            }
        });
    });
}

// 콘텐츠 상세 정보 표시 함수
function displayContentDetails(contentMap, container) {
    const contentDetailsContainer = document.createElement('div');
    contentDetailsContainer.id = 'content-details';
    contentDetailsContainer.innerHTML = '<h3>여행 콘텐츠 목록</h3>';

    // Group the content by date
    const contentByDate = {};

    for (const contentId in contentMap) {
        const content = contentMap[contentId];
        if (content) {
            const contentDate = formatDate(content.date);  // Assuming `content.date` is the date field

            if (!contentByDate[contentDate]) {
                contentByDate[contentDate] = [];
            }
            contentByDate[contentDate].push(content);
        }
    }

    // Loop through each date group and display content
    for (const date in contentByDate) {
        const dateSection = document.createElement('div');
        dateSection.classList.add('date-section');

        const dateTitle = document.createElement('h4');
        dateTitle.textContent = `날짜: ${date}`;
        dateSection.appendChild(dateTitle);

        const contentList = document.createElement('div');
        contentList.classList.add('content-list');

        contentByDate[date].forEach(content => {
            const contentDiv = document.createElement('div');
            contentDiv.classList.add('content-item');
            contentDiv.innerHTML = `
                <h5>${content.title}</h5>
                <p>전화번호: ${content.tel || "정보 없음"}</p>
                <p>주소: ${content.addr1 || "정보 없음"}</p>
                <img src="${content.firstimage}" alt="${content.title}" style="max-width:200px;">
                <p>홈페이지: <a href="${content.homepage}" target="_blank">${content.homepage || "정보 없음"}</a></p>
                <p>개요: ${content.overview || "정보 없음"}</p>
                <hr>
            `;
            contentList.appendChild(contentDiv);
        });

        dateSection.appendChild(contentList);
        contentDetailsContainer.appendChild(dateSection);
    }

    container.appendChild(contentDetailsContainer);
}

// 에러 핸들링을 위한 failLoadContentDetails 함수 정의
function failLoadContentDetails(error) {
    hideLoading();
    console.error('Error loading content details:', error);
    alert('콘텐츠 정보를 가져오는 데 실패했습니다.');
    // 선택 사항: showError 함수를 사용하여 모달로 에러 표시
    showError('콘텐츠 정보를 가져오는 데 실패했습니다.');
}
