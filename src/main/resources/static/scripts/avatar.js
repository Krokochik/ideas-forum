function sendRequest(method, url, body = null) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()

    xhr.open(method, url)

    xhr.responseType = 'text'
    xhr.setRequestHeader('Content-Type', 'application/json')

    xhr.onload = () => {
      if (xhr.status == 302 || (xhr.responseURL !== url)) {
        reject(xhr.response)
      }
      else {
        resolve(xhr.response)
      }
    }

    xhr.onerror = () => {
      reject(xhr.response)
    }

    xhr.send(JSON.stringify(body));
  })
}

function tuneImages(avatar) {
  document.getElementById('temp').setAttribute('style', document.getElementById('temp').getAttribute('style') + 'background-image: url(' + avatar + ');');
  document.getElementById('avatar').setAttribute('style', document.getElementById('temp').getAttribute('style'));
  document.getElementById('avatar').style.visibility = '';
  document.getElementById('imagePreview').setAttribute('style', document.getElementById('temp').getAttribute('style'));
  document.getElementById('imagePreview').style.width = ''; document.getElementById('imagePreview').style.height = '';
  document.getElementById('imagePreview').style.visibility = '';
  document.getElementById('temp').remove();
}

async function loadAvatar() {
  sendRequest('POST', 'https://ideas-forum.herokuapp.com/avatar')
    .then(
      data => {
        return data;
      },
      error => {
        return 'https://raw.githubusercontent.com/Krokochik/resources/main/guest.png';
      }
    );
}

function saveAvatar() {
  loadAvatar().then(avatar => document.cookie = 'avatar=' + avatar);
}

function getAvatar() {
  var cookieAvatar = document.cookie.avatar;
  if (avatar !== undefined)
    return cookieAvatar;
  else 
    return 'https://raw.githubusercontent.com/Krokochik/resources/main/guest.png';
}

function deleteAvatar() {
  document.cookie.avatar = '';
}