/*
 *
 * https://github.com/fatlinesofcode/ngDraggable
 */
var app = angular.module('dragDrop', []);

app.directive('draggable', function() {

    return {
        scope: {
            with: '='
        },
        link:function (scope, element) {
            // this gives us the native JS object
            var el = element[0];

            el.draggable = true;

            el.addEventListener(
                'dragstart',
                function (e) {
                    if (scope.with == '') {
                        alert('请先填写模块');
                        return false;
                    }
                    el.innerHTML = '拖动模型';
                    e.dataTransfer.effectAllowed = 'move';
                    this.classList.add('drag');
                    return false;
                },
                false
            );

            el.addEventListener(
                'dragend',
                function (e) {
                    el.innerHTML = '添加模型';
                    this.classList.remove('drag');
                    return false;
                },
                false
            );
        }
    }
});

app.directive('droppable', function() {
    return {
        scope: {
            drop: '&',
            bin: '=',
            identity:'='
        },
        link: function(scope, element) {
            // again we need the native object
            var el = element[0];

            el.addEventListener(
                'dragover',
                function(e) {
                    e.dataTransfer.dropEffect = 'move';

                    // allows us to drop
                    if (e.preventDefault) e.preventDefault();
                    this.classList.add('over');
                    return false;
                },
                false
            );

            el.addEventListener(
                'dragenter',
                function(e) {
                    this.classList.add('over');
                    return false;
                },
                false
            );

            el.addEventListener(
                'dragleave',
                function(e) {
                    this.classList.remove('over');
                    return false;
                },
                false
            );

            el.addEventListener(
                'drop',
                function(e) {
                    // Stops some browsers from redirecting.
                    if (e.stopPropagation) e.stopPropagation();
                    this.classList.remove('over');

                    //var item = document.getElementById(e.dataTransfer.getData('Text'));
                    $(this).append($('<div style="position:absolute;top:'+(e.offsetY-26)+'px;left:'+(getOffsetX(e)-26)+'px;border:1px solid #5c5c5c;width:50px;height:50px;text-align:center;font-size:12px;line-height:50px;">'+scope.bin+'</div>'));

                    if(scope.identity == true){
                        ++scope.bin;
                    }

                    // call the passed drop function
                    scope.$apply(function(scope) {
                        var fn = scope.drop();
                        if ('undefined' !== typeof fn) {
                           fn(scope.bin);
                        }
                    });

                    function getOffsetX(event){
                        var evt =event||window.event;
                        var srcObj = evt.target || evt.srcElement;
                        if (evt.offsetX){
                            return evt.offsetX;
                        }else{
                            var rect = srcObj.getBoundingClientRect();
                            var clientx = evt.clientX;
                            return clientx - rect.left;
                        }
                    }
                    
                    return false;
                },
                false
            );
        }
    }
});
