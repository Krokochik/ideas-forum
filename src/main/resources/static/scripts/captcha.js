$(document).ready(function() {
    $('#squaredFour1').change(function() {
        if ($(this).is(':checked')) {
            $(this).prop('disabled', true);
            hcaptcha.execute();
            $('#submit').prop('disabled', true);
            $('#prnt').addClass('translucent-after');
            $('#prnt').removeClass('visible-after');
            $('#prnt').removeClass('hidden-after');
        }
    });
});
function onSubmit(token) {
    $('#captchaLbl').css('background-color', '#ffeba7');
    $('#prnt').addClass('visible-after');
    $('#prnt').removeClass('translucent-after');
    $('#prnt').removeClass('hidden-after');
}
function onError() {
    $('#squaredFour1').prop('disabled', false);
    $('#squaredFour1').prop('checked', false);
    $('#captchaLbl').css('background-color', '#ccc');
    $('#prnt').addClass('hidden-after');
    $('#prnt').removeClass('translucent-after');
    $('#prnt').removeClass('visible-after');
    $('#submit').prop('disabled', false);
}