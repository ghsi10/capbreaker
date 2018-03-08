$(document).ready(function () {
    $("#inputTaskId").on('input', function () {
        if (this.value.length > 0 && $("#inputTaskPassword").val().length > 0)
            $("#getResultBtn").removeAttr("disabled");
        else
            $("#getResultBtn").attr("disabled", "disabled");
    });

    $("#inputTaskPassword").on('input', function () {
        if (this.value.length > 0 && $("#inputTaskId").val().length > 0)
            $("#getResultBtn").removeAttr("disabled");
        else
            $("#getResultBtn").attr("disabled", "disabled");
    });

    $("#adminInputTaskId").on('input', function () {
        if (this.value.length > 0)
            $("#getAdminResultBtn").removeAttr("disabled");
        else
            $("#getAdminResultBtn").attr("disabled", "disabled");
    });
});