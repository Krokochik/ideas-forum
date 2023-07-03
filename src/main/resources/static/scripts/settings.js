       function toLight() {
           if(document.cookie.theme != 'light')
               document.cookie = 'theme=light'
               window.location.href = window.location.href;
       }

       function toDark() {
           if(document.cookie.theme != 'dark')
               document.cookie = 'theme=dark'
               window.location.href = window.location.href;
       }

       function onLoad() {
           if(document.getElementById('theme').value == 'dark')
               document.getElementById('fListGroupCheckableRadios1').setAttribute('checked', '');
           else
               document.getElementById('fListGroupCheckableRadios2').setAttribute('checked', '');

           var isIdConfirmed = document.querySelector('#chng3').getAttribute('bool');

            var btn = document.getElementById('chng3');
            btn.addEventListener('click', function() { // Добавляем обработчик события на клик кнопки
                if (isIdConfirmed !== 'true') {
                    window.location.href = '/id-confirmation'; //
                }
            });
            if (isIdConfirmed === 'true') {
                btn.click();
            }


        } onLoad();
var interval;
        function updateMfaCodes() {
  $.ajax({
    url: "/mfa/codes",
    method: "POST",
    success: function(response) {
    clearInterval(interval);
      var codes = [];
      response.codes.forEach(code => codes.push(code));

      $("#list").remove();
      $("#qrcode").remove();

      $("#column1").empty();
      for (var i = 0; i < 4; i++) {
        $("#column1").append("<p>" + codes[i] + "</p>");
      }

      $("#column2").empty();
      for (var j = 4; j < 8; j++) {
        $("#column2").append("<p>" + codes[j] + "</p>");
      }

            $("#column3").empty();
      for (var k = 8; k < 12; k++) {
        $("#column3").append("<p>" + codes[j] + "</p>");
      }

            $("#column4").empty();
      for (var l = 12; l < 16; l++) {
        $("#column4").append("<p>" + codes[j] + "</p>");
      }
    },
    error: function() {}
  });
}

updateMfaCodes();
interval = setInterval(updateMfaCodes, 5000);