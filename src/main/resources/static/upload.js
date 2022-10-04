function readURL(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function(e) {
            $('#imagePreview').css('background-image', 'url('+e.target.result +')');
            $('#imagePreview').hide();
            $('#imagePreview').fadeIn(650);
        }
        reader.readAsDataURL(input.files[0]);
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "https://ideas-forum.herokuapp.com/profile");
        xhr.setRequestHeader("Content-type", "application/json");

        var file = input.files[0];
        var freader = new FileReader();
        freader.readAsDataURL(file);
        freader.onload = (function (f) {
            return function (e) {
                xhr.send(JSON.stringify({"avatar": this.result, "name": document.getElementById('name').value}));
            };
        })(file);
    }
}
$("#imageUpload").change(function() {
    readURL(this);
});