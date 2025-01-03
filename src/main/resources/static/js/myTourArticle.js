// 페이지 로드 시 실행되는 함수
function fetchMyTourList() {
    fetch('/trip/myTrip', {
        method: 'GET',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json'
        }
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
            option.textContent = `나의 ${tripInfoDto.areaName} 여행 ${formatDate(tripInfoDto.startDate)} - ${formatDate(tripInfoDto.endDate)}`;
            select.appendChild(option);
        });

        // 선택 시 상세 정보를 현재 페이지에 표시하는 이벤트 리스너 추가
        select.addEventListener('change', function() {
            const selectedId = this.value;
            if (selectedId) {
                fetchTripDetails(selectedId);
            }
        });
    })
    .catch(error => {
        console.error('Error loading myTrip:', error);
        alert('로그인이 필요합니다.');
        location.replace('/login');
    });
}

window.onload = fetchMyTourList;

// 쿠키 조회 함수
function getCookie(key) {
    var result = null;
    var cookie = document.cookie.split(';');
    cookie.some(function (item) {
        item = item.trim(); // 공백 제거
        var dic = item.split('=');
        if (key === dic[0]) {
            result = decodeURIComponent(dic[1]);
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
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json'
        },
        body: body ? JSON.stringify(body) : null // body가 있을 경우 JSON 문자열로 변환
    })
    .then(response => {
        if (response.status === 200 || response.status === 201 || response.status === 204) {
            // 데이터가 있는 경우 JSON 파싱 후 success 호출
            if (response.status !== 204) { // 204 No Content인 경우
                return response.json().then(data => success(data));
            } else {
                return success();
            }
        }
        const refresh_token = getCookie('refresh_token');
        if (response.status === 401 && refresh_token) {
            // 토큰 재발급 시도
            return fetch('/api/token', {
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
                throw new Error('Failed to refresh token');
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
    })
    .catch(error => {
        console.error('HTTP Request Error:', error);
        fail();
    });
}

// 날짜 형식을 원하는 형태로 변환하는 함수 (예: 2024-05-01 → 2024.05.01)
function formatDate(input) {
    if (!input) return "정보 없음";
    // input이 날짜 문자열인지 확인
    if (typeof input === 'string' && !isNaN(Date.parse(input))) {
        const date = new Date(input);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}.${month}.${day}`;
    }
    return input;
}

// 함수: 특정 여행 정보 상세 가져오기
function fetchTripDetails(id) {
    httpRequest('GET', `/trip/${id}`, null, function(tripInfo) {
        const detailsContainer = document.getElementById('trip-details');

        // 기존 상세 정보와 지도를 초기화
        detailsContainer.innerHTML = '';
        const mapContainer = document.getElementById('map');
        mapContainer.innerHTML = '';

        // tripInfo가 없는 경우 처리
        if (!tripInfo) {
            detailsContainer.innerHTML = '<p>여행 정보를 불러올 수 없습니다.</p>';
            return;
        }

        // 콘텐츠 ID 추출 (중복 제거)
        let allContentIds = [];
        for (let day in tripInfo.contentIdsByDate) {
            allContentIds = allContentIds.concat(tripInfo.contentIdsByDate[day]);
        }
        allContentIds = [...new Set(allContentIds)]; // 중복 제거

        // 콘텐츠 상세 정보 가져오기
        fetchContentDetails(allContentIds)
            .then(contentDetails => {
                // 콘텐츠를 날짜별로 매핑
                const contentByDate = {};
                for (let day in tripInfo.contentIdsByDate) {
                    contentByDate[day] = tripInfo.contentIdsByDate[day].map(id => contentDetails[id] || {
                        contentid: id,
                        title: "정보 없음",
                        tel: "정보 없음",
                        addr1: "정보 없음",
                        firstimage: "/img/logo.png",
                        mapx: "정보 없음",
                        mapy: "정보 없음",
                        homepage: "정보 없음",
                        overview: "정보 없음"
                    });
                }

                // 상세 여행 정보 HTML 생성
                const tripDetailsHtml = `
                    <div class="card mb-3">
                        <div class="card-body">
                            <h5 class="card-title">${tripInfo.areaName || '정보 없음'}</h5>
                            <p class="card-text">여행 지역: ${tripInfo.areaName || '정보 없음'}</p>
                            <p class="card-text">시작일: ${formatDate(tripInfo.startDate)}</p>
                            <p class="card-text">종료일: ${formatDate(tripInfo.endDate)}</p>
                        </div>
                    </div>
                    <h2>날짜별 콘텐츠 정보</h2>
                    <ul>
                        ${Object.entries(contentByDate).map(([day, contents]) => `
                            <li>
                                <strong>날짜: ${day}</strong>
                                <ul>
                                    ${contents.map(content => `
                                        <li class="content-detail" data-id="${content.contentid}" data-title="${content.title}" data-mapx="${content.mapx}" data-mapy="${content.mapy}">
                                            <p>
                                                제목: ${content.title || '정보 없음'}<br>
                                                전화번호: ${content.tel || '정보 없음'}<br>
                                                주소: ${content.addr1 || '정보 없음'}<br>
                                                이미지: <img src="${content.firstimage || '/img/logo.png'}" alt="이미지"><br>
                                                홈페이지: ${content.homepage && content.homepage !== "정보 없음" ? `<a href="${content.homepage}" target="_blank">${content.homepage}</a>` : '정보 없음'}<br>
                                                개요: ${content.overview || '정보 없음'}<br>
                                            </p>
                                        </li>
                                    `).join('')}
                                </ul>
                            </li>
                        `).join('')}
                    </ul>
                `;

                detailsContainer.innerHTML = tripDetailsHtml;

                // 지도 초기화 및 표시
                initializeMap(contentDetails);
            })
            .catch(error => {
                console.error('Error fetching content details:', error);
                detailsContainer.innerHTML += '<p>콘텐츠 정보를 불러오는 데 실패했습니다.</p>';
            });
    }, function() {
        alert("여행 정보를 가져올 수 없습니다.");
    });
}

// 함수: 콘텐츠 상세 정보 가져오기
function fetchContentDetails(contentIds) {
    // 콘텐츠 ID별로 API 호출
    const fetchPromises = contentIds.map(id => {
        return fetch(`/trip/${id}`, {
            method: 'GET',
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('access_token'),
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                // API 호출 실패 시 기본값 반환
                return {
                    contentid: id,
                    title: "정보 없음",
                    tel: "정보 없음",
                    addr1: "정보 없음",
                    firstimage: "/img/logo.png",
                    mapx: "정보 없음",
                    mapy: "정보 없음",
                    homepage: "정보 없음",
                    overview: "정보 없음"
                };
            }
            return response.json();
        })
        .catch(error => {
            console.error(`Error fetching content ID ${id}:`, error);
            return {
                contentid: id,
                title: "정보 없음",
                tel: "정보 없음",
                addr1: "정보 없음",
                firstimage: "/img/logo.png",
                mapx: "정보 없음",
                mapy: "정보 없음",
                homepage: "정보 없음",
                overview: "정보 없음"
            };
        });
    });

    // 모든 API 호출이 완료될 때까지 기다림
    return Promise.all(fetchPromises).then(results => {
        const contentMap = {};
        results.forEach(content => {
            contentMap[content.contentid] = content;
        });
        return contentMap;
    });
}

// Kakao Maps API를 사용하여 지도 초기화 및 표시
function initializeMap(contentDetails) {
    // 마커 위치 정보를 담을 배열
    var markers = [];

    // 마커들의 위도, 경도를 합산할 변수
    var totalMapX = 0;
    var totalMapY = 0;

    // 각 콘텐츠의 데이터를 순회하며 마커 위치 추출
    contentDetails.forEach(function(content) {
        var mapX = parseFloat(content.mapx); // X좌표 (경도)
        var mapY = parseFloat(content.mapy); // Y좌표 (위도)

        // 유효한 좌표가 있는 경우에만 마커 추가
        if (!isNaN(mapX) && !isNaN(mapY)) {
            markers.push({
                mapx: mapX,
                mapy: mapY,
                title: content.title || '정보 없음'
            });

            // 위도와 경도를 합산
            totalMapX += mapX;
            totalMapY += mapY;
        }
    });

    console.log("Markers:", markers);

    // 마커들이 없으면 초기 지도를 서울 시청으로 설정
    if (markers.length === 0) {
        var defaultMapX = 126.9780;
        var defaultMapY = 37.5665;

        var mapContainer = document.getElementById('map'),
            mapOption = {
                center: new kakao.maps.LatLng(defaultMapY, defaultMapX), // 서울 시청 위치
                level: 10 // 초기 확대 레벨
            };

        var map = new kakao.maps.Map(mapContainer, mapOption);
        return;
    }

    // 마커들의 평균 위치 계산
    var avgMapX = totalMapX / markers.length;  // 평균 경도
    var avgMapY = totalMapY / markers.length;  // 평균 위도

    // 지도 생성 (카카오맵 예시)
    var mapContainer = document.getElementById('map'),
        mapOption = {
            center: new kakao.maps.LatLng(avgMapY, avgMapX), // 마커들의 평균 위치로 초기 지도 중심 설정
            level: 10 // 초기 확대 레벨
        };

    var map = new kakao.maps.Map(mapContainer, mapOption);

    // markers 배열을 사용하여 지도에 마커 추가
    markers.forEach(function(markerData) {
        var position = new kakao.maps.LatLng(markerData.mapy, markerData.mapx);
        var marker = new kakao.maps.Marker({
            position: position,
        });
        marker.setMap(map);

        // 정보 창 생성
        var infowindow = new kakao.maps.InfoWindow({
            content: `<div style="padding:5px;"><strong>${markerData.title}</strong></div>`
        });

        // 마커에 클릭 이벤트 추가
        kakao.maps.event.addListener(marker, 'click', function() {
            infowindow.open(map, marker);
        });
    });

    // polyline을 그리기 위한 경로 설정
    var path = markers.map(function(markerData) {
        return new kakao.maps.LatLng(markerData.mapy, markerData.mapx);
    });

    // polyline 옵션 설정
    var polyline = new kakao.maps.Polyline({
        path: path, // polyline의 경로 (마커 배열 순서대로 연결)
        strokeWeight: 5, // 선의 두께
        strokeColor: '#FF0000', // 선의 색상 (빨강)
        strokeOpacity: 0.7, // 선의 불투명도
        strokeStyle: 'solid' // 선의 스타일 (실선)
    });

    // polyline 지도에 추가
    polyline.setMap(map);
}