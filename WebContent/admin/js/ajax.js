function validateCallback(form, callback, confirmMsg) {
	var $form = $(form);
	var formData = new FormData(form);
	if ($.html5Validate.isAllpass(form)) {
		$.ajax({
			type: form.method || 'POST',
			url:$form.attr("action"),
		    cache: false,
		    data: formData,
		    processData: false,
		    contentType: false
		}).done(function(res) {
			if(res.success){
				alert('增加成功');
			}
		}).fail(function(res) {
			alert(res);
		});   
    }
	return false;
}