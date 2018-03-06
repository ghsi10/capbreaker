var isSelectedfile = false;
var isTermsChecked = false;

$(document).ready(function () {

    $("#terms").click(function () {
        var checked_status = this.checked;
        if (checked_status === true)
            isTermsChecked = true;
        else
            isTermsChecked = false;
        validator();
    });

    $("#cap-file").change(function () {
        var file = this.files.item(0);
        isSelectedfile = true;
        validator();
        $("#fname").html(file.name);
    });
});

function validator() {
    if (isTermsChecked === true && isSelectedfile === true)
        $("#upload-file-btn").removeAttr("disabled");
    else
        $("#upload-file-btn").attr("disabled", "disabled");
}