var captchaPassed = false;

function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
        function (m, key, value) {
            vars[key] = value;
        });
    return vars;
}

$(document).ready(function () {
    if (getUrlVars()["oauth2"] === undefined) {
        $('#submit').prop('disabled', true);
        $('#captchaLbl').hover(function () {
            if (captchaPassed == false) {
                $('#prnt').addClass('translucent-after');
                $('#prnt').removeClass('visible-after');
                $('#prnt').removeClass('hidden-after');
            }
        }, function () {
            if (captchaPassed == false) {
                $('#prnt').addClass('hidden-after');
                $('#prnt').removeClass('translucent-after');
                $('#prnt').removeClass('visible-after');
            }
        });
        $('#squaredFour1').change(function () {
            if ($(this).is(':checked')) {
                $(this).prop('disabled', true);
                hcaptcha.execute();
                captchaPassed = true;
                $('#submit').prop('disabled', true);
                $('#prnt').addClass('translucent-after');
                $('#prnt').removeClass('visible-after');
                $('#prnt').removeClass('hidden-after');
            }
        });
    }
});

function onSubmit(token) {
    captchaPassed = true;
    $('#captchaLbl').css({
        'background-color': '#ffeba7',
        'pointer-events': 'none'
    });
    $('#submit').prop('disabled', false);
    $('#prnt').addClass('visible-after');
    $('#prnt').removeClass('translucent-after');
    $('#prnt').removeClass('hidden-after');
}

function onError() {
    captchaPassed = false;
    $('#captchaLbl').css({
        'background-color': '#ccc',
        'pointer-events': 'all'
    });
    $('#squaredFour1').prop('disabled', false);
    $('#squaredFour1').prop('checked', false);
    $('#prnt').addClass('hidden-after');
    $('#prnt').removeClass('translucent-after');
    $('#prnt').removeClass('visible-after');
    $('#submit').prop('disabled', true);
}