/*
    version:1.0.0
*/
(function(){
    //类继承
    var extendClass = function(subClass, superClass){
        var F = function(){};

        F.prototype = superClass.prototype;　　
        subClass.prototype = new F();　　
     
        subClass.superclass = superClass.prototype;
      
    };
    /*
        模板控制器
    */
    var iTemplate = (function(){
        var template = function(){};
        template.prototype = {
            // 针对数组数据 iTemplate.makeList('<p a="{a}">{b}</p>', [{a:1,b:2},{a:22,b:33}] ) return '<p a="1">2</p><p a="22">33</p>'
            makeList: function(tpl, arr, fn){
                var res = [], $10 = [], reg = /{(.+?)}/g, json2 = {}, index = 0;
                for(var el = 0;el<arr.length;el++){
                    if(typeof fn === "function"){
                        json2 = fn.call(this, el, arr[el], index++)||{};
                    }
                    res.push(
                         tpl.replace(reg, function($1, $2){
                            return ($2 in json2)? json2[$2]: (undefined === arr[el][$2]? '':arr[el][$2]);
                        })
                    );
                }
                return res.join('');
            },
            // 针对单个数据 iTemplate.substitute('<p a="{a}">{b}</p>',{a:1,b:2}) return '<p a="1">2</p>'
            substitute: function(tpl, obj){
                if (!(Object.prototype.toString.call(tpl) === '[object String]')) {
                    return '';
                }
                if(!(Object.prototype.toString.call(obj) === '[object Object]' && 'isPrototypeOf' in obj)) {
                    return tpl;
                }
                //    /\{([^{}]+)\}/g
                return tpl.replace(/\{(.*?)\}/igm , function(match, key) {
                    if(typeof obj[key] != 'undefined'){
                        return obj[key];
                    }
                    return '';
                });
            }
        }
        return new template();
    })();
    // 缓存器
    var Cache = (function() {
        var a = {};
            return {
                set: function(b, c) {
                    a[b] = c
                },
                get: function(b) {
                    return a[b]
                },
                clear: function() {
                    a = {}
                },
                remove: function(b) {
                    delete a[b]
                }
            }
    } ());
    // 工具库
    var Util = {
        // 获取url 参数 http://www.baidu.com/q?name=1&age=2 return {name:1,age:2}
        getUrlParams : function(){
            var href = location.href,
                arr = href.split('?'),
                result = {},paramsArr;

            if(!arr[1]){
                return result;
            }
            // paramsArr ['aa=1','bb=2']
            paramsArr = arr[1].split('&');
            for(var i = 0; i<paramsArr.length; i++ ) {
                var keys = paramsArr[i].split('=');
                result[ keys[0] ] = keys[1];
            }

            return result;

        }
    };
    // UI库
    var UI = {
        // 返回对象宽和高 没有对象则返回window的宽和高
        getClient : function(e){
            if (e) {
                w = e.clientWidth;
                h = e.clientHeight;
            } else {
                w = (window.innerWidth) ? window.innerWidth : (document.documentElement && document.documentElement.clientWidth) ? document.documentElement.clientWidth : document.body.offsetWidth;
                h = (window.innerHeight) ? window.innerHeight : (document.documentElement && document.documentElement.clientHeight) ? document.documentElement.clientHeight : document.body.offsetHeight;
            }
            return {w:w,h:h};
        }
    };
    function QAQ(){

    }
    QAQ.Util = Util;
    QAQ.UI = UI;
    QAQ.Cache = Cache;
    QAQ.iTemplate = iTemplate;
    QAQ.substitute = iTemplate.substitute;
    QAQ.getUrlParams = Util.getUrlParams;
    QAQ.extendClass = extendClass;
    QAQ.getClient = UI.getClient;
    window['QAQ'] = QAQ;
})();

/*  
    弹出框插件

    var dialog = new QAQ.Dialog({
        id: '',//选填dialog的id
        cls: '.x1 .x2',//选填dialog的css类
        backdrop: false,//默认点击dialog背景时 不关闭dialog
        keyboard: true,//默认 按键盘escape 关闭dialog
        title: "对话框标题",
        content: '',//dialog的模板 3种模式  1. content:'<p>我是dialog的内容</p>' 2.contentUrl:'/views/dialog模板.html' 3.contentSelector:'#模板id'
        width: 420,//选填
        zIndex: 999,//
        cache: true,//是否对模板进行缓存
        modal: true, //是否显示遮罩
        renderTo: 'body',//暂时无用
        drag: false,//是否可拖拽
        winResize: false,//浏览器缩放时是否重新定位
        success: function(){
            // element是 dialog 原生dom
            var element = this.$element;
            //关闭
            this.close();
        }
    });
    //关闭
    dialog.close();

    注 若模板包含angularjs
    var dialog = new QAQ.AADialog({
        compile:$compile//必填
        scope:$scope//必填
    });


     */
