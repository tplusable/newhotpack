//function fetchAreaList() {
//    function success(response) {
//        response.text().then(htmlContent => {
//            document.open();
//            document.write(htmlContent);
//            document.close();
//        }).catch(error => {
//            console.error('Error parsing HTML:', error);
//            alert('데이터를 불러오는 중 오류가 발생했습니다.');
//        });
//    }
//
//    function fail() {
//        alert("로그인이 필요합니다.");
//        location.replace('/login');
//    }
//
//    console.log(response)
//    // `httpRequest` 함수로 요청 실행
//    httpRequest('GET', '/getAreaList', null, success, fail);
//}
//
//// HTML 페이지 로드 후 데이터 가져오기
//window.onload = fetchAreaList;
//
//// 쿠키를 가져오는 함수
//function getCookie(key) {
//    var result = null;
//    var cookie = document.cookie.split(';');
//    cookie.some(function (item) {
//        item = item.replace(' ', '');
//
//        var dic = item.split('=');
//
//        if (key === dic[0]) {
//            result = dic[1];
//            return true;
//        }
//    });
//
//    return result;
//}
//
//// HTTP 요청을 보내는 함수
//function httpRequest(method, url, body, success, fail) {
//    fetch(url, {
//        method: method,
//        headers: { // 로컬 스토리지에서 액세스 토큰 값을 가져와 헤더에 추가
//            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
//            'Content-Type': 'application/json',
//        },
//        body: body,
//    }).then(response => {
//        if (response.status === 200 || response.status === 201) {
//            return success();
//        }
//        const refresh_token = getCookie('refresh_token');
//        if (response.status === 401 && refresh_token) {
//            fetch('/api/token', {
//                method: 'POST',
//                headers: {
//                    Authorization: 'Bearer ' + localStorage.getItem('access_token'),
//                    'Content-Type': 'application/json',
//                },
//                body: JSON.stringify({
//                    refreshToken: getCookie('refresh_token'),
//                }),
//            })
//                .then(res => {
//                    if (res.ok) {
//                        return res.json();
//                    }
//                })
//                .then(result => { // 재발급이 성공하면 로컬 스토리지값을 새로운 액세스 토큰으로 교체
//                    localStorage.setItem('access_token', result.accessToken);
//                    httpRequest(method, url, body, success, fail);
//                })
//                .catch(error => fail());
//        } else {
//            return fail();
//        }
//    });
//}