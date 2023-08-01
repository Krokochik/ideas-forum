// =====================
// Create required vars
// =====================
var output = $('.output');
var input = $('textarea.input');
var toOutput;
let serverEndpoint = 'http://localhost:6606/terminal';

// Creates the event listner for the comands ==
// Yes this is a long one - could do with some
// improvements ===============================
function getCsrfToken() {
  const cookieValue = document.cookie
    .split('; ')
    .find(cookie => cookie.startsWith('XSRF-TOKEN='));

  if (cookieValue) {
    return cookieValue.split('=')[1];
  }

  return null;
}

input.keydown(function (e) {
  if (e.keyCode == 13 && !event.ctrlKey) {
    var inputVal = $.trim(input.val());
    event.preventDefault();
    input.val('');

    if (inputVal == "clear") {
      clearConsole();
    } else if (inputVal.startsWith('sql ') || inputVal.startsWith('sql\n')) {
      var body = {
        sql: $.trim(inputVal.substring(4))
      };
      $.ajax({
        url: serverEndpoint,
        method: 'POST',
        data: JSON.stringify(body),
        success: function (response) {
          console.log(response);
        },
        error: function (xhr, status, error) {
          console.log(xhr);
          console.log(status);
          console.log(error);
        },
        beforeSend: function (xhr) {
          xhr.setRequestHeader('X-XSRF-TOKEN', getCsrfToken());
          xhr.setRequestHeader('Content-Type', 'application/json');
        },
      });
    }
  } else if (event.keyCode == 13 && event.ctrlKey) {
    var content = this.value;
    var caret = getCaret(this);
    this.value = content.substring(0, caret) + "\n" + content.substring(caret, content.length);
    event.stopPropagation();
  }
});

function getCaret(el) {
  if (el.selectionStart) {
    return el.selectionStart;
  } else if (document.selection) {
    el.focus();

    var r = document.selection.createRange();
    if (r == null) {
      return 0;
    }

    var re = el.createTextRange(),
      rc = re.duplicate();
    re.moveToBookmark(r.getBookmark());
    rc.setEndPoint('EndToStart', re);

    return rc.text.length;
  }
  return 0;
}
// functions related to the commands typed
// =======================================

//clears the screen
function clearConsole() {
  output.html("");
  Output('<span>clear</span></br>');
}

function sayThis(data) {
  data = data.substr(data.indexOf(' ') + 1);
  Output('<span class="green">[say]:</span><span>' + data + '</span></br>');
}

// Prints out the result of the command into the output div
function Output(data) {
  $(data).appendTo(output);
}