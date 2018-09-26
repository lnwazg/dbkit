	<#include "../include/doctype.ftl"/>
	<title>SQL执行统计</title>
</head>
<body>
	<div class="layui-tab layui-tab-card">
	  <ul class="layui-tab-title">
	    <li class="layui-this">近访SQL</li>
	    <li>Bad SQL</li>
	  </ul>
	  <div class="layui-tab-content">
	    <div class="layui-tab-item layui-show">
	    	<table class="layui-hide" id="recentSqls"></table>
	    </div>
	    <div class="layui-tab-item">
	    	<table class="layui-hide" id="badSqls"></table>
	    </div>
	  </div>
	</div>
	
	<script src="layui/layui.js" charset="utf-8"></script>
	<script>
		layui.use(['table','element'], function(){
		  var table = layui.table;
		  table.render({
		    elem: '#recentSqls'
		    ,url:'../monitor/queryAllSqlData.do'
		    ,cellMinWidth: 80 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
		    ,cols: [[
		      {field:'sql', width:1200, title: 'sql语句', sort: true}
		      ,{field:'cost', width:120, title: '耗时毫秒数', sort: true}
		    ]]
		     ,page: true,
		     limit:10
		  });
		  table.render({
			    elem: '#badSqls'
			    ,url:'../monitor/queryBadSqlData.do'
			    ,cellMinWidth: 80 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
			    ,cols: [[
			      {field:'sql', width:1200, title: 'sql语句', sort: true}
			      ,{field:'cost', width:120, title: '耗时毫秒数', sort: true}
			    ]]
			  });
		});
	</script>
</body>
</html>