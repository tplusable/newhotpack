document.addEventListener('DOMContentLoaded', () => {
              const mapContainer = document.getElementById('map'); // 지도 표시용 컨테이너
              const map = new kakao.maps.Map(mapContainer, {
                center: new kakao.maps.LatLng(37.5665, 126.9780), // 초기 좌표 (서울)
                level: 5 // 초기 줌 레벨
              });

              // JSON 형태로 받은 touristSpotsByDate
              const contentIdsByDate = JSON.parse(document.getElementById('contentIdsByDate').textContent);

              const id = [[${tripInfo.id}]]; // tripInfo.id 사용

              // 서버에서 특정 contentId의 데이터를 가져오는 함수
              async function fetchTouristSpot(contentId) {
                try {
                  const response = await fetch(`/trip/view/${id}/${contentId}`);
                  if (!response.ok) throw new Error('Failed to fetch tourist spot data');
                  return await response.json();
                } catch (error) {
                  console.error(error);
                  return null;
                }
              }

              // 지도에 마커를 추가하는 함수
              function addMarker(lat, lng, title) {
                const position = new kakao.maps.LatLng(lat, lng);
                const marker = new kakao.maps.Marker({ position });
                marker.setMap(map);

                // 마커 클릭 이벤트
                const infowindow = new kakao.maps.InfoWindow({
                  content: `<div style="padding:5px;">${title}</div>`
                });
                kakao.maps.event.addListener(marker, 'click', () => {
                  infowindow.open(map, marker);
                });

                return marker;
              }

              // 선택된 contentId에 따라 지도에 데이터를 표시
              async function displayTouristSpots(contentIds) {
                for (const day in contentIds) {
                  for (const contentId of contentIds[day]) {
                    const spotData = await fetchTouristSpot(contentId);
                    if (spotData) {
                      const { mapy, mapx, title } = spotData;
                      addMarker(parseFloat(mapy), parseFloat(mapx), title);
                    }
                  }
                }
              }

              // Fetch and display tourist spots
              displayTouristSpots(contentIdsByDate);
            });