package com.lnwazg.dbkit.resolver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.lnwazg.dbkit.pagination.PaginationProcessor;
import com.lnwazg.dbkit.pagination.impl.MySQLCompatiblePaginationProcessor;
import com.lnwazg.dbkit.pagination.impl.OracleCompatibleProcessor;
import com.lnwazg.dbkit.resolver.TableDataResolver;
import com.lnwazg.kit.date.DateUtils;

public class TableDataResolverImpl implements TableDataResolver
{
    /**
     * 与Mysql兼容的分页处理器
     */
    private PaginationProcessor mySQLCompatiblePaginationProcessor = new MySQLCompatiblePaginationProcessor();
    
    /**
     * 与Oracle兼容的分页处理器
     */
    private PaginationProcessor oracleCompatibleProcessor = new OracleCompatibleProcessor();
    
    public PaginationProcessor getPaginationProcessor(Connection conn)
    {
        String jdbcUrl = null;
        try
        {
            jdbcUrl = conn.getMetaData().getURL();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Get Connection URL Error Caused", e);
        }
        //根据jdbc的连接信息，来选取对应的分页处理器
        if (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:") || jdbcUrl.startsWith("jdbc:postgresql:")
            || jdbcUrl.startsWith("jdbc:sqlite:") || jdbcUrl.startsWith("jdbc:cobar:") || jdbcUrl.startsWith("jdbc:h2:") || jdbcUrl.startsWith("jdbc:hsqldb:"))
        {
            return mySQLCompatiblePaginationProcessor;
        }
        else if (jdbcUrl.startsWith("jdbc:oracle:") || jdbcUrl.startsWith("jdbc:alibaba:oracle:") || jdbcUrl.startsWith("jdbc:db2:")
            || jdbcUrl.startsWith("jdbc:sqlserver:"))
        {
            return oracleCompatibleProcessor;
        }
        else
        {
            throw new IllegalStateException("Unsupported Database [" + jdbcUrl + "] Pagination, Please Set Pagination Provider");
        }
    }
    
    public void setParam(PreparedStatement ps, int index, Object param)
        throws SQLException
    {
        if (param == null)
        {
            //如果参数值为空，那么就调用setNull()方法，当然调用之前需要知道参数的类型
            int colType = Types.VARCHAR;
            try
            {
                colType = ps.getParameterMetaData().getParameterType(index);
            }
            catch (SQLException e)
            {
                // ignore
            }
            ps.setNull(index, colType);
        }
        else
        {
            //参数值不为空，那么将参数对象传入
            setParamByType(ps, index, param);
        }
    }
    
    /**
     * 根据参数类型，设置参数的值<br>
     * 这里是设置参数的时候的转换，与之相对应的，是获取结果集时候的转换 
     * @see com.lnwazg.dbkit.resolver.impl.ResultSetToObjectMapper
     * @author nan.li
     * @param ps
     * @param index
     * @param param
     * @throws SQLException
     */
    private void setParamByType(PreparedStatement ps, int index, Object param)
        throws SQLException
    {
        if (param instanceof Date)
        {
            //对日期类型做特殊处理  add by linan 2016-7-31
            //直接将日期数据格式化为字符串存入数据库：yyyy-MM-dd HH:mm:ss 
            //解析的时候也会按照这个格式反向解析为Date类型的对象
            //反向解析的代码如下：
            //            if (field.getType() == Date.class)
            //            {
            //                //作为字符串进行转换
            //                String value = rs.getString(fieldName);
            //                if (StringUtils.isNoneBlank(value))
            //                {
            //                    field.set(t, DateUtils.parseDate(value, DateUtils.DEFAULT_DATE_TIME_FORMAT_PATTERN));
            //                }
            //            }
            ps.setObject(index, DateUtils.getFormattedDateTimeStr(DateUtils.DEFAULT_DATE_TIME_FORMAT_PATTERN, (Date)param));
        }
        else
        {
            //其他类型的话，该传什么就传什么。
            ps.setObject(index, param);
        }
        //当然这边可以继续支持更多类似于Date数据类型的扩展。修改的时候需要同步修改com.lnwazg.dbkit.resolver.impl.ResultSetObjectFieldNameMapper类
    }
    
    public Object readValue(ResultSet rs, int index)
        throws SQLException
    {
        return rs.getObject(index);
    }
}
