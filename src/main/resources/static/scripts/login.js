$(document).ready(function () {
    $("#username").on('input', function () {
        if (this.value.length > 0 && $("#password").val().length > 0)
            $("#loginSbmit").removeAttr("disabled");
        else
            $("#loginSbmit").attr("disabled", "disabled");
    });

    $("#password").on('input', function () {
        if (this.value.length > 0 && $("#username").val().length > 0)
            $("#loginSbmit").removeAttr("disabled");
        else
            $("#loginSbmit").attr("disabled", "disabled");
    });
});