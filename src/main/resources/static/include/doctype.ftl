<!DOCTYPE html>
<html>
<head>
	<base href="${base}">
  	<meta charset="utf-8">
	<meta name="renderer" content="webkit">
  	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  	<link rel="stylesheet" href="layui/css/layui.css"  media="all">
	<script>
	    var basePath = '${base}';
	    //兼容IE8、IE9等低版本浏览器找不到console对象的写法
	    window.console = window.console || (function(){
	        var c = {}; c.log = c.warn = c.debug = c.info = c.error = c.time = c.dir = c.profile = c.clear = c.exception = c.trace = c.assert = function(){};
	        return c;
	    })();
	</script>