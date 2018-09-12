package com.lnwazg.dbkit.vo;

import java.sql.SQLException;

import com.lnwazg.dbkit.jdbc.impl.BasicJdbcSupport;

/**
 * 封装了BasicJdbcSupport的Runnable对象
 * @author nan.li
 * @version 2017年3月9日
 */
public abstract class BasicJdbcSupportConnectionObject<T> extends BasicJdbcSupport
{
    /**
     * 核心run方法，会有一个明确的执行结果
     * @return 执行结果
     * @throws SQLException sql exception caused
     */
    public abstract T run()
        throws SQLException;
}
