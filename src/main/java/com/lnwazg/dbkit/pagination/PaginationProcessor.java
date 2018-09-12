package com.lnwazg.dbkit.pagination;

import java.util.Collection;

import com.lnwazg.dbkit.order.OrderBy;
import com.lnwazg.dbkit.vo.SqlAndArgs;

/**
 * 分页处理器
 * @author nan.li
 * @version 2017年5月7日
 */
public interface PaginationProcessor
{
    /**
     * 分页的行号
     */
    String COLUMN_ROW_NUMBER = "row_number__";
    
    /**
     * 分页处理
     * @param orders query result orders
     * @param offset row offset
     * @param limit row limit
     * @param sql sql
     * @param args sql args
     * @return sql and args object
     */
    SqlAndArgs process(Collection<OrderBy> orders, int offset, int limit, String sql, Collection<? super Object> args);
}
