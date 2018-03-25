$(document).ready(function () {
    $("#username").on('input', function () {
        if (this.value.length > 0 && $("#password").val().length > 0)
            $("#loginSubmit").removeAttr("disabled");
        else
            $("#loginSubmit").attr("disabled", "disabled");
    });

    $("#password").on('input', function () {
        if (this.value.length > 0 && $("#username").val().length > 0)
            $("#loginSubmit").removeAttr("disabled");
        else
            $("#loginSubmit").attr("disabled", "disabled");
    });
});