(function( QAQ ){
    
    var getClient = QAQ.getClient;
    var getUrlParams = QAQ.getUrlParams;
    var MCache = QAQ.Cache;
    var substitute = QAQ.substitute;
    var extendClass = QAQ.extendClass;
   
    function BindAsEventListener (object, fun) {
        return function(event) {
            return fun.call(object, (event || window.event));
        }
    }

    function addEventHandler(oTarget, sEventType, fnHandler) {
        if (oTarget.addEventListener) {
            oTarget.addEventListener(sEventType, fnHandler, false);
        } else if (oTarget.attachEvent) {
            oTarget.attachEvent("on" + sEventType, fnHandler);
        } else {
            oTarget["on" + sEventType] = fnHandler;
        }
    };

    function removeEventHandler(oTarget, sEventType, fnHandler) {
        //console.log(oTarget.id,oTarget, sEventType, fnHandler)
        if (oTarget.removeEventListener) {
            oTarget.removeEventListener(sEventType, fnHandler, false);
        } else if (oTarget.detachEvent) {
            oTarget.detachEvent("on" + sEventType, fnHandler);
        } else { 
            oTarget["on" + sEventType] = null;
        }
    };

    var Bind = function(object, fun) {
        return function() {
            return fun.apply(object, arguments);
        }
    }
    /*
        拖拽插件
        new SimpleDrag({
            target:'运动对象',
            drag:'拖拽的dom对象'
        })
    */
    
    var SimpleDrag = function(){
        this.initialize.apply(this, arguments);
    };
    SimpleDrag.prototype = {
      //拖放对象,触发对象
      initialize: function(options) {
        var options = options || {};
        var drag = options.drag,target = options.target;
        this.Drag = $(target)[0];
        this._x = this._y = 0;
        this._fM = BindAsEventListener(this, this.Move);
        this._fS = Bind(this, this.Stop);
        this.Drag.style.position = "absolute";
        addEventHandler($(drag)[0], "mousedown", BindAsEventListener(this, this.Start));
      },
      //准备拖动
      Start: function(oEvent) {
        this._x = oEvent.clientX - this.Drag.offsetLeft;
        this._y = oEvent.clientY - this.Drag.offsetTop;
        addEventHandler(document, "mousemove", this._fM);
        addEventHandler(document, "mouseup", this._fS);
      },
      //拖动
      Move: function(oEvent) {
        this.Drag.style.left = oEvent.clientX - this._x + "px";
        this.Drag.style.top = oEvent.clientY - this._y + "px";
      },
      //停止拖动
      Stop: function() {
        removeEventHandler(document, "mousemove", this._fM);
        removeEventHandler(document, "mouseup", this._fS);
      }
    };

    
    var dialogTpl = '<div class="qaq sb_dialog_layer {_class_}" tabindex="-1" data-role="dialog"  id="{_id_}" >'+
                        '<div  class="sb_dialog_layer_main" data-role="main">'+
                            '<div class="sb_dialog_layer_title qaq-clearfix" data-role="header">'+
                                '<h3 data-role="title">{_title_}</h3>'+
                                '<a data-role="close" href="javascript:;" title="关闭" class="sb_dialog_btn_close">×</a>'+
                            '</div>'+
                            '<div class="sb_dialog_layer_cont" data-role="content">{_content_}</div>'+
                            '<div class="sb_tip_button" data-role="footer">{_footer_}</div>'+
                        '</div>'+
                    '</div>';

    var Dialog = function(options){
        var modalTpl = '<div class="sb_dialog_modal" data-role="modal"><a data-role="backdrop" href="javascript:;" class="sb_dialog_modal_link"></a></div>';
        
        this.dialogTpl = dialogTpl;
        this.modalTpl = modalTpl;
        this.setOptions(options);
        this.init();

    };

    Dialog.prototype = {
        showLoading:function(){
            var $element = this.$element;
            $($element).find('[data-role="content"]').html('<div style="padding:15px;text-align:center;">正在努力加载...</div>');
        },
        // 加载内容模板
        loadContentTpl:function(){
            var content = this.opts.content,
                contentUrl = this.opts.contentUrl,
                contentSelector = this.opts.contentSelector,
                cache = this.opts.cache,
                deferred = $.Deferred(),tpl,
                $element = this.$element;
           
            if(typeof contentUrl!='undefined'){
                
                if( MCache.get(contentUrl) && cache){
                    //angular下 不延时加载 就会报 $apply has already digest
                    setTimeout(function(){
                        deferred.resolve(MCache.get(contentUrl));
                    }, 30)
                    
                    
                }else{
                    this.showLoading();

                    $.ajax({
                        url: contentUrl,
                        type: 'GET',
                        dataType: 'html'
                    })
                    .done(function(result) {
                        //console.log("success",result);
                        MCache.set(contentUrl,result);
                        setTimeout(function(){
                            deferred.resolve(result);
                        }, 10);
                       
                    })
                    .fail(function() {
                        //console.log("error");
                        deferred.reject('获取内容失败');
                    })
                    .always(function() {
                       // console.log("complete");
                    });
                }
                    
            }else if(typeof contentSelector!='undefined'){
                tpl = $(contentSelector).html();
                
                setTimeout(function(){
                    deferred.resolve(tpl);
                }, 30)
                //deferred.resolve(tpl);

            }else if(typeof content === 'string'){
                
                setTimeout(function(){
                    deferred.resolve(content);
                }, 30)
                
            
            }else{
                setTimeout(function(){
                    deferred.reject('获取内容失败');
                }, 30);
                
            }

            return deferred.promise();
            
        },
        //设置参数
        setOptions:function(options){
            var options = options || {},opts = {},value,timeStamp = new Date().valueOf(),
                defaults = {
                    id: 'dialog_'+timeStamp,
                    backdrop: false,//'static' for a backdrop which doesn't close the modal on click.
                    keyboard: true,//Closes the modal when escape key is pressed
                    title: "对话框",
                    content: '',
                    zIndex:2014,
                    cache: true,
                    width: 420,
                    modal: true, 
                    destroy: true,
                    winResize: false,
                    buttons:[],
                    success:function(){}
                      /*  buttons: 
                     [
                        {
                            name  : '取消',
                            click: function(){
                                this.close();
                            }
                        },
                        {
                            name     : '确定',
                            click: function(){
                                this.close();
                            }
                        }
                    ]*/
                };

            opts = $.extend(defaults,options);
            opts.buttons = opts.buttons || [];
            for(var i = 0; i<opts.buttons.length; i++){
                if(typeof opts.buttons[i]['id'] === 'undefined'){
                    opts.buttons[i]['id'] = 'dialog-btn-'+timeStamp+i;
                }
            }
            this.dialogTpl = opts.dialogTpl?opts.dialogTpl:this.dialogTpl;
            this.opts = opts;
           
        },
        init:function(){
            this.renderUI();
            this.bindUI();
            this.syncUI();
        },
        //生成遮罩
        renderModal:function(){
            if(!this.opts.modal){
                return;
            }
            var modalTpl = this.modalTpl,height = getClient().h;
    
            this.$modal = $(modalTpl).appendTo($("body"));
            this.$modal[0].style.height = height+'px';
            this.$modal[0].style.zIndex = this.opts.zIndex-1;
        },
        // 绑定 ESC键
        bindKeyboard:function(){
            var keyboard = this.opts.keyboard;

            this._bindKeyboard = BindAsEventListener(this,function(event){
                var e = event ? event : window.event; 
                var keyCode = e.which ? e.which : e.keyCode;//获取按键值
                //console.log('keyCode:'+keyCode)
                // keycode 27 = Esc
                if(keyCode === 27 ){
                    this.close();
                }
                //console.log(e,e.which,e.keyCode)
            });
            //console.log('keyboard',keyboard)
            if(keyboard){
                addEventHandler(document,'keydown',this._bindKeyboard);
            }
        },
        bindUI:function(){
            var self=this,opts = this.opts;

            this.bindClose();
            this.bindFooterButtons();
            this.bindWindowResize();
            this.bindKeyboard();
            this.bindDrag();
            this.bindBackdrop();
        },
        // 点击dialog区域外时 自动关闭
        bindBackdrop:function(){
            if(false === this.opts.modal){
                return;
            }
            var $modal = this.$modal;
            this._onBackDropClick = BindAsEventListener(this,function(){
                //console.log('backdrop ',this.opts.backdrop)
                if(true === this.opts.backdrop){
                    this.close();
                }
            });
            addEventHandler($($modal).find('[data-role="backdrop"]')[0],'click',this._onBackDropClick);
        },
        // 绑定拖拽
        bindDrag:function(){
            var id = '#'+this.opts.id,drag = id +' [data-role="header"]';

            if(true === this.opts.drag){
                 this._drag = new SimpleDrag({
                    target:id,
                    drag:drag
                });
                $(drag)[0].style.cursor = 'move';
            }
               
        },
        bindFooterButtons:function(){
            var self = this,buttons = this.opts.buttons,button,$button,callback,id;

            for (var i = buttons.length - 1; i >= 0; i--) {
                button = buttons[i];
                callback = button.click;
                id = '#'+button.id;
                (function(id,callback,self){
                    
                    addEventHandler($(id)[0],'click',function(){
                        if(typeof callback === 'function'){
                            callback.call(self);
                        }
                    });
                    
                })(id,callback,self);
                    
            };
        },
        // 点击包含 data-role="close" 属性的dom  关闭dialog
        bindClose:function(){
            var self=this,opts = this.opts,$element = this.$element;

            this._close = BindAsEventListener(this,this.close);
            //addEventHandler( $($element).find('[data-role="close"]')[0],'click',this._close);

            $($element).delegate('[data-role="close"]', 'click', function(event) {
                self.close();
            });
            
        },
        //浏览器缩放重新定位
        bindWindowResize:function(){
            if(false === this.opts.winResize){
                return;
            }
            var self = this;
            this._onWindowResize = BindAsEventListener(this,function(){
                //console.log('_onWindowResize',this,self)
                this.initPosition();
            });
            addEventHandler(window,'resize',this._onWindowResize);
        },
        getContentHtml:function(){
            var self = this, $element = this.$element;
   
            this.loadContentTpl().done(function(content){

                self.renderContent(content);
                self.initPosition();
          
                $($element).animate({ opacity: 1 }, 200);
                //console.log('success',self.opts.success)
                self.opts.success && self.opts.success.call(self);

            }).fail(function(msg){
                self.showErrorMsg(msg);
            });
        },
        renderContent:function(content){
            var $element = this.$element;
            $($element).find('[data-role="content"]').html(content);
        },
        showErrorMsg:function(msg){
            var $element = this.$element;

            $($element).find('[data-role="content"]').html('<div style="padding:15px;text-align:center;">'+msg+'</div>');
        },
        getDialogHtml:function(){
            var opts = this.opts,
                buttons = opts.buttons,
                dialogTpl = this.dialogTpl,
                obj = {},footer='',btnFrag = [],btn,btnStr,cls,
                btnTpl = '<button id="{id}" class="btn {cls}">{name}</button>';
        
            for(var i=0;i<buttons.length;i++){
                btn = buttons[i];
                cls = btn.cls;
                if(cls){
                    btn.cls = cls.replace(/\./g,'');
                }
                btnStr = substitute(btnTpl,btn);
                btnFrag.push(btnStr);
            }
            
            
            opts['cls'] = opts['cls'] || '';
            opts['cls'] = opts['cls'].replace(/\./g,'');
            obj = {
                _id_:opts.id,
                _class_:opts.cls,
                _title_:opts.title,
                _content_:'',
                _footer_:btnFrag.join(' ')
            };


            var html = substitute(dialogTpl,obj);
        

            return html;
        },
        renderDialog:function(){
            //console.log(2222222222,this.opts)
            var html = this.getDialogHtml();
            //$("body")[0].insertAdjacentHTML("beforeEnd", html);
            $("body").append(html);
            this.$element = $('#'+this.opts.id)[0];
            this.getContentHtml();

        },
        renderUI:function(){
            var self=this,opts = this.opts;

            this.renderModal();
            this.renderDialog();
        },
        syncUI:function(){
            //this.initPosition();

        },
        initPosition:function(){
            var opts = this.opts,
                width = opts.width || 400,
                height ,
                contentHeight = opts.height,
                dialogHeight = height+64,
                winHeight = getClient().h,
                winWidth = getClient().w,
                scrollTop,zIndex = opts.zIndex,
                top,left,
                $element = this.$element;

            
            //假如底部按钮为空
            if(opts.buttons.length === 0){
                $($element).find('[data-role="footer"]')[0].style.display = 'none';
            }
           
            
            left =  Math.floor( (winWidth-width)*0.5 );

            if(contentHeight){
                $($element).find('[data-role="content"]')[0].style.height = contentHeight + 'px';
            }
            
            //谷歌下不能立即获取scrollTop
            setTimeout(function(){
                $element.style.width = width+'px';
                $element.style.left = left+'px';
                $element.style.zIndex = zIndex;
                height =  $element.offsetHeight;
                scrollTop = Math.max(document.documentElement.scrollTop,document.body.scrollTop)
                //top = Math.floor( (winHeight-height)*0.45+scrollTop );//position:absolute
                top = Math.floor( (winHeight-height)*0.45 );//position:fixed
                $element.style.top = top+'px';
               
            },50);
            
        },
        close:function(){
            var modal = this.$modal,dialog = this.$element;
            
            this.removeEventListeners();
            if(dialog){
                $(dialog).animate({
                    opacity: 0},
                    100, function() {
                    $(dialog).remove();
                    $(modal).remove();
                });
            }
            
        },
        destroy:function(){

        },
        // 去除监听
        removeEventListeners:function(){
            var self = this;
            if(this.opts.keyboard){
                removeEventHandler(document,'keydown',this._bindKeyboard);
            }
            if(this.opts.winResize){
                removeEventHandler(window,'resize',this._onWindowResize);
            }
            //removeEventHandler(document,'keydown',this._bindKeyboard);
            //removeEventHandler(window,'resize',this._onWindowResize);
        }
    };

    //angularjs下的dialog
    var AADialog = function(options){
        var options = options || {};
        var cls = options.cls || '';
        var scope = options.scope;
        var compile = options.compile;
        if(!scope){
            alert('缺少参数 scope ');
            return;
        }
        if(!compile){
            alert('缺少参数 compile ');
            return;
        }
       
        Dialog.call(this,options); 
       

    };
    extendClass(AADialog,Dialog);

    AADialog.prototype.renderContent=function(content){
        var $compile = this.opts.compile;
        var $scope = this.opts['scope'];
        var $element = this.$element;
        var link = $compile(content);
        var node = link($scope);
      
        $($element).find('[data-role="content"]').html(node);
        $scope.$apply();
    };
   
    // 通用对框框
    var MMDialog = {
        alert:function(msg,opts){
            var msg = msg || '';
            var content = '<div class="sb-alert-box">'+msg+'</div>';
            var options = {
                title:'警告框',
                content:content,
                buttons:[
                    {
                        name  : '确定',
                        cls:'.btn-primary',
                        click: function(){
                            this.close();
                        }
                    }
                ]
            };
            $.extend(options,opts || {} );
            new Dialog(options);
        },
        confirm:function(msg,callback,opts){
            var msg = msg || '';
            var content = '<div class="sb-confirm-box"><span class="sb-confirm-icon"></span>'+msg+'</div>';
            var options = {
                title:'确认框',
                content:content,
                buttons:[
                    {
                        name  : '确定',
                        cls:'.btn-primary',
                        click: function(){
                            this.close();
                            callback && callback();
                        }
                    },
                    {
                        name  : '取消',
                        cls:'.btn-default',
                        click: function(){
                            this.close();
                        }
                    }
                ]
            };
            $.extend(options,opts || {} );
            new Dialog(options);
            
        },
        info:function(msg,msgtype,opts){
            var msg = msg || '';
            var msgtype = msgtype || '';
            var msgclass = '';
            var content;
            switch(msgtype){
                case 'warning':
                    msgclass = 'sb-icon-warning';
                    break;
                case 'error':
                    msgclass = 'sb-icon-error';
                    break;
                case 'success':
                    msgclass = 'sb-icon-success';
                    break;
                case 'info':
                    msgclass = 'sb-icon-message';
                    break;
                default:
                    msgclass = 'sb-icon-message';
                    break;
            };

            /*<span class="sb-dialog-icon '+ msgclass +'"></span>*/
            content = '<div class="sb-info-box"><span class="sb-info-txt">'+msg+'</span></div>';

            var options = {
                title:'提示框',
                content:content,
                buttons:[
                    {
                        name  : '确定',
                        cls:'.btn-primary',
                        click: function(){
                            this.close();
                        }
                    }
                ]
            };
            $.extend(options,opts || {} );
            new Dialog(options);
        }

    };
    function centerElement($element,width,height){
        var win = getClient(),
            winHeight = win.h,
            winWidth = win.w,
            scrollTop,
            top,left;
            
            left =  Math.floor( (winWidth-width)*0.5 );

            $element.style.display = 'block';
            $element.style.width = width+'px';
            $element.style.left = left+'px';
            
            //谷歌下不能立即获取scrollTop
            setTimeout(function(){
                height =  height || $element.offsetHeight;
                scrollTop = Math.max(document.documentElement.scrollTop,document.body.scrollTop)
                top = Math.floor( (winHeight-height)*0.45 );
                $element.style.top = top+'px';
              
                
            },30);
    }
    // 146 * 146 加载中
    var Loading = {

        show:function(msg){
            var msg = msg || '正在加载中...';
            var loadingTpl = '<div id="sb-loading-box"><div class="sb-loading-cont"><span class="sb-loading-txt">{msg}</span></div></div>';
            if($('#sb-loading-box').length >0){
                $('#sb-loading-box').find('.sb-loading-txt').text(msg)
            }else{
                var frag = substitute(loadingTpl,{msg:msg});
                $('body').append(frag);
                centerElement($('#sb-loading-box')[0],146,146);
            }
            //$('#sb-loading-box').remove();
           

            
        },
        hide:function(){
            $('#sb-loading-box').remove();
        }
    };
    /*
    <div class="toast-bottom-full-width" id="toast-container">
        <div style="" class="toast toast-info">
            <div class="toast-message">用户信息暂无变更</div>
        </div>
    </div>

     */
    //
    //消息提示 msgtype: 1 success 2 error 3 warning 4 info
    function AMessage(msg,msgtype,opts) {
        var timeStamp = new Date().valueOf(),opts = opts || {};
        var id = 'toast-'+timeStamp;
        var msg = msg || '',msgtype = msgtype || 'info',interval = opts.interval || 5000,content;
        var messageWrapTpl = '<div class="toast-top-full-width" id="toast-container"></div>';
        var messageTpl = '<div style="" class="toast toast-{_msgtype_}" id="{_id_}" > <a class="toast-close" href="javascript:;" title="关闭">×</a> <div class="toast-message">{_message_}</div> </div>';
        var $container;

        //
        if( $('#toast-container').length>0 ){
            $container = $('#toast-container');
        }else{
            $container = $(messageWrapTpl).appendTo('body');
        }
        content = substitute( messageTpl,{ _message_:msg,_id_:id,_msgtype_:msgtype} );
        $container.append(content);
        // 绑定关闭
        $container.find('.toast-close').bind('click',function(event){
            var $target = $(event.currentTarget),$parent = $target.closest('.toast');

            $parent.fadeOut(600, function() {
                if( $('#toast-container .toast:visible').length == 0 ){
                    $('#toast-container').remove();
                }
            });
        });

        if('success' === msgtype){
            interval = opts.interval || 3000;
        }
        (function(id,interval){
           
            var timer = setTimeout(function(){
               
                $('#'+id).fadeOut(600,function(){
                    if( $('#toast-container .toast:visible').length == 0 ){
                        $('#toast-container').remove();
                    }
                 });

            }, interval);
        })(id,interval);
            
    };
    AMessage.show = function(){
        AMessage.apply(null,arguments);
    };
    AMessage.hide = function(){
        $('#toast-container').remove();
    };

    ScreenMask = {
        show: function (selector) {
            var selector = selector || 'body',tpl = '<div class="qaq-screen-mask" ><span></span></div>';
            this.hide();
            jQuery(selector).eq(0).append($(tpl));
        },
        hide: function (selector) {
            var selector = selector || 'body';
            jQuery(selector).find('.qaq-screen-mask').remove();
        }
    };
    
    QAQ.ScreenMask = ScreenMask;
    QAQ.Dialog = Dialog;
    QAQ.AADialog = AADialog;
    QAQ.MMDialog = MMDialog;
    QAQ.Loading = Loading;
    QAQ.Message = AMessage;

    $.extend(QAQ.Dialog,MMDialog);
   
})( QAQ );

