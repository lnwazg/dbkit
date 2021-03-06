# dbkit 超强的数据库中间件：

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
- 支持方法命名规则查询增强，可查询所有（由findBy简化为find），语法为：find、query、findOrderByIdAndName、queryOrderByIdAndName（2019-2-15）
- 自带Bad SQL性能监控UI控制台，当激活“SQL_MONITOR”选项后SQL控制台也会启动，方便实时监控优化SQL性能
- 支持内建的性能监测http接口，可视化查看数据库查询性能
- 内置性能监控模块端口号可自适应(2018-9-19)
- 完美支持多数据源极速开发(2018-9-21)
- 实体类支持通用的时间戳模板父类，降低重复的业务代码（2018-10-6）
- 新增saveOrUpdate()方法（2018-10-13）
- 支持对SQLite这种不支持并发写的文件数据库进行写同步，开关为：DbKit.SQLITE_SYNC_WRITE（2018-10-29）

## 产品架构

### Level3 功能封装

- 自动建表工具
- 注解事务管理
- DB对象模板
- OLAP统计分析引擎
- 根据连接信息生成Entity对象的工具
- Entity对象模板，例如：时间模板
- DB结构图生成器
- JPA风格的查询引擎
- BadSQL监控层
- DB版HashMap容器
- 权限控制工具
- 多数据源支持
- SQLite串行写入支持层

### Level2 CRUD封装

- 封装了常用的CRUD API，更多API持续添加中。

### Level1 连接池驱动

- HikariCP

## 近期计划：

- 近期准备支持完整的JPA method name like sql invoking  
- AOP切面事务除了支持注解外，还要支持通配符过滤  
- 做一次性能压测，找出框架（可能存在的）性能瓶颈
- 引入主流框架的所有API