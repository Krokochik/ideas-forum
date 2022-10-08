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

function saveAvatar() {
    if (input1 !== null) {
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "http://localhost:6606/profile");
        xhr.setRequestHeader("Content-type", "application/json");
        var file = input1.files[0];
        var freader = new FileReader();
        freader.readAsDataURL(file);
        freader.onload = (function (f) {
            return function (e) {
                xhr.send(JSON.stringify({"avatar": img, "name": document.getElementById('name').value}));
            };
        })(file);
    }
}