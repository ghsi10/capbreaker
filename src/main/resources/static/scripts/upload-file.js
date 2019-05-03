var isSelectedfile = false;
var isTermsChecked = false;

$(document).ready(function () {

    $("#terms").click(function () {
        var checked_status = this.checked;
        isTermsChecked = checked_status === true;
        validator();
    });

    $("#cap-file").change(function () {
        var file = this.files.item(0);
        isSelectedfile = true;
        validator();
        $("#fName").html(file.name);
    });
});

function validator() {
    if (isTermsChecked === true && isSelectedfile === true)
        $("#upload-file-btn").removeAttr("disabled");
    else
        $("#upload-file-btn").attr("disabled", "disabled");
}