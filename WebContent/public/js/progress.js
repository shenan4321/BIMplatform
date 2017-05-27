// jquery-progress插件
/* *
.progressbars{position: absolute;bottom:20px;right:40px;width: 250px;height: 58px;padding: 10px 15px;background: rgba(0,0,0,0.4);border-radius: 10px;}
.progressbars .text{color: #fff;font-size:12px;line-height: 18px;padding-bottom: 2px;margin-bottom: 8px;}
.progress-striped{width: 100%;height: 100%;background-image: -webkit-linear-gradient(to bottom, #d2e9a1, #b8d875,#a6cf61,#85bc41,#69af36,#5aa630,#5da33b);background-image: linear-gradient(to bottom, #d2e9a1, #b8d875,#a6cf61,#85bc41,#69af36,#5aa630,#5da33b);}
.progress{overflow: hidden;height: 20px;margin-bottom: 20px;background-color: #f5f5f5;border-radius: 4px;-webkit-box-shadow: inset 0 1px 2px rgba(0,0,0,.1);box-shadow: inset 0 1px 2px rgba(0,0,0,.1);}
.progress-bar{float: left;width: 100%;height: 100%;font-size: 12px;line-height: 20px;color: #fff;text-align: center;background-image: -webkit-linear-gradient(45deg,rgba(255,255,255,.15) 25%,transparent 25%,transparent 50%,rgba(255,255,255,.15) 50%,rgba(255,255,255,.15) 75%,transparent 75%,transparent);background-image: linear-gradient(45deg,rgba(255,255,255,.15) 25%,transparent 25%,transparent 50%,rgba(255,255,255,.15) 50%,rgba(255,255,255,.15) 75%,transparent 75%,transparent);-webkit-box-shadow: inset 0 -1px 0 rgba(0,0,0,.15);background-size: 40px 40px;box-shadow: inset 0 -1px 0 rgba(0,0,0,.15);-webkit-transition: width .6s ease;transition: width .6s ease;} 
 * */
!function($){
	
	var defaluts = {
		progress:0,
		max:100,
		title:'正在读取中'
	};
	
	$.progress = function(options){
        	var $this = this;
        	var opts = $.extend({}, defaluts, options);
			if($(".progress").length!=1){
				var boxHTML = '<div class="progressbars"><div class="progressdiv" id="progressDIV"><div class="text"><span id="progressTitle">loading</span> (<span id="progressNum">0</span>/<span id="progressTotal">0</span>)</div>'+
		        	'<div class="progress">'+
		            '<div class="progress-striped"  style="width:0%;" id="progressStriped">'+
		            '<div class="progress-bar"></div>'+
		            '</div></div></div></div>';
				$(boxHTML).appendTo("body");
			}
        	return {
        		update:function(options){
    				document.getElementById('progressTitle').innerHTML=options.title;
    			    document.getElementById('progressNum').innerHTML=options.progress;
    		        document.getElementById('progressTotal').innerHTML=options.max;
    				document.getElementById('progressStriped').style.width = (~~(options.progress/options.max*100))+'%';
        		},
        		hide:function(){
        			//$('.progressbars').hide();
        		}
        	}
        
    };
}(jQuery);

