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
}
function onError() {
    $('#captchaPassed').prop('checked', '');
    $('#squaredFour1').prop('disabled', false);
    $('#squaredFour1').click();
}