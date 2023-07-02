$(document).ready(function() {
    $('#squaredFour1').change(function() {
        if ($(this).is(':checked')) {
            $(this).prop('disabled', true);
            hcaptcha.execute();
        }
    });
});
function onSubmit(token) {
    $('#captchaPassed').prop('checked', '');
    $('#captchaLbl').css("background-color": "#ffeba7")
    $('#captchaLbl:after').css("opacity": "1")
}
function onError() {
    $('#squaredFour1').prop('disabled', false);
    $('#captchaLbl').css("background-color": "#ccc)
    $('#captchaLbl:after').css("opacity": "1")
}