package com.lnwazg.dbkit.tools.sqlmonitor;

import java.io.IOException;

import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.httpkit.Constants;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * 扩展大全
 * @author nan.li
 * @version 2018年5月15日
 */
public class SQLMonitor
{
    /**
     * SQL耗时信息表<br>
     * SQL监视器仅在开发期间使用，便于迅速定位badsql，因此不对容量做限制<br>
     * 在上线之后关闭SQLMonitor，以节约内存开销
     */
    public static RecordMap<String, Long> sqlCostMap = new RecordMap<>(200, 1000000, 15);
    
    /**
     * SQL监视器
     * @author nan.li
     */
    public static void initSqlMonitor()
    {
        if (DbKit.SQL_MONITOR)
        {
            //监控sql的执行情况
            //若引用了httpkit，则启用一个http服务器
            boolean hasHttpKitClass = ClassKit.checkClassPathExists("com.lnwazg.httpkit.server.HttpServer");
            if (hasHttpKitClass)
            {
                Logs.i("SQL_MONITOR已启用...");
                Logs.i("启动内置http服务器进行sql监控...");
                //启动http服务器，监控sql的执行情况
                int port = 5000;
                HttpServer server;
                try
                {
                    server = HttpServer.bind(port);
                    //server之所以没做成单例模式的，是因为我要支持多实例的server对象在一个项目中共存
                    server.setControllerSuffix("do");
                    server.setMyZooGroupName("dbkit");
                    server.setContextPath("dbkit");
                    server.addFreemarkerPageDirRoute("web", Constants.DEFAULT_WEB_RESOURCE_BASE_PATH);
                    server.packageSearchAndInitUseDefaultFilterConfigs("com.lnwazg.dbkit.tools.sqlmonitor.controller");
                    Logs.i("SQL实时监控模式地址请访问  http://127.0.0.1:" + port + server.getBasePath() + "/web/page/monitor.ftl");
                    //监听在这个端口处
                    server.listen();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Logs.i("编译路径中未发现httpKit，忽略启动http服务器！");
            }
        }
    }
    
    /**
     * 记录下SQL的耗时情况
     * @author nan.li
     * @param sql
     * @param cost
     */
    public static void record(String sql, long cost)
    {
        sqlCostMap.put(sql, cost);
    }
}
