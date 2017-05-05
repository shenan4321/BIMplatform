<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE HTML>
<html>
  <head>
    <base href="<%=basePath%>">
    <title>东链博BIM管理系统</title>
    <style>
    	html,body,.page-container{width:100%;height:100%;overflow:hidden;position:relative}
    	.bin {
            min-height: 150px;
            min-width: 150px;
            float: left;
            border: 1px solid red;
            padding: 20px;
        }
        .item {
            min-height: 150px;
            min-width: 150px;
            background: blue;
            float: right;
            transition: all 0.2s ease;
        }
        .drag {
            opacity: 0.5;
        }
        .over {
            background:#ffe1e1;
        }
    </style>	
  </head>
  <body>
  	<div id="dlbPage" class="page-container" ng-view>
  	</div>	
  </body>
  <script>
  	var Dlb = {};
  </script>
<script src="public/js/jquery.js"></script>
<script src="public/js/angularjs/1.4.4/angular.min.js"></script>
<script src="public/js/angularjs/1.4.4/angular-resource.min.js"></script>
<script src="public/js/angularjs/1.4.4/angular-route.min.js"></script>
<script src="public/js/angularjs/1.4.4/angular-sanitize.min.js"></script>
<script src="public/js/angularjs/1.4.4/angular-animate.min.js"></script>
<script src="public/js/angularjs/1.4.4/angular-touch.min.js"></script>
<script src="app/config/config.js"></script>
<script src="app/config/app.js"></script>




</html>