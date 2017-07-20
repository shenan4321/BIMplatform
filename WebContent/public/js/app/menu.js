function initStart(){
	var thisTime;
	window.tt = false;
	window.asideBoxList = new Array($('.aside-box li').length-1);
	if(!tt){
		angular.bootstrap(document,['myApp']);
		tt = true;
	}
	
	$('.aside-box li').on('click',function(){	
		var $this = $(this);
		var num = $this.index();
		$('.aside-box li').removeClass('hover').eq(num).addClass('hover');
		$('.nav-slide').addClass('hover');
		$('.nav-slide-o').hide().eq(num).show();
		if(!asideBoxList[num]){
			asideBoxList[num] = num;
			$this.scope().menuClick($this.attr('data-name'));
		}
	});
	$('#luopanBox1').on('click',function(){
		$('#luopanBox1').hide();
		$('#luopanBox').show();
	});
	$('#luopanBox').on('click',function(){
		$('#luopanBox').hide();
		$('#luopanBox1').show();
	});
	function thisMouseOut(){
		$('.nav-slide').removeClass('hover');
	}
	
	$('.nav-iconback').click(function(){
		 thisMouseOut();
		 $('.nav-slide.hover').removeClass('hover');
		 $('.aside-box li.hover').removeClass('hover');
	});
	
	//'./project/queryProjectByRid.do?rid='+string
/*	$.ajax({
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
				$.each(res.data,function(index,item){
					if(item.rid==string){
						luopanData.push({name:this.name,link:'http://'+location.host+location.pathname+'?rid='+item.rid,selected:true})	
					}else{
						luopanData.push({name:this.name,link:'http://'+location.host+location.pathname+'?rid='+item.rid})
					}			
				});
				$('#luopanSvg').luopan({
					data:luopanData,
				});	
			}
	})
		   }
		})*/
	
}