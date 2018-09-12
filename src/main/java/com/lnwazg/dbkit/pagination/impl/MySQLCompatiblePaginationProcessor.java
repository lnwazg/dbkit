package com.lnwazg.dbkit.pagination.impl;

import java.util.Collection;

import com.lnwazg.dbkit.order.OrderBy;
import com.lnwazg.dbkit.pagination.PaginationProcessor;
import com.lnwazg.dbkit.utils.StringUtils;
import com.lnwazg.dbkit.vo.SqlAndArgs;

/**
 * 与Mysql兼容的分页处理器<br>
 * 适用于jdbc:mysql   jdbc:mariadb   jdbc:postgresql   jdbc:sqlite   jdbc:cobar   jdbc:h2   jdbc:hsqldb这几种数据库
 * @author nan.li
 * @version 2017年5月7日
 */
public class MySQLCompatiblePaginationProcessor implements PaginationProcessor
{
    public SqlAndArgs process(Collection<OrderBy> orders, int start, int limit, String sql, Collection<? super Object> args)
    {
        sql += " order by " + StringUtils.join(orders, ", ") + " limit ?";
        args.add(start);
        if (limit > 0)
        {
            sql += ", ?";
            args.add(limit);
        }
        return new SqlAndArgs(sql, args);
    }
}
