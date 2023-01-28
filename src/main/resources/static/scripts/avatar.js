function sendRequest(method, url, body = null) {
    const xhr = new XMLHttpRequest();

    xhr.open(method, url);

    xhr.responseType = 'text';
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.onload = () => {
      if (xhr.status == 302 || (xhr.responseURL !== url)) {
        return 'https://raw.githubusercontent.com/Krokochik/resources/main/guest.png';
      }
      else {
        return xhr.response;
      }
    }

    xhr.onerror = () => {
      return xhr.response;
    }

    xhr.send(JSON.stringify(body));
  )
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
  var response = await sendRequest('POST', 'https://ideas-forum.herokuapp.com/avatar');
  return response;
}

async function saveAvatar() {
  await loadAvatar().then(avatar => document.cookie = 'avatar=' + avatar);
}

function saveAvatar(avatar) {
  document.cookie = 'avatar=' + avatar;
}

function getAvatar() {
  var cookieAvatar = document.cookie.avatar;
  alert(cookieAvatar);
  if (cookieAvatar !== undefined) {
    return cookieAvatar;
  }
  else {
    var avatar = loadAvatar();

    if (avatar !== 'https://raw.githubusercontent.com/Krokochik/resources/main/guest.png') {
      saveAvatar(avatar);
    }
    return avatar;

  }
}

function deleteAvatar() {
  document.cookie.avatar = '';
}