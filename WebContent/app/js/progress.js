// jquery-progress插件

!function($){
	
	var defaluts = {
		progress:0,
		max:0,
		title:'正在读取中'
	};
	
	$.fn.extend({
        "progress":function(options){
        	var $this = this;
        	 var opts = $.extend({}, defaluts, options);
        	 
        	 opts.create = function(){
        		 var boxHTML = '<div class=".progressbars"><div class="progressdiv" id="progressDIV"><div class="text"><span id="progressTitle"></span> (<span id="progressNum"></span>/<span id="progressTotal"></span>)</div>'+
        		        '<div class="progress">'+
        		            '<div class="progress-striped"  style="width:0%;" id="progressStriped">'+
        		                '<div class="progress-bar"></div>'+
        		            '</div></div></div></div>';
        		 $(boxHTML).appendTo("body");
        	 };
        	 
			if($(".progress").length!=1){
				opts.create();
			}
        	return {
        		show:function(){
        			$this.each(function(options) {
        				var width= (~~(options.progress/options.max*100));
        				$('#progressStriped').css("width",width+"%");
        				$('#progressTitle').html(data.title);
        				$('#progressNum').html(data.progress);
        				$('#progressTotal').html(data.max);
        			});
        		},
        		hide:function(){
        			$('.progressbars').remove();
        		}
        	}
        }
    });
}(jQuery);

