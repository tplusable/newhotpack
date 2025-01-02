document.addEventListener('DOMContentLoaded', () => {
  const customLists = {}; // 날짜별 관광지 리스트 관리
  let markers = []; // 지도 마커 저장
  let polylines = []; // 지도 폴리라인 저장
  let selectedDay = null; // 현재 선택된 날짜
  const mapContainer = document.getElementById('map');
  const map = new kakao.maps.Map(mapContainer, { center: new kakao.maps.LatLng(33.450701, 126.570667), level: 5 });

  // 초기화 함수
  function initialize() {
    setupDayButtons();
    setupAddButtons();
    setupNextButton();
  }

  // 날짜 버튼 클릭 이벤트 등록
  function setupDayButtons() {
    document.querySelectorAll('.days-list button').forEach(button => {
      button.addEventListener('click', () => {
        selectedDay = button.textContent.trim(); // 클릭한 버튼의 텍스트(날짜)를 가져와 selectedDay에 저장
        console.log('선택된 날짜:', selectedDay); // 날짜 값 확인용 로그 출력
        displayCustomList(selectedDay); // 해당 날짜의 리스트를 표시
      });
    });
  }


  // "+ 추가" 버튼 클릭 이벤트 등록
  function setupAddButtons() {
    document.querySelectorAll('.add-button').forEach(button => {
      button.addEventListener('click', event => {
        if (!selectedDay) {
          alert('먼저 일자를 선택하세요!');
          return;
        }

        const itemElement = event.target.closest('.item');
        const itemData = extractItemData(itemElement);

        if (!customLists[selectedDay]) customLists[selectedDay] = [];
        const marker = addMarker(new kakao.maps.LatLng(itemData.mapy, itemData.mapx));
        itemData.marker = marker;

        customLists[selectedDay].push(itemData);
        connectMarkers(marker);
        displayCustomList(selectedDay);
        map.setCenter(marker.getPosition());
      });
    });
  }

    function setupNextButton() {
      document.getElementById('nextButton').addEventListener('click', async () => {
        const startDate = document.getElementById('startDateText').textContent;
            const endDate = document.getElementById('endDateText').textContent;

        // 날짜별로 contentid를 정리
        const contentIdsByDate = Object.keys(customLists).reduce((acc, day) => {
          acc[day] = customLists[day].map(item => item.contentid);
          return acc;
        }, {});

        console.log('contentIdsByDate:', contentIdsByDate);

        // 서버로 보낼 데이터 구성
        const postData = {
          areaName: getAreaName(),
          startDate: startDate, // 화면에서 가져온 여행 시작일
          endDate: endDate, // 임의 종료일
          contentIdsByDate: contentIdsByDate,
        };

        console.log('Post Data:', postData); // 디버깅용 출력

        // 데이터 전송
        try {
          await sendPostData(postData); // 전송 완료를 기다림
          alert('여행 정보가 저장되었습니다!');
          window.location.href = "/trip/view/myTrip"; // 저장 후 페이지 이동
        } catch (error) {
          console.error('데이터 전송 실패:', error);
          alert('데이터 전송 중 문제가 발생했습니다.');
        }
      });
    }



  // 아이템 데이터를 추출
  function extractItemData(itemElement) {
    return {
      title: itemElement.getAttribute('data-title'),
      firstimage: itemElement.getAttribute('data-image'),
      mapx: parseFloat(itemElement.getAttribute('data-mapx')),
      mapy: parseFloat(itemElement.getAttribute('data-mapy')),
      contentid: itemElement.getAttribute('data-content')
    };
  }

  // 커스텀 리스트 표시
  function displayCustomList(day) {
    const customList = document.getElementById('customItems');
    customList.innerHTML = ''; // 기존 리스트 초기화

    if (customLists[day]) {
      customLists[day].forEach((item, index) => {
        const listItem = createCustomListItem(item, index, day);
        customList.appendChild(listItem);
      });
    }
  }

  // 커스텀 리스트 항목 생성
  function createCustomListItem(item, index, day) {
    const listItem = document.createElement('li');
    listItem.innerHTML = `
      <div class="item-content">
        <img src="${item.firstimage}" alt="이미지" class="item-image" />
        <span>${item.title}</span>
        <button class="delete-button" data-index="${index}" data-day="${day}">삭제</button>
      </div>
    `;

    listItem.querySelector('.delete-button').addEventListener('click', event => {
      const idx = event.target.getAttribute('data-index');
      const dayKey = event.target.getAttribute('data-day');

      removeCustomListItem(dayKey, idx);
    });

    return listItem;
  }

  // 커스텀 리스트 아이템 삭제
  function removeCustomListItem(day, index) {
    const item = customLists[day][index];
    item.marker.setMap(null); // 마커 삭제
    customLists[day].splice(index, 1); // 리스트에서 삭제
    removeLastPolyline();
    displayCustomList(day);
  }

  // 마커 추가
  function addMarker(position) {
    const marker = new kakao.maps.Marker({ position });
    marker.setMap(map);
    markers.push(marker);
    return marker;
  }

  // 마커 연결
  function connectMarkers(marker) {
    if (markers.length > 1) {
      const path = [
        markers[markers.length - 2].getPosition(),
        marker.getPosition()
      ];
      const polyline = new kakao.maps.Polyline({
        map,
        path,
        strokeColor: '#FF0000',
        strokeWeight: 2
      });
      polylines.push(polyline);
    }
  }

  // 마지막 폴리라인 제거
  function removeLastPolyline() {
    if (polylines.length > 0) {
      const polyline = polylines.pop();
      polyline.setMap(null);
    }
  }

  // 지역 이름 가져오기
  function getAreaName() {
    const areaNameElement = document.querySelector("h1");
    return areaNameElement
      ? areaNameElement.textContent.replace("관광지 목록 for ", "")
      : "Unknown Area";
  }

  // 데이터 전송
  function sendPostData(postData) {
    fetch('/trip/save', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json',
       'Authorization': `Bearer ${localStorage.getItem("access_token")}`},
      body: JSON.stringify(postData)
    })
      .then(response => {
        if (!response.ok) throw new Error('데이터 저장 실패');
        return response.json();
      })
      .then(() => {
            alert('여행 정보가 저장되었습니다!');

          })
          .catch(error => {
            // 에러가 발생해도 아무런 반응을 하지 않음
            console.log('Error:', error); // 로그로만 처리하거나 아무것도 하지 않음
          });
  }


  // 초기화 실행
  initialize();
});
