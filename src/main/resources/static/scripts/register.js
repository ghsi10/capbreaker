$(document).ready(function () {
    var username = $("#username");
    var password = $("#password");
    var passwordAgain = $("#passwordAgain");

    username.on('input', function () {
        if (this.value.length > 0 && password.val().length > 0 && password.val() === passwordAgain.val())
            $("#signUp").removeAttr("disabled");
        else
            $("#signUp").attr("disabled", "disabled");
    });

    password.on('input', function () {
        if (this.value.length > 0 && username.val().length > 0 && password.val() === passwordAgain.val())
            $("#signUp").removeAttr("disabled");
        else
            $("#signUp").attr("disabled", "disabled");
    });

    passwordAgain.on('input', function () {
        if (this.value.length > 0 && username.val().length > 0 && password.val() === passwordAgain.val())
            $("#signUp").removeAttr("disabled");
        else
            $("#signUp").attr("disabled", "disabled");
    });
});