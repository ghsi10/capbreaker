$(document).ready(function () {

    $("#terms").click(function () {
        var checked_status = this.checked;
        if (checked_status === true)
            $("#upload-file-btn").removeAttr("disabled");
        else
            $("#upload-file-btn").attr("disabled", "disabled");
    });
});