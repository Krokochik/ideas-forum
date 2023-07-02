$(document).ready(function() {
    $('#squaredFour1').change(function() {
        if ($(this).is(':checked')) {
            $(this).prop('disabled', true);
            hcaptcha.execute();
            $('#submit').prop('disabled', true);
        }
    });
});
function onSubmit(token) {
    $('#captchaLbl').css('background-color', '#ffeba7');
}
function onError() {
    $('#squaredFour1').prop('disabled', false);
    $('#squaredFour1').prop('checked', false);
    $('#captchaLbl').css('background-color', '#ccc');
    $('#submit').prop('disabled', false);
}