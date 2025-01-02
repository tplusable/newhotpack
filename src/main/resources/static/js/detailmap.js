document.addEventListener('DOMContentLoaded', function() {
    // 모든 'content-detail' 클래스를 가진 <li> 요소를 가져옵니다.
    var contentDetails = document.querySelectorAll('.content-detail');

    // 마커 위치 정보를 담을 배열
    var markers = [];

    // 마커들의 위도, 경도를 합산할 변수
        var totalMapX = 0;
        var totalMapY = 0;

    // 각 <li> 요소를 순회하며 데이터를 추출
    contentDetails.forEach(function(detail) {
        // 'data-*' 속성 값 추출
        var mapX = parseFloat(detail.getAttribute('data-mapx')); // X좌표 (경도)
        var mapY = parseFloat(detail.getAttribute('data-mapy')); // Y좌표 (위도)

        // 유효한 좌표가 있는 경우에만 마커 추가
        if (mapX && mapY) {
            markers.push({
                mapx: mapX,
                mapy: mapY
            });

            // 위도와 경도를 합산
            totalMapX += mapX;
            totalMapY += mapY;
        }
    });

    console.log("Markers:", markers);

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


});
