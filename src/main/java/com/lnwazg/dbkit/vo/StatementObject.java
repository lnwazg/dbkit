package com.lnwazg.dbkit.vo;

import java.sql.SQLException;
import java.sql.Statement;

import com.lnwazg.dbkit.tools.AllDbExecInfo;

/**
 * Statement辅助类
 * @author nan.li
 * @version 2017年5月7日
 */
public interface StatementObject<T>
{
    /**
     * 构建并返回一个详细的对象
     * @author nan.li
     * @return
     * @throws SQLException
     */
    AllDbExecInfo build()
        throws SQLException;
        
    /**
     * 执行Statement对象，并返回执行结果
     * @author nan.li
     * @param statement
     * @return
     * @throws SQLException
     */
    T exec(Statement statement)
        throws SQLException;
}
