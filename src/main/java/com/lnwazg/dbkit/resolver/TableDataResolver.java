package com.lnwazg.dbkit.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.lnwazg.dbkit.pagination.PaginationProcessor;

/**
 * 表数据的解析器
 * @author nan.li
 * @version 2017年5月7日
 */
public interface TableDataResolver
{
    /**
     * 获取一个分页处理器对象
     * @author nan.li
     * @param conn
     * @return
     */
    PaginationProcessor getPaginationProcessor(Connection conn);
    
    /**
     * PreparedStatement为某个index的数据设值
     * @author nan.li
     * @param ps
     * @param index
     * @param param
     * @throws SQLException
     */
    void setParam(PreparedStatement ps, int index, Object param)
        throws SQLException;
        
    /**
     * 读取并解析ResultSet某个index数据的值
     * @author nan.li
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    Object readValue(ResultSet rs, int index)
        throws SQLException;
}
