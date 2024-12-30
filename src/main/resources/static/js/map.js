document.addEventListener('DOMContentLoaded', () => {
  const customList = document.getElementById('customItems');

  // + 버튼 클릭 이벤트 핸들러
  document.querySelectorAll('.add-button').forEach(button => {
    button.addEventListener('click', event => {
      const item = event.target.closest('.item');
      const title = item.getAttribute('data-title');
      const firstimage = item.getAttribute('data-image');
      const mapx = item.getAttribute('data-mapx');
      const mapy = item.getAttribute('data-mapy');

      // 선택된 아이템을 커스텀 리스트에 추가
      const listItem = document.createElement('li');
      const itemContent = `
        <div class="item-content">
          <img src="${firstimage}" alt="이미지" class="item-image" />
          <span>${title}</span>
        </div>
      `;

      listItem.innerHTML = itemContent;
      customList.appendChild(listItem);

      // 지도 위치 업데이트
      const latLng = new kakao.maps.LatLng(mapy, mapx);
      map.setCenter(latLng);
      addMarker(latLng);
    });
  });

  // 카카오 지도 설정
  const mapContainer = document.getElementById('map');
  const mapOption = {
    center: new kakao.maps.LatLng(33.450701, 126.570667),
    level: 5
  };
  const map = new kakao.maps.Map(mapContainer, mapOption);

  const markers = [];
  const polylines = [];
  function addMarker(position) {
    const marker = new kakao.maps.Marker({ position });
    marker.setMap(map);
    markers.push(marker);

    // 핀 간 연결을 위한 선을 추가
        if (markers.length > 1) {
          const path = [markers[markers.length - 2].getPosition(), position];
          const polyline = new kakao.maps.Polyline({
            map: map,
            path: path,
            strokeColor: '#FF0000', // 선 색상
            strokeWeight: 2 // 선 두께
          });
          polylines.push(polyline); // 폴리라인 배열에 추가
        }
  }
});
