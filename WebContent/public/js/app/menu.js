function initStart(){
	var thisTime;
	var tt = false;
	window.asideBoxList = new Array($('.aside-box li').length-1);
	$('.aside-box li').on('click',function(){
		if(!tt){
			angular.bootstrap(document,['myApp']);
			tt = true;
		}
		var $this = $(this);
		var num = $this.index();
		$('.aside-box li').removeClass('hover').eq(num).addClass('hover');
		$('.nav-slide').addClass('hover');
		$('.nav-slide-o').hide().eq(num).show();
		if(!asideBoxList[num]){
			asideBoxList[num] = num;
			$this.scope().menuClick($this.attr('data-name'));
		}
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
				$.each(res.data,function(index,item){
					if(item.rid==string){
						luopanData.push({name:this.name,link:'http://'+location.host+location.pathname+'?rid='+item.rid,selected:true})	
					}else{
						luopanData.push({name:this.name,link:'http://'+location.host+location.pathname+'?rid='+item.rid})
					}			
				});
				console.log(luopanData);
				$('#luopanSvg').luopan({
					data:luopanData,
				});	
			}
	})
		   }
		})
	
}