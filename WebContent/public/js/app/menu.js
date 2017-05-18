$(function(){
	var thisTime;
	$('.aside-box li').click(function(){
		var thisUB	=	$('.aside-box li').index($(this));
		$('.aside-box li').removeClass('hover').eq(thisUB).addClass('hover');
		//if($.trim($('.nav-slide-o').eq(thisUB).html()) != ""){
			$('.nav-slide').addClass('hover');
			$('.nav-slide-o').hide();
			$('.nav-slide-o').eq(thisUB).show();
		//}
		
	})
	
	function thisMouseOut(){
		$('.nav-slide').removeClass('hover');
	}
	
	$('.nav-iconback').click(function(){
		 thisMouseOut();
	});
	
	$('#luopanSvg').luopan({});	
})