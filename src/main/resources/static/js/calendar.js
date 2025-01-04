let currentDateStart = new Date();
let currentDateEnd = new Date();
let selectedDateStart = null;
let selectedDateEnd = null;

function toggleCalendar(inputId) {
    // 모든 달력을 숨김
    document.querySelectorAll('.calendar').forEach(calendar => {
        calendar.style.display = 'none';
    });

    // 해당 입력 필드에 연결된 달력 ID 가져오기
    const calendarId = 'calendar-' + inputId;
    const calendar = document.getElementById(calendarId);

    // 현재 달력 상태 가져오기
    const isHidden = calendar.style.display === 'none' || calendar.style.display === '';

    // 클릭한 달력 보임/숨김 처리
    if (isHidden) {
        calendar.style.display = 'block';
        if (inputId === 'startDate') {
            renderCalendar(currentDateStart.getFullYear(), currentDateStart.getMonth(), 'startDate');
        } else {
            renderCalendar(currentDateEnd.getFullYear(), currentDateEnd.getMonth(), 'endDate');
        }
    }
}

document.addEventListener('click', (event) => {
    if (!event.target.closest('.date-container')) {
        document.querySelectorAll('.calendar').forEach(cal => cal.style.display = 'none');
    }
});

function renderCalendar(year, month, inputId) {
    const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    const calendarMonth = document.getElementById('calendar-month-' + inputId);
    calendarMonth.innerText = `${monthNames[month]} ${year}`;

    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const firstDay = new Date(year, month, 1).getDay();
    const calendarDays = document.getElementById('calendar-days-' + inputId);
    calendarDays.innerHTML = '';  // 기존 날짜들 지우기

    // 첫 번째 날의 전 공백 채우기
    for (let i = 0; i < firstDay; i++) {
        const emptyCell = document.createElement('span');
        calendarDays.appendChild(emptyCell);
    }

    // 날짜 채우기
    for (let day = 1; day <= daysInMonth; day++) {
        const dayCell = document.createElement('span');
        dayCell.innerText = day;
        dayCell.onclick = () => selectDate(day, inputId); // 날짜 선택
        calendarDays.appendChild(dayCell);
    }
}

function selectDate(day, inputId) {
    const selectedDate = new Date(inputId === 'startDate' ? currentDateStart.getFullYear() : currentDateEnd.getFullYear(),
                                  inputId === 'startDate' ? currentDateStart.getMonth() : currentDateEnd.getMonth(),
                                  day);

    if (inputId === 'startDate') {
        selectedDateStart = selectedDate;
        document.getElementById('startDate').value = formatDate(selectedDate);
    } else {
        selectedDateEnd = selectedDate;
        document.getElementById('endDate').value = formatDate(selectedDate);
    }

    closeCalendar(inputId);
}

function formatDate(date) {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function prevMonth(inputId) {
    if (inputId === 'startDate') {
        currentDateStart.setMonth(currentDateStart.getMonth() - 1);
        renderCalendar(currentDateStart.getFullYear(), currentDateStart.getMonth(), inputId);
    } else {
        currentDateEnd.setMonth(currentDateEnd.getMonth() - 1);
        renderCalendar(currentDateEnd.getFullYear(), currentDateEnd.getMonth(), inputId);
    }
}

function nextMonth(inputId) {
    if (inputId === 'startDate') {
        currentDateStart.setMonth(currentDateStart.getMonth() + 1);
        renderCalendar(currentDateStart.getFullYear(), currentDateStart.getMonth(), inputId);
    } else {
        currentDateEnd.setMonth(currentDateEnd.getMonth() + 1);
        renderCalendar(currentDateEnd.getFullYear(), currentDateEnd.getMonth(), inputId);
    }
}

function closeCalendar(inputId) {
    document.getElementById('calendar-' + inputId).style.display = 'none';
}
function submitForm() {
    const areaCode = document.getElementById('areaCode').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    // 시작 날짜와 종료 날짜 유효성 검사
    if (new Date(startDate) > new Date(endDate)) {
        alert("종료 날짜는 시작 날짜 이후이어야 합니다.");
        return;
    }

    const form = document.getElementById('travelForm');
    form.action = "/getTouristSpots"; // 목적지 URL
    form.submit();
}

function updateLabel(inputId) {
    const input = document.getElementById(inputId);
    const label = document.getElementById(`${inputId}Label`);

    if (input.value) {
        label.textContent = input.value;  // 선택된 날짜를 label에 표시
    } else {
        label.textContent = inputId === 'startDate' ? '출발일 선택' : '도착일 선택';
    }
}




