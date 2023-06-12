var input1 = null;

function readURL(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('imagePreview').setAttribute('src', e.target.result);
            img = e.target.result;
        }
        reader.readAsDataURL(input.files[0]);

        input1 = input;

    }
}
$("#imageUpload").change(function() {
    readURL(this);
});

  function sendRequest(method, url, body = null) {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest()

        function getCsrfToken() {
          const cookieValue = document.cookie
            .split('; ')
            .find(cookie => cookie.startsWith('XSRF-TOKEN='));

          if (cookieValue) {
            return cookieValue.split('=')[1];
          }

          return null;
        }

        // Добавить CSRF-токен в заголовок запроса
        function addCsrfTokenToRequest(xhr) {
          const csrfToken = getCsrfToken();

          if (csrfToken) {
            //xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
            //body._csrf = csrfToken;
          }
        }

      xhr.open(method, url)
      addCsrfTokenToRequest(xhr)

      xhr.responseType = 'json'
      xhr.setRequestHeader('Content-Type', 'application/json')

      xhr.onload = () => {
          resolve(xhr.response)
      }

      xhr.onerror = () => {
        resolve(xhr.response)
      }

      xhr.send(JSON.stringify(body));
    })
  }

function saveChanges() {
    var xhr = new XMLHttpRequest();
                var newNickname = ""
                if (document.getElementById("nickname").value === "")
                    newNickname = document.getElementById("nickname").placeholder
                else
                    newNickname = document.getElementById("nickname").value


    if (input1 !== null) {
        var file = input1.files[0];
        var freader = new FileReader();
        freader.readAsDataURL(file);
        freader.onload = (function (f) {
            return function (e) {
                sendRequest('POST', "https://ideas-forum.herokuapp.com/profile", {nickname: newNickname, username: document.getElementById('name').value, avatar: img})
                  .then(data => {if ((document.getElementById("nickname").value.trim().length >= 4) || (document.getElementById("imagePreview").style !== document.getElementById("avatar").style))
                                    window.location.href = window.location.href;})
            };
        })(file);
    }
    else sendRequest('POST', "https://ideas-forum.herokuapp.com/profile", {nickname: newNickname, username: document.getElementById('name').value})
           .then(data => {if ((document.getElementById("nickname").value.trim().length >= 4) || document.getElementById("imagePreview").style !== document.getElementById("avatar").style)
                             window.location.href = window.location.href;})



}

function dismissChanges() {
    document.getElementById('imagePreview').setAttribute('src', document.getElementById('avatar').getAttribute('src'));
    document.getElementById('imagePreview').style.width = ''; document.getElementById('imagePreview').style.height = '';
    document.getElementById("nickname").value = '';
}