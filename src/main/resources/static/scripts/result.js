$(document).ready(function() {
	$("#inputTaskId").on('input', function() {
		if (this.value.length > 0)
			$("#getResultBtn").removeAttr("disabled");
		else
			$("#getResultBtn").attr("disabled", "disabled");
	});
});