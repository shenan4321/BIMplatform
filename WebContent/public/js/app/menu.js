function initStart(){
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
	
	//'./project/queryProjectByRid.do?rid='+string
	$.ajax({
		   url:"./project/queryProjectByRid.do?rid="+string,
		   type:"POST",
		   dataType:"json",
		   success:function(data){
				$.ajax({
					url:"./model/queryModelInfoByPid.do?pid="+data.data.pid,
					type:"POST",
					dataType:"json",
					success:function(res){
					var luopanData = [];
					$.each(res.data,function(item){
						if(item.rid==string){
							luopanData.push({name:this.name,link:'http://'+location.host+location.pathname+'?rid='+this.rid,selected:true})	
						}else{
							luopanData.push({name:this.name,link:'http://'+location.host+location.pathname+'?rid='+this.rid})
						}			
					});
					$('#luopanSvg').luopan({
						data:luopanData,
					});	
				}
		})
		   }
		})
	
}