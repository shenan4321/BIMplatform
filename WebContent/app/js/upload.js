'use strict';

(function (app) {

    app.controller( 'upload', ['$routeParams', '$timeout', '$http','$scope','$rootScope','tips', function ($routeParams, $timeout, $http,$scope,$rootScope,tips) {

    	 var oDiv = $("#drop_area").get(0);
         var oP = $("#preview");
         //进入
         oDiv.ondragenter = function() {
             oP.html('');
         };
         //移动，需要阻止默认行为，否则直接在本页面中显示文件
         oDiv.ondragover = function(e) {

             e.preventDefault();
         };
         //离开
         oDiv.onleave = function() {
             oP.html('请将图片文件拖拽至此区域！');
         };
         //拖拽放置，也需要阻止默认行为
         oDiv.ondrop = function(e) {
             e.preventDefault();
             //获取拖拽过来的对象,文件对象集合
             var fs = e.dataTransfer.files;
             //若为表单域中的file标签选中的文件，则使用form[表单name].files[0]来获取文件对象集合
             //打印长度
             console.log(fs.length);
             //循环多文件拖拽上传
             for (var i = 0; i < fs.length; i++) {
                 //文件类型
                 var _type = fs[i].type;
                 console.log(_type);
                 //判断文件类型
                 if (_type.indexOf('image') == -1) {
                     //文件大小控制
                     //console.log(fs[i].size);
                     //读取文件对象
                     var reader = new FileReader();
                     //读为DataUrl,无返回值
                     reader.readAsDataURL(fs[i]);
                     reader.onloadstart = function(e) {
                         //开始加载
                     };
                     // 这个事件在读取进行中定时触发
                     reader.onprogress = function(e) {
                         $("#total").html(e.total);
                     };
                     //当读取成功时触发，this.result为读取的文件数据
                     reader.onload = function() {

                         var xhr = new XMLHttpRequest();

                         xhr.addEventListener("load", function(e) {
                             var result = JSON.parse(this.response);
                         }, false);
                         xhr.open("POST", '/BIMplatform/uploadAndDeserializeSave.do');

                         var formData = new FormData();
                         formData.append("action", "file");
                         formData.append("file", fs[0]);

                         xhr.send(formData);
                     };
                     //无论成功与否都会触发
                     reader.onloadend = function() {
                         if (reader.error) {
                             console.log(reader.error);
                         } else {
                             //上传没有错误，ajax发送文件，上传二进制文件
                         }
                     }
                 } else {
                     alert('请上传图片文件！');
                 }
             }

         }
    	
    	
    }]);

})(angular.module('upload', []));
