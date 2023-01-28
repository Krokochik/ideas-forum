function sendRequest(method, url, callback) {
    const xhr = new XMLHttpRequest();

    xhr.open(method, url);

    xhr.responseType = 'text';

    xhr.onload = () => {
      if (xhr.status == 302 || (xhr.responseURL !== url)) {
        callback('https://raw.githubusercontent.com/Krokochik/resources/main/guest.png');
      }
      else {
        callback(xhr.response);
      }
    }

    xhr.onerror = () => {
      callback('https://raw.githubusercontent.com/Krokochik/resources/main/guest.png');
    }

    xhr.send();
}

function getCookie(name) {
  let matches = document.cookie.match(new RegExp(
    "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
  ));
  return matches ? decodeURIComponent(matches[1]) : undefined;
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

async function loadAndSaveAvatar() {
  var cookieAvatar = getCookie('avatar');
  if (cookieAvatar !== undefined) {
    tuneImages(cookieAvatar);
    return;
  }
  sendRequest('POST', 'https://ideas-forum.herokuapp.com/avatar', saveAvatar);
}

function saveAvatar(avatar) {
  document.cookie = 'avatar=' + avatar;
  loadAndSaveAvatar();
}