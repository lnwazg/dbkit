package com.lnwazg.dbkit.pagination.impl;

import java.util.Collection;

import com.lnwazg.dbkit.order.OrderBy;
import com.lnwazg.dbkit.pagination.PaginationProcessor;
import com.lnwazg.dbkit.utils.StringUtils;
import com.lnwazg.dbkit.vo.SqlAndArgs;

/**
 * 与Oracle兼容的分页处理器<br>
 * 适用于jdbc:oracle  jdbc:alibaba:oracle  jdbc:db2  jdbc:sqlserver这几种数据库
 * @author nan.li
 * @version 2017年5月7日
 */
public class OracleCompatibleProcessor implements PaginationProcessor
{
    /**
     * 分页处理
     	<pre>
    	SELECT * FROM (
    		SELECT ROW_NUMBER() OVER (ORDER BY OrderDate) RowNum, *
    		FROM  Orders
    		WHERE OrderDate &gt;= '1980-01-01') RowConstrainedResult
    	WHERE RowNum &gt;= 1
    	    AND RowNum &lt; 20
    	ORDER BY RowNum
    	</pre>
     */
    public SqlAndArgs process(Collection<OrderBy> orders, int start, int limit, String sql, Collection<? super Object> args)
    {
        //        Logs.info("Process Oracle Compatible Pagination Sql ["+sql+"].");
        int indexFrom = StringUtils.indexOfIgnoreCase(sql, " from ");
        StringBuilder sbSql = new StringBuilder("select * from (").append(sql.substring(0, indexFrom))
            .append(", row_number() over (order by ")
            .append(StringUtils.join(orders, ", "))
            .append(") ")
            .append(COLUMN_ROW_NUMBER)
            .append(sql.substring(indexFrom))
            .append(") where ")
            .append(COLUMN_ROW_NUMBER)
            .append(" >= ? ");
        args.add(start);
        if (limit > 0)
        {
            sbSql.append("and ").append(COLUMN_ROW_NUMBER).append(" < ?");
            args.add(start + limit);
        }
        return new SqlAndArgs(sbSql.toString(), args);
    }
}
