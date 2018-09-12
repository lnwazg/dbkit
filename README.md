# dbkit
超强的数据库中间件：

- 纯注解方式，实现基于mysql数据库业务的快速开发编码操作  
- 创新的接口注解传参方式，少写成吨的代码，轻松完成数据库开发
- 接口注解传参，支持：按名称&按顺序传递，这两种传参方式
- 自动sql注入拦截功能
- 数据库->Bean 反向生成器，一键由表结构自动生成Bean
- 自动化建表（无表则新建，否则不新建）
- 智能注解事务管理，全自动化事务管理
- 极简畅快的DAO开发体验
- 极简权限控制工具
- 根据数据库表自动生成png格式的表结构图工具
- 内置BI分析引擎，精巧的瑞士军刀，让你游刃有余分析任意来源的数据
- 基于DB Table的持久化集合框架支持
- 方便的数据库默认配置表工具DBConfigHelper
- 支持方法命名规则查询，例如:findByNameAndAgeOrderByIdDesc
- 支持内建的性能监测http接口，可视化查看数据库查询性能


- 小技巧：
A cycle was detected in the build path of project
2009年10月14日 13:34:00
阅读数：30847
解决Eclipse中Java工程间循环引用而报错的问题 
如果我们的项目包含多个工程（project），而它们之间又是循环引用的关系，那么Eclipse在编译时会抛出如下一个错误信息： 
“A cycle was detected in the build path of project: XXX” 
解决方法非常简单： 
Eclipse Menu -> Window -> Preferences... -> Java -> Compiler -> Building -> Building path problems -> Circular dependencies -> 将Error改成Warning