/**
 * 绑定事件。
 *
 * `callback`方法在执行时，arguments将会来源于trigger的时候携带的参数。如
 * ```javascript
 * var obj = {};
 *
 * // 使得obj有事件行为
 * Mediator.installTo( obj );
 *
 * obj.on( 'testa', function( arg1, arg2 ) {
 *     console.log( arg1, arg2 ); // => 'arg1', 'arg2'
 * });
 *
 * obj.trigger( 'testa', 'arg1', 'arg2' );
 * ```
 *
 * 如果`callback`中，某一个方法`return false`了，则后续的其他`callback`都不会被执行到。
 * 切会影响到`trigger`方法的返回值，为`false`。
 *
 * `on`还可以用来添加一个特殊事件`all`, 这样所有的事件触发都会响应到。同时此类`callback`中的arguments有一个不同处，
 * 就是第一个参数为`type`，记录当前是什么事件在触发。此类`callback`的优先级比脚低，会再正常`callback`执行完后触发。
 * ```javascript
 * obj.on( 'all', function( type, arg1, arg2 ) {
 *     console.log( type, arg1, arg2 ); // => 'testa', 'arg1', 'arg2'
 * });
 * ```
 *
 * @method on
 * @grammar on( name, callback[, context] ) => self
 * @param  {String}   name     事件名，支持多个事件用空格隔开
 * @param  {Function} callback 事件处理器
 * @param  {Object}   [context]  事件处理器的上下文。
 * @return {self} 返回自身，方便链式
 * @chainable
 * @class Mediator
 */
(function( QAQ ){

    var Mediator = function() {
        var slice = [].slice,
            separator = /\s+/,
            protos;
    
        // 根据条件过滤出事件handlers.
        function findHandlers( arr, name, callback, context ) {
            return $.grep( arr, function( handler ) {
                return handler &&
                        (!name || handler.e === name) &&
                        (!callback || handler.cb === callback ||
                        handler.cb._cb === callback) &&
                        (!context || handler.ctx === context);
            });
        }
    
        function eachEvent( events, callback, iterator ) {
            // 不支持对象，只支持多个event用空格隔开
            $.each( (events || '').split( separator ), function( _, key ) {
                iterator( key, callback );
            });
        }
    
        function triggerHanders( events, args ) {
            var stoped = false,
                i = -1,
                len = events.length,
                handler;
    
            while ( ++i < len ) {
                handler = events[ i ];
    
                if ( handler.cb.apply( handler.ctx2, args ) === false ) {
                    stoped = true;
                    break;
                }
            }
    
            return !stoped;
        }
    
        protos = {
    
            on: function( name, callback, context ) {
                var me = this,
                    set;
    
                if ( !callback ) {
                    return this;
                }
    
                set = this._events || (this._events = []);
    
                eachEvent( name, callback, function( name, callback ) {
                    var handler = { e: name };
    
                    handler.cb = callback;
                    handler.ctx = context;
                    handler.ctx2 = context || me;
                    handler.id = set.length;
    
                    set.push( handler );
                });
    
                return this;
            },
    
            /**
             * 绑定事件，且当handler执行完后，自动解除绑定。
             * @method once
             * @grammar once( name, callback[, context] ) => self
             * @param  {String}   name     事件名
             * @param  {Function} callback 事件处理器
             * @param  {Object}   [context]  事件处理器的上下文。
             * @return {self} 返回自身，方便链式
             * @chainable
             */
            once: function( name, callback, context ) {
                var me = this;
    
                if ( !callback ) {
                    return me;
                }
    
                eachEvent( name, callback, function( name, callback ) {
                    var once = function() {
                            me.off( name, once );
                            return callback.apply( context || me, arguments );
                        };
    
                    once._cb = callback;
                    me.on( name, once, context );
                });
    
                return me;
            },
    
            /**
             * 解除事件绑定
             * @method off
             * @grammar off( [name[, callback[, context] ] ] ) => self
             * @param  {String}   [name]     事件名
             * @param  {Function} [callback] 事件处理器
             * @param  {Object}   [context]  事件处理器的上下文。
             * @return {self} 返回自身，方便链式
             * @chainable
             */
            off: function( name, cb, ctx ) {
                var events = this._events;
    
                if ( !events ) {
                    return this;
                }
    
                if ( !name && !cb && !ctx ) {
                    this._events = [];
                    return this;
                }
    
                eachEvent( name, cb, function( name, cb ) {
                    $.each( findHandlers( events, name, cb, ctx ), function() {
                        delete events[ this.id ];
                    });
                });
    
                return this;
            },
    
            /**
             * 触发事件
             * @method trigger
             * @grammar trigger( name[, args...] ) => self
             * @param  {String}   type     事件名
             * @param  {*} [...] 任意参数
             * @return {Boolean} 如果handler中return false了，则返回false, 否则返回true
             */
            trigger: function( type ) {
                var args, events, allEvents;
    
                if ( !this._events || !type ) {
                    return this;
                }
    
                args = slice.call( arguments, 1 );
                events = findHandlers( this._events, type );
                allEvents = findHandlers( this._events, 'all' );
    
                return triggerHanders( events, args ) &&
                        triggerHanders( allEvents, arguments );
            }
        };
    
        /**
         * 中介者，它本身是个单例，但可以通过[installTo](#WebUploader:Mediator:installTo)方法，使任何对象具备事件行为。
         * 主要目的是负责模块与模块之间的合作，降低耦合度。
         *
         * @class Mediator
         */
        return $.extend({
    
            /**
             * 可以通过这个接口，使任何对象具备事件功能。
             * @method installTo
             * @param  {Object} obj 需要具备事件行为的对象。
             * @return {Object} 返回obj.
             */
            installTo: function( obj ) {
                return $.extend( obj, protos );
            }
    
        }, protos );
    }();

    QAQ.Mediator = Mediator;
    
})( QAQ );
/**
 * This jQuery plugin displays pagination links inside the selected elements.
 *
 * @author Gabriel Birke (birke *at* d-scribe *dot* de)
 * @version 1.2
 * @param {int} maxentries Number of entries to paginate
 * @param {Object} opts Several options (see README for documentation)
 * @return {Object} jQuery Object
 */
(function(){
    //maxentries 记录总数  opts.$scope
   jQuery.fn.pagination = function(maxentries, opts){
        opts = jQuery.extend({
            items_per_page:10,//每页记录数目
            num_display_entries:10,//页码显示数
            current_page:0,
            num_edge_entries:2,//边界页码显示数
            link_to:"javascript:;",
            prev_text:"上一页",
            next_text:"下一页",
            ellipse_text:"...",
            prev_show_always:true,
            next_show_always:true,
            callback:function(page){
                return false;
            }
        },opts||{});
        
        return this.each(function() {
          
            /**
             * Calculate the maximum number of pages
             */
            function numPages() {
                return Math.ceil(maxentries/opts.items_per_page);
            }
            
            /**
             * Calculate start and end point of pagination links depending on 
             * current_page and num_display_entries.
             * @return {Array}
             */
            function getInterval()  {
                var ne_half = Math.ceil(opts.num_display_entries/2);
                var np = numPages();
                var upper_limit = np-opts.num_display_entries;
                var start = current_page>ne_half?Math.max(Math.min(current_page-ne_half, upper_limit), 0):0;
                var end = current_page>ne_half?Math.min(current_page+ne_half, np):Math.min(opts.num_display_entries, np);
                return [start,end];
            }
            
            /**
             * This is the event handling function for the pagination links. 
             * @param {int} page_id The new page number
             */
            function pageSelected(page_id, evt){
                current_page = page_id;
                drawLinks();
                /*--------------------majinhui-----------------------*/
                if(opts.$scope){
                    setTimeout(function(){
                        opts.$scope.$apply(function(){
                            opts.callback(page_id)
                        });
                    }, 10);
                        
                }else{
                    opts.callback(page_id);
                }
                    
     
                return;
                var continuePropagation = opts.callback(page_id, panel);
                if (!continuePropagation) {
                    if (evt.stopPropagation) {
                        evt.stopPropagation();
                    }
                    else {
                        evt.cancelBubble = true;
                    }
                }
                return continuePropagation;
            }
            
            /**
             * This function inserts the pagination links into the container element
             */
            function drawLinks() {
                panel.empty();
                var interval = getInterval();
                var np = numPages();
                // This helper function returns a handler function that calls pageSelected with the right page_id
                var getClickHandler = function(page_id) {
                    return function(evt){ return pageSelected(page_id,evt); }
                }
                // Helper function for generating a single link (or a span tag if it's the current page)
                var appendItem = function(page_id, appendopts){
                    page_id = page_id<0?0:(page_id<np?page_id:np-1); // Normalize page id to sane value
                    appendopts = jQuery.extend({text:page_id+1, classes:""}, appendopts||{});
                    type = appendopts.type;
                    //假如是当前页
                    if(page_id == current_page ){
                        if('prev' == type || 'next' == type){
                            var lnk = jQuery("<li class='disabled'><a  href='javascript:;'>"+(appendopts.text)+"</a></li>");
                        }else{
                            var lnk = jQuery("<li class='active'><a  href='javascript:;'>"+(appendopts.text)+"</a></li>");
                        }
                            
                    }
                    else
                    {
                        var lnk = jQuery("<li><a href='javascript:;'>"+(appendopts.text)+"</a></li>")
                            .bind("click", getClickHandler(page_id))
                            .attr('href', opts.link_to.replace(/__id__/,page_id));
                            
                            
                    }
                    if(appendopts.classes){lnk.addClass(appendopts.classes);}
                    panel.append(lnk);
                }
                // Generate "Previous"-Link
                if(opts.prev_text && (current_page > 0 || opts.prev_show_always)){
                    appendItem(current_page-1,{text:opts.prev_text, classes:"prev",type:'prev'});
                }
                // Generate starting points
                if (interval[0] > 0 && opts.num_edge_entries > 0)
                {
                    var end = Math.min(opts.num_edge_entries, interval[0]);
                    for(var i=0; i<end; i++) {
                        appendItem(i);
                    }
                    if(opts.num_edge_entries < interval[0] && opts.ellipse_text)
                    {
                        jQuery("<li><span>"+opts.ellipse_text+"</span></li>").appendTo(panel);
                    }
                }
                // Generate interval links
                for(var i=interval[0]; i<interval[1]; i++) {
                    appendItem(i);
                }
                // Generate ending points
                if (interval[1] < np && opts.num_edge_entries > 0)
                {
                    if(np-opts.num_edge_entries > interval[1]&& opts.ellipse_text)
                    {
                        jQuery("<li><span>"+opts.ellipse_text+"</span></li>").appendTo(panel);
                    }
                    var begin = Math.max(np-opts.num_edge_entries, interval[1]);
                    for(var i=begin; i<np; i++) {
                        appendItem(i);
                    }
                    
                }
                // Generate "Next"-Link
                if(opts.next_text && (current_page < np-1 || opts.next_show_always)){
                    appendItem(current_page+1,{text:opts.next_text, classes:"next",type:'next'});
                }
            }
            
            // Extract current_page from options
            var current_page = opts.current_page;
            // Create a sane value for maxentries and items_per_page
            maxentries = (!maxentries || maxentries < 0)?1:maxentries;
            opts.items_per_page = (!opts.items_per_page || opts.items_per_page < 0)?1:opts.items_per_page;
            // Store DOM element for easy access from all inner functions
            if(jQuery(this).find('ul').length>0){
                var panel = jQuery(this).find('ul');
            }else{
                var panel = jQuery('<ul class=" pagination"></ul>').appendTo( jQuery(this) );
            }
            
            
            // Attach control functions to the DOM element 
            this.selectPage = function(page_id){ pageSelected(page_id);}
            this.prevPage = function(){ 
                if (current_page > 0) {
                    pageSelected(current_page - 1);
                    return true;
                }
                else {
                    return false;
                }
            }
            this.nextPage = function(){ 
                if(current_page < numPages()-1) {
                    pageSelected(current_page+1);
                    return true;
                }
                else {
                    return false;
                }
            }
            // When all initialisation is done, draw the links
            drawLinks();
            jQuery(this).attr('data-pagination',true);
            //console.log(1111111,this,jQuery(this).data())

            // call callback function
            //opts.callback(current_page, this);
        });
    } 
})();

/*ScrollTo*/
(function( QAQ ){
    var intval = function (v)
    {
        v = parseInt(v);
        return isNaN(v) ? 0 : v;
    };
    var getPos = function (e)
    {
        var l = 0;
        var t  = 0;
        var w = intval(jQuery.css(e,'width'));
        var h = intval(jQuery.css(e,'height'));
        var wb = e.offsetWidth;
        var hb = e.offsetHeight;
        while (e.offsetParent){
            l += e.offsetLeft + (e.currentStyle?intval(e.currentStyle.borderLeftWidth):0);
            t += e.offsetTop  + (e.currentStyle?intval(e.currentStyle.borderTopWidth):0);
            e = e.offsetParent;
        }
        l += e.offsetLeft + (e.currentStyle?intval(e.currentStyle.borderLeftWidth):0);
        t  += e.offsetTop  + (e.currentStyle?intval(e.currentStyle.borderTopWidth):0);
        return {x:l, y:t, w:w, h:h, wb:wb, hb:hb};
    };
    var getClient = QAQ.getClient;

    var getScroll = function (e) 
    {
        if (e) {
            t = e.scrollTop;
            l = e.scrollLeft;
            w = e.scrollWidth;
            h = e.scrollHeight;
        } else  {
            if (document.documentElement && document.documentElement.scrollTop) {
                t = document.documentElement.scrollTop;
                l = document.documentElement.scrollLeft;
                w = document.documentElement.scrollWidth;
                h = document.documentElement.scrollHeight;
            } else if (document.body) {
                t = document.body.scrollTop;
                l = document.body.scrollLeft;
                w = document.body.scrollWidth;
                h = document.body.scrollHeight;
            }
        }
        return { t: t, l: l, w: w, h: h };
    };
    
    jQuery.fn.ScrollTo = function(s) {
        o = jQuery.speed(s);
        return this.each(function(){
            new ScrollTo(this, o);
        });
    };
    var ScrollTo = function (e, o)
    {
        var z = this;
        z.o = o;
        z.e = e;
        z.p = getPos(e);
        z.s = getScroll();

        //console.log(z.p,z.s)
        z.clear = function(){clearInterval(z.timer);z.timer=null};
        z.t=(new Date).getTime();
        z.step = function(){
            var t = (new Date).getTime();
            var p = (t - z.t) / z.o.duration;
            if (t >= z.o.duration+z.t) {
                z.clear();
                setTimeout(function(){z.scroll(z.p.y, z.p.x)},13);
            } else {
                st = ((-Math.cos(p*Math.PI)/2) + 0.5) * (z.p.y-z.s.t) + z.s.t;
                sl = ((-Math.cos(p*Math.PI)/2) + 0.5) * (z.p.x-z.s.l) + z.s.l;
                z.scroll(st, sl);
            }
        };
        z.scroll = function (t, l){window.scrollTo(l, t)};
        z.timer=setInterval(function(){z.step();},13);
    };

})( QAQ );
