package com.lnwazg.dbkit.resolver;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集对象解析器
 * @author nan.li
 * @version 2017年5月7日
 */
public interface ResultSetResolve<T>
{
    /**
     * 对结果集进行解析，并返回处理结果对象
     * @author nan.li
     * @param rs
     * @return
     * @throws SQLException
     */
    T exec(ResultSet rs)
        throws SQLException;
}
