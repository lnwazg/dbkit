package com.lnwazg.dbkit.utils;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.singleton.B;

/**
 * 数据源管理工具
 * @author nan.li
 * @version 2018年9月21日
 */
public class DsManager
{
    /**
     * 根据数据源名称获取数据连接对象<br>
     * 若数据源名称为空，则返回默认的数据连接对象
     * @author nan.li
     * @param dsName
     * @return
     */
    public static MyJdbc getDataSourceByName(String dsName)
    {
        MyJdbc jdbc = null;
        if (StringUtils.isNotEmpty(dsName))
        {
            Logs.d("使用数据源:" + dsName);
            //获取指定名称的数据源连接对象
            jdbc = B.g(MyJdbc.class, dsName);
        }
        else
        {
            Logs.d("使用默认数据源");
            //获取默认数据源连接对象
            jdbc = B.g(MyJdbc.class);
        }
        return jdbc;
    }
    
}
