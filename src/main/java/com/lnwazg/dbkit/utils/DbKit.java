package com.lnwazg.dbkit.utils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.jdbc.impl.ext.MysqlJdbcSupport;
import com.lnwazg.dbkit.jdbc.impl.ext.OracleJdbcSupport;
import com.lnwazg.dbkit.jdbc.impl.ext.SqliteJdbcSupport;
import com.lnwazg.dbkit.proxy.DaoProxy;
import com.lnwazg.dbkit.proxy.ServiceProxy;
import com.lnwazg.dbkit.tools.sqlmonitor.SQLMonitor;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.property.PropertyUtils;
import com.lnwazg.kit.reflect.ClassKit;
import com.lnwazg.kit.singleton.B;

/**
 * 数据库工具包
 * @author nan.li
 * @version 2016年5月20日
 */
public class DbKit
{
    /**
     * 默认的配置文件路径
     */
    public static final String DEFAULT_CONFIG_FILEPATH = "das.properties";
    
    /**
     * 数据库连接的url
     */
    public static String URL = "";
    
    public static String USERNAME = "";
    
    public static String PASSWORD = "";
    
    /**
     * 数据库名称。根据连接字符串中的参数解析获得
     */
    public static String SCHEMA_NAME = "";
    
    /**
     * 数据库类型
     */
    public static DbType dbType = null;
    
    /**
     * 是否是调试模式<br>
     * 调试模式可以看到更多有用的信息
     */
    public static boolean DEBUG_MODE = false;
    
    /**
     * 执行的SQL监控工具，可以统计慢sql
     */
    public static boolean SQL_MONITOR = false;
    
    /**
     * 对于SQLITE数据库，是否对写操作进行同步<br>
     * 默认开启同步，防止并发写对SQLITE带来的问题
     */
    private static boolean SQLITE_SYNC_WRITE = true;
    
    /**
     * 加载默认配置位置的数据源
     * @author nan.li
     * @return
     */
    public static DataSource getDefaultDataSource()
    {
        return getDataSource(DEFAULT_CONFIG_FILEPATH);
    }
    
    public static MyJdbc getDefaultJdbc()
    {
        return getJdbc(DEFAULT_CONFIG_FILEPATH);
    }
    
    /**
     * 获取jdbc连接工具的实例<br>
     * 最原始的方法
     * @author nan.li
     * @param configPath
     * @return
     */
    private static MyJdbc getJdbc0(String configPath)
    {
        Map<String, String> configs = PropertyUtils.load(DbKit.class.getClassLoader().getResourceAsStream(configPath));
        if (configs.isEmpty())
        {
            Logs.e(String.format("配置文件%s不存在！因此无法初始化DataSource！", configPath));
            return null;
        }
        return getJdbc(configs.get("url"), configs.get("username"), configs.get("password"));
    }
    
    /**
     * 获取jdbc连接工具的实例<br>
     * 优先从缓存表中获取；若获取不到，才重新初始化实例
     * @author nan.li
     * @param configPath
     * @return
     */
    public static MyJdbc getJdbc(String configPath)
    {
        if (StringUtils.isEmpty(configPath))
        {
            Logs.e("configPath cannot be empty!");
            return null;
        }
        if (configToJdbcMapCache.containsKey(configPath))
        {
            Logs.i("get MyJdbc from cache...");
            return configToJdbcMapCache.get(configPath);
        }
        MyJdbc jdbc = getJdbc0(configPath);
        configToJdbcMapCache.put(configPath, jdbc);
        return jdbc;
    }
    
    public static Map<String, MyJdbc> configToJdbcMapCache = new HashMap<>();
    
    public static MyJdbc getJdbc(String url, String username, String password)
    {
        String key = String.format("%s%s%s", url, username, password);
        if (configToJdbcMapCache.containsKey(key))
        {
            Logs.i("get MyJdbc from cache...");
            return configToJdbcMapCache.get(key);
        }
        DataSource datasource = getDataSource(url, username, password);
        MyJdbc myJdbc = getJdbcFromDataSource(datasource);
        configToJdbcMapCache.put(key, myJdbc);
        initExtra();
        return myJdbc;
    }
    
    /**
     * 初始化其他东西
     * @author nan.li
     */
    private static void initExtra()
    {
        SQLMonitor.initSqlMonitor();
    }
    
    private static MyJdbc getJdbcFromDataSource(DataSource datasource)
    {
        if (datasource != null)
        {
            try
            {
                switch (dbType)
                {
                    case mysql:
                        return new MysqlJdbcSupport(datasource, SCHEMA_NAME);
                    case oracle:
                        return new OracleJdbcSupport(datasource, SCHEMA_NAME);
                    case sqlite:
                        MyJdbc myJdbc = new SqliteJdbcSupport(datasource, SCHEMA_NAME);
                        if (SQLITE_SYNC_WRITE)
                        {
                            myJdbc = DaoProxy.proxyDaoInterface(SqliteJdbcSupport.class, myJdbc);
                        }
                        
                        myJdbc.execute("PRAGMA synchronous=OFF;");//关闭同步，进入sqlite的极速模式！
                        myJdbc.execute("PRAGMA journal_mode=WAL;");//write ahead log，性能大幅增强
                        return myJdbc;
                    default:
                        return new MysqlJdbcSupport(datasource, SCHEMA_NAME);
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * 加载指定配置位置的数据源
     * @author nan.li
     * @param configPath
     * @return
     */
    public static DataSource getDataSource(String configPath)
    {
        Map<String, String> configs = PropertyUtils.load(DbKit.class.getClassLoader().getResourceAsStream(configPath));
        if (configs.isEmpty())
        {
            Logs.e(String.format("配置文件%s不存在！因此无法初始化DataSource！", configPath));
            return null;
        }
        return getDataSource(configs.get("url"), configs.get("username"), configs.get("password"));
    }
    
    public static Map<String, DataSource> dsCache = new HashMap<>();
    
    /**
     * 根据几个参数获取数据源<br>
     * 并且解析出数据库名称以及数据库类型
     * @author nan.li
     * @param url
     * @param username
     * @param password
     * @return
     */
    public static DataSource getDataSource(String url, String username, String password)
    {
        //DataSource重复利用，避免重复加载浪费资源
        String key = String.format("%s%s%s", url, username, password);
        if (dsCache.containsKey(key))
        {
            Logs.i("get dataSource from cache...");
            return dsCache.get(key);
        }
        if (StringUtils.startsWith(url, "jdbc:mysql"))
        {
            dbType = DbType.mysql;
            //假如是mysql的数据库
            //则去主动获取到schemaName
            SCHEMA_NAME = StringUtils.substring(url, StringUtils.lastIndexOf(url, "/") + "/".length());
            if (StringUtils.indexOf(SCHEMA_NAME, "?") != -1)
            {
                //去除后面的参数
                SCHEMA_NAME = StringUtils.substring(SCHEMA_NAME, 0, StringUtils.indexOf(SCHEMA_NAME, "?"));
            }
            Logs.i(String.format("解析到的schemaName: %s", SCHEMA_NAME));
        }
        else if (StringUtils.startsWith(url, "jdbc:sqlite"))
        {
            //jdbc:sqlite://e:/fileTree.db
            dbType = DbType.sqlite;
        }
        else if (StringUtils.startsWith(url, "jdbc:oracle"))
        {
            //jdbc:oracle:thin:@192.168.0.129:1521:orcl
            dbType = DbType.oracle;
            //schema不一定是orcl，schema更像是用户名，也就是当前用户
            SCHEMA_NAME = username;
        }
        else
        {
            Logs.e("无法识别的数据库类型，url=" + url);
            return null;
        }
        URL = url;
        USERNAME = username;
        PASSWORD = password;
        
        //根据具体的dataSourceSupporter去初始化具体的连接池
        DataSource dataSource = DataSourceSupporter.initAndReturn(dbType, url, username, password);
        dsCache.put(key, dataSource);
        return dataSource;
    }
    
    /**
     * 批量提交的数目
     */
    public static final int DEFAULT_BATCH_SIZE = 200;
    
    /**
     * 包扫描，并初始化DAO层
     * @author nan.li
     * @param packageName
     * @param jdbc
     */
    public static void packageSearchAndInitDao(String packageName, MyJdbc jdbc)
    {
        Logs.i("begin package search dao...");
        List<Class<?>> cList = ClassKit.getPackageAllClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                Logs.i(String.format("[package search] init %s ", clazz.getName()));
                
                //如果MyJdbc是clazz的父类的话，那么就需要对该DAO进行代理
                if (MyJdbc.class.isAssignableFrom(clazz))
                {
                    Object daoObject = DaoProxy.proxyDaoInterface(clazz, jdbc);//根据接口生成动态代理类
                    B.s(clazz, daoObject);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 自适应数据源并初始化DAO层
     * @author nan.li
     * @param packageName
     */
    public static void packageSearchAndInitDao(String packageName)
    {
        Logs.i("begin package search dao...");
        List<Class<?>> cList = ClassKit.getPackageAllClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                Logs.i(String.format("[package search] init %s ", clazz.getName()));
                
                //如果MyJdbc是clazz的父类的话，那么就需要对该DAO进行代理
                if (MyJdbc.class.isAssignableFrom(clazz))
                {
                    
                    String dsName = "";
                    if (clazz.isAnnotationPresent(com.lnwazg.dbkit.anno.dao.DataSource.class))
                    {
                        com.lnwazg.dbkit.anno.dao.DataSource dataSource = clazz.getAnnotation(com.lnwazg.dbkit.anno.dao.DataSource.class);
                        dsName = dataSource.value();
                    }
                    MyJdbc jdbc = DsManager.getDataSourceByName(dsName);
                    if (StringUtils.isNotEmpty(dsName))
                    {
                        Logs.i("使用数据源:" + dsName + "初始化DAO：" + clazz.getSimpleName());
                    }
                    else
                    {
                        Logs.i("使用默认数据源初始化DAO：" + clazz.getSimpleName());
                    }
                    Object daoObject = DaoProxy.proxyDaoInterface(clazz, jdbc);//根据接口生成动态代理类
                    B.s(clazz, daoObject);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 包扫描，并初始化Service层
     * @author nan.li
     * @param packageName
     * @param jdbc
     */
    public static void packageSearchAndInitService(String packageName, MyJdbc jdbc)
    {
        Logs.i("begin package search service...");
        List<Class<?>> cList = ClassKit.getPackageAllClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                Logs.i(String.format("[package search] init %s ", clazz.getName()));
                //                com.lnwazg.service.UserService$1 
                if (clazz.getName().indexOf("$") != -1)
                {
                    //对内部类不作代理，避免cglib出现的奇怪问题
                    Logs.i(String.format("%s 是Service的内部类，智能忽略之...", clazz.getName()));
                    continue;
                }
                Object object = ServiceProxy.proxyService(clazz, jdbc);//根据接口生成动态代理类
                B.s(clazz, object);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 自适应数据源并初始化Service层
     * @author nan.li
     * @param packageName
     */
    public static void packageSearchAndInitService(String packageName)
    {
        Logs.i("begin package search service...");
        List<Class<?>> cList = ClassKit.getPackageAllClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                Logs.i(String.format("[package search] init %s ", clazz.getName()));
                //                com.lnwazg.service.UserService$1 
                if (clazz.getName().indexOf("$") != -1)
                {
                    //对内部类不作代理，避免cglib出现的奇怪问题
                    Logs.i(String.format("%s 是Service的内部类，智能忽略之...", clazz.getName()));
                    continue;
                }
                Object object = ServiceProxy.proxyService(clazz);//根据接口生成动态代理类
                B.s(clazz, object);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 包扫描，并根据实体类初始化对应的数据库表结构
     * @author nan.li
     * @param string
     * @param jdbc
     */
    public static void packageSearchAndInitTables(String packageName, MyJdbc jdbc)
    {
        Logs.i("begin package search entities and init database tables...");
        List<Class<?>> cList = ClassKit.getPackageAllClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                Logs.i(String.format("[package search] init %s ", clazz.getName()));
                //com.lnwazg.entity.User$1 
                if (clazz.getName().indexOf("$") != -1)
                {
                    //对内部类不初始化对应的数据库表
                    Logs.i(String.format("%s 是Entity的内部类，智能忽略之...", clazz.getName()));
                    continue;
                }
                jdbc.createTable(clazz);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 自适应数据源并初始化数据库表结构。通过包扫描，并根据实体类初始化对应的数据库表结构<br>
     * 自适应规则为：<br>
     * 1.根据clazz上的@DataSource注解的内容去决定采用的数据源<br>
     * 2.若注解内容为空或者获取失败，则采用默认数据源
     * @author nan.li
     * @param packageName
     */
    public static void packageSearchAndInitTables(String packageName)
    {
        Logs.i("begin package search entities and init database tables...");
        Logs.i("开始自适应数据源进行表初始化...");
        List<Class<?>> cList = ClassKit.getPackageAllClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                Logs.i(String.format("[package search] init %s ", clazz.getName()));
                //com.lnwazg.entity.User$1 
                if (clazz.getName().indexOf("$") != -1)
                {
                    //对内部类不初始化对应的数据库表
                    Logs.i(String.format("%s 是Entity的内部类，智能忽略之...", clazz.getName()));
                    continue;
                }
                
                String dsName = "";
                if (clazz.isAnnotationPresent(com.lnwazg.dbkit.anno.dao.DataSource.class))
                {
                    com.lnwazg.dbkit.anno.dao.DataSource dataSource = clazz.getAnnotation(com.lnwazg.dbkit.anno.dao.DataSource.class);
                    dsName = dataSource.value();
                }
                MyJdbc jdbc = DsManager.getDataSourceByName(dsName);
                if (StringUtils.isNotEmpty(dsName))
                {
                    Logs.i("使用数据源:" + dsName + "初始化表：" + clazz.getSimpleName());
                }
                else
                {
                    Logs.i("使用默认数据源初始化表：" + clazz.getSimpleName());
                }
                jdbc.createTable(clazz);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 包扫描，根据 AlterTable注解修改表结构<br>
     * 指定专用的数据源
     * @author nan.li
     * @param string
     * @param jdbc
     */
    public static void packageSearchAndModifyTables(String packageName, MyJdbc jdbc)
    {
        Logs.i("begin package search entities and MODIFY database tables...");
        List<Class<?>> cList = ClassKit.getPackageAllClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                Logs.i(String.format("[package search] MODIFY %s ", clazz.getName()));
                //com.lnwazg.entity.User$1 
                if (clazz.getName().indexOf("$") != -1)
                {
                    //对内部类不初始化对应的数据库表
                    Logs.i(String.format("%s 是Entity的内部类，智能忽略之...", clazz.getName()));
                    continue;
                }
                jdbc.alterTable(clazz);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
