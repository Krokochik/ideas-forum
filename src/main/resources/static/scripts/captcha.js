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
    $('#squaredFour1').prop('disabled', false);
    $('#captchaPassed').prop('checked', '');
}