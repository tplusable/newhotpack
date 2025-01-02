//Http 요청을 보내는 공통 함수
export function httpRequest(method, url, body, success, fail) {
  fetch(url, {
    method: method,
    headers: {
      Authorization: 'Bearer ' + localStorage.getItem('access_token'),
      'Content-Type':'application/json',
    },
    body:body,
  }).then(response=> {
    if (response.status===200||response.status===201) {
      return success();
    }
    const refresh_token=getCookie('refresh_token');
    if(response.status===401&& refresh_token) {
      fetch('/api/token', {
        method:'POST',
        headers:{
          Authorization:'Bearer '+localStorage.getItem('access_token'),
          'Content-Type':'application/json',
        },
        body:JSON.stringify({
          refreshToken:getCookie('refresh_token'),
        }),
      })
        .then(res=> {
          if(res.ok) {
            return res.json();
          }
        })
        .then(result => {
          localStorage.setItem('access_token', result.accessToken);
          httpRequest(method, url, body, success, fail);
        })
        .catch(error=>fail());
    } else {
      return fail();
    }
  });
}

// //쿠키 값 가져오기
// export function getCookie(name) {
//   const value=`; ${document.cookie}`;
//   const parts=value.split(`; ${name}=`);
//   if (parts.length===2) return parts.pop().split(';').shift();
// }

// 쿠키를 가져오는 함수
function getCookie(key) {
  let result = null;
  let cookie = document.cookie.split(';');
  cookie.some(function (item) {
    item = item.replace(' ', '');

    let dic = item.split('=');

    if (key === dic[0]) {
      result =dic[1];
      return true;
    }
  });

  return result;
}

// 쿠키를 삭제하는 함수
function deleteCookie(name) {
  document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}