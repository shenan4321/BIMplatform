window.luopanEvent = {};
(function ($) {
    var defaluts = {
        data:[{name:'建议中文', link:'www.baidu.com', onClick:function(){}}],//有onClick就不写link
        foreground: 'red',
        background: 'yellow',
        id:'luopanSvg',
        width:192,
        roundWidth:25, //内边环距
        roundR:4 //内侧宽度
    };

    $.extend({
        "luopan": function (options) {
            var opts = $.extend({}, defaluts, options);

            idBox = document.getElementById(opts.id);
            var halfWidth = opts.width/2;
            var innerWidth = halfWidth - opts.roundWidth - 1;//90
            var innestWidth = halfWidth - opts.roundWidth - 1 - (opts.roundR *1.5); //75
            var innestX = halfWidth -  innestWidth; //40
            var p3R = opts.width/2 - (opts.roundWidth + 1 -10);

            var o = this; //创建元素

            idBox.setAttribute("width", opts.width);//变成它的
            idBox.setAttribute("height", opts.width);//变成它的

            var svgG = document.createElementNS("http://www.w3.org/2000/svg","g");
            svgG.setAttribute('id',"itemsContainer");

            this.createDom = function(data){

                var len = data.length;
                var angle = 360/len;
                if(len<4) angle = 120;
                var t= Math.PI * angle / 180;
                sinB =  Math.sin(t);
                cosB =  Math.cos(t);
                sinB = (sinB==6.123233995736766e-17) ? 0:sinB;
                cosB = (cosB==6.123233995736766e-17) ? 0:cosB;

                opts.path1 = 'M 0 '+  halfWidth +' A ' +  halfWidth + ' ' +  halfWidth + ' 0 0 1 ' +   (halfWidth * (1-cosB)) +' '+   (halfWidth * (1-sinB))  +  ' L ' +   (halfWidth - innerWidth*cosB) +' ' + (halfWidth - innerWidth*sinB) + ' A '
                    + ( halfWidth - opts.roundWidth -1 ) + ' ' + ( halfWidth - opts.roundWidth -1 ) + ' 0 0 0 '+opts.roundWidth + ' ' +  halfWidth + ' Z';

                opts.path2=  'M '+opts.roundWidth +' '+halfWidth+' A '+innerWidth+' '+innerWidth + ' 0 0 1 '+ (halfWidth - innerWidth*cosB) +' ' + (halfWidth - innerWidth*sinB)  +' L '
                    +(halfWidth - (innestWidth)*cosB) +' '+(halfWidth - (innestWidth)*sinB)+' A '+innestWidth+' '+ innestWidth +' 0  0  0  '+ innestX +'  '+halfWidth+' Z';


                opts.path3=  'M '+ (opts.roundWidth + 1 -10 ) +' '+ halfWidth +' A '+ p3R+' '+ p3R +' 0 0 1 '+ (halfWidth - p3R*cosB) +' ' + (halfWidth - p3R*sinB) ;

                for(var i=0;i<len;i++){

                    var svgItem = document.createElementNS("http://www.w3.org/2000/svg","a");
                    svgItem.setAttribute('class',data[i].selected ?'svg-item-selected':'svg-item');
                    svgItem.setAttribute('item',"item-"+i);
                    svgItem.setAttribute('role','link');

                    if($.isFunction(data[i].onClick)){
                        luopanEvent['s'+i] = data[i].onClick;
                        svgItem.setAttributeNS('http://www.w3.org/1999/xlink',"xlink:href","javascript:luopanEvent['s"+i+"']("+i+")");
                    }else{
                        svgItem.setAttributeNS('http://www.w3.org/1999/xlink',"xlink:href","javascript:"+("window.location.href='"+data[i].link+"'"||'void(0)'));
                    }


                    svgItem.setAttribute('transform','rotate('+angle*i+','+halfWidth+','+halfWidth+')');
                    var path1 = document.createElementNS("http://www.w3.org/2000/svg","path");
                    path1.setAttribute("fill","none");
                    path1.setAttribute("opacity", '0.7');
                    path1.setAttribute("class",'sector');
                    path1.setAttribute("d", opts.path1);


                    var path2 = document.createElementNS("http://www.w3.org/2000/svg","path");
                    path2.setAttribute("fill","none");
                    path2.setAttribute("opacity", '0.7');
                    path2.setAttribute("class",'decsec');
                    path2.setAttribute("d", opts.path2);

                    var defs = document.createElementNS("http://www.w3.org/2000/svg","defs");
                    var path3 = document.createElementNS("http://www.w3.org/2000/svg","path");
                    path3.setAttribute("d", opts.path3);
                    path3.setAttribute("id", 'path'+i);
                    defs.appendChild(path3);

                    var stext = document.createElementNS("http://www.w3.org/2000/svg","text");
                    stext.setAttribute("x","0");
                    stext.setAttribute("y","0");
                    stext.setAttribute("dx",~~(0.5*( (2*Math.PI* p3R * angle/360) - 12 * data[i].name.length ))+2 );
                    stext.setAttribute("dy",data[i].dy||2);
                    stext.setAttribute("font-size","12");
                    stext.setAttribute("fill","#fff");
                    stext.setAttribute("class","svg-txt");
                    var textString = document.createTextNode(data[i].name);

                    var stextPath = document.createElementNS("http://www.w3.org/2000/svg","textPath");
                    stextPath.setAttributeNS('http://www.w3.org/1999/xlink',"xlink:href",'#path'+i);
                    stextPath.appendChild(textString);
                    stext.appendChild(stextPath);

                    svgItem.appendChild(path1);
                    svgItem.appendChild(path2);
                    svgItem.appendChild(defs);
                    svgItem.appendChild(stext);
                    svgG.appendChild(svgItem);

                }
                idBox.appendChild(svgG);
            };

            this.createDom(opts.data);
            return {
                createDom : o.createDom,
                removeDom:function(){
                    document.getElementById('itemsContainer').innerHTML = '';
                },
                updateDom : function(data){
                	this.removeDom();
                	this.createDom(data);
                }
            };

        }
    });

})(window.jQuery);

