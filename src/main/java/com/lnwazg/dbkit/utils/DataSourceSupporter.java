package com.lnwazg.dbkit.utils;

import javax.sql.DataSource;

import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * 连接池支持类<br>
 * 经过这种优化之后，可以最小化maven依赖：当需要某种连接池组件时，只要将其在pom中声明，即可正常使用。不需要的连接池组件可以放心地从pom中去掉，并且不会引发编译错误！
 * @author lnwazg@126.com
 * @version 2016年10月23日
 */
public class DataSourceSupporter
{
    /**
     * 当前的连接池种类
     */
    private static DataSourceProviderEnum currentDataSourceProvider = DataSourceProviderEnum.HikariCP;
    
    //    private static DataSourceProviderEnum currentDataSourceProvider = DataSourceProviderEnum.druid;
    
    static enum DataSourceProviderEnum
    {
        druid, //阿里巴巴的连接池组件
        HikariCP;//光速连接池组件
    };
    
    /**
     * 根据连接池的配置，选用合适的连接池
     * @author lnwazg@126.com
     * @param dbType 
     * @param url
     * @param username
     * @param password
     * @return
     */
    public static DataSource initAndReturn(DbType dbType, String url, String username, String password)
    {
        Logs.i("开始初始化" + currentDataSourceProvider.name() + "数据源...");
        
        //根据不同的数据库类型，加载对应的数据库驱动
        switch (dbType)
        {
            case mysql:
                Logs.i("开始加载" + dbType.name() + "数据库驱动...");
                try
                {
                    Class.forName("com.mysql.jdbc.Driver");
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
                break;
            case sqlite:
                Logs.i("开始加载" + dbType.name() + "数据库驱动...");
                try
                {
                    Class.forName("org.sqlite.JDBC");
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
                break;
            case oracle:
                Logs.i("开始加载" + dbType.name() + "数据库驱动...");
                try
                {
                    Class.forName("oracle.jdbc.driver.OracleDriver");
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
                break;
            default:
                Logs.e("无法识别的dbType:" + dbType);
                break;
        }
        
        switch (currentDataSourceProvider)
        {
            case druid:
                
                //                                DruidDataSource datasource = new DruidDataSource();
                //                                datasource.setUrl(url);
                //                                datasource.setUsername(username);
                //                                datasource.setPassword(password);
                //                                try
                //                                {
                //                                    datasource.init();
                //                                    Logs.i("DB数据源初始化成功！");
                //                                }
                //                                catch (SQLException e)
                //                                {
                //                                    e.printStackTrace();
                //                                }
                //                                return datasource;
                //                    Class<?> clazz = ClassKit.forName("com.alibaba.druid.pool.DruidDataSource");
                //                    Validate.notNull(clazz, "找不到类路径：com.alibaba.druid.pool.DruidDataSource，您可能未配置druid依赖！");
                //                    Object druidDataSource = clazz.newInstance();
                Object druidDataSource = ClassKit.newInstance("com.alibaba.druid.pool.DruidDataSource");
                ClassKit.invokeMethod(druidDataSource, "setUrl", new Class[] {String.class}, url);
                ClassKit.invokeMethod(druidDataSource, "setUsername", new Class[] {String.class}, username);
                ClassKit.invokeMethod(druidDataSource, "setPassword", new Class[] {String.class}, password);
                ClassKit.invokeMethod(druidDataSource, "init");
                return (DataSource)druidDataSource;
            case HikariCP:
                //                                HikariConfig config = new HikariConfig();
                //                                config.setJdbcUrl(url);
                //                                config.setUsername(username);
                //                                config.setPassword(password);
                //                                config.addDataSourceProperty("cachePrepStmts", "true");
                //                                config.addDataSourceProperty("prepStmtCacheSize", "250");
                //                                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                //                                HikariDataSource ds = new HikariDataSource(config);
                //                                return ds;
                Object hikariConfig = ClassKit.newInstance("com.zaxxer.hikari.HikariConfig");
                ClassKit.invokeMethod(hikariConfig, "setJdbcUrl", new Class[] {String.class}, url);
                ClassKit.invokeMethod(hikariConfig, "setUsername", new Class[] {String.class}, username);
                ClassKit.invokeMethod(hikariConfig, "setPassword", new Class[] {String.class}, password);
                
                //设置最大连接数
                ClassKit.invokeMethod(hikariConfig, "addDataSourceProperty", new Class[] {String.class, Object.class}, "maximumPoolSize", "50");
                ClassKit.invokeMethod(hikariConfig, "addDataSourceProperty", new Class[] {String.class, Object.class}, "cachePrepStmts", "true");
                ClassKit.invokeMethod(hikariConfig, "addDataSourceProperty", new Class[] {String.class, Object.class}, "prepStmtCacheSize", "250");
                ClassKit.invokeMethod(hikariConfig, "addDataSourceProperty", new Class[] {String.class, Object.class}, "prepStmtCacheSqlLimit", "2048");
                
                Object ds = ClassKit.newInstance("com.zaxxer.hikari.HikariDataSource", new Class[] {hikariConfig.getClass()}, hikariConfig);
                return (DataSource)ds;
            default:
                break;
        }
        return null;
    }
    
}
