 document.addEventListener('DOMContentLoaded', () => {
      const customLists = {}; // 날짜별 관광지 리스트 관리
      let selectedDay = null;

      // 초기화 함수
      function initialize() {
        createDaySections();
        setupToggleButtons();
      }

      function createDaySections() {
        const startDate = new Date(document.getElementById('startDateText').textContent);
        const endDate = new Date(document.getElementById('endDateText').textContent);
        const dayCount = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;

        const daysContainer = document.getElementById('daysContainer');

        // 날짜를 담을 <ul> 생성
        const dayList = document.createElement('ul');
        dayList.classList.add('days-list');

        for (let i = 0; i < dayCount; i++) {
          const currentDate = new Date(startDate.getTime() + i * 24 * 60 * 60 * 1000);
          const dayString = `${currentDate.getMonth() + 1}월 ${currentDate.getDate()}일 (${getKoreanDayName(currentDate)})`;

          // 각 날짜를 <li>로 생성
          const dayItem = document.createElement('li');
          dayItem.classList.add('day-item');

          // 날짜에 대한 내용 추가
          dayItem.innerHTML = `
            <div class="day-header">
              <span>${dayString}</span>
              <button class="toggle-button">▼</button>
            </div>
            <div class="day-content" style="display: none;">
              <button class="add-button" data-day="${i + 1}">장소 추가하기</button>
              <ul id="customItems-${i + 1}" class="custom-container"></ul>
            </div>
          `;

          dayList.appendChild(dayItem);
        }

        // <ul>을 daysContainer에 추가
        daysContainer.appendChild(dayList);

        // 토글 버튼 초기화
        setupToggleButtons();
      }


      // 토글 버튼 클릭 이벤트 등록
      function setupToggleButtons() {
        document.querySelectorAll('.toggle-button').forEach(button => {
          button.addEventListener('click', event => {
            const dayContent = event.target.closest('.day-section').querySelector('.day-content');
            const isHidden = dayContent.style.display === 'none';

            dayContent.style.display = isHidden ? 'block' : 'none';
            event.target.textContent = isHidden ? '▲' : '▼';
          });
        });
      }

      // 초기화 실행
      initialize();
    });