$(document).ready(function() {

	$("#terms").click(function() {
		var checked_status = this.checked;
		if (checked_status == true) {
			$("#upload-file-btn").removeAttr("disabled");
		} else {
			$("#upload-file-btn").attr("disabled", "disabled");
		}
	});

	$("#cap-file").change(function() {
		var file = this.files.item(0);
		$("#fname").html(file.name);
	});
});