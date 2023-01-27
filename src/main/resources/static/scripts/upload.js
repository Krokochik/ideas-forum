var input1 = null;

function readURL(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function(e) {
            $('#imagePreview').css('background-image', 'url('+e.target.result +')');
            $('#imagePreview').hide();
            $('#imagePreview').fadeIn(650);
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

      xhr.open(method, url)

      xhr.responseType = 'json'
      xhr.setRequestHeader('Content-Type', 'application/json')

      xhr.onload = () => {
          resolve(xhr.response)
          console.log(xhr.response)
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
    document.getElementById('imagePreview').setAttribute('style', document.getElementById('avatar').getAttribute('style'));
    document.getElementById('imagePreview').style.width = ''; document.getElementById('imagePreview').style.height = '';
    document.getElementById("nickname").value = '';
}