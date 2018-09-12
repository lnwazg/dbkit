package com.lnwazg.dbkit.tools.bi;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.bi.bean.BiDataSource;
import com.lnwazg.dbkit.tools.bi.bean.BiMetaInfo;
import com.lnwazg.dbkit.tools.bi.bean.BiView;
import com.lnwazg.dbkit.tools.db2javabean.DbTableStructureKit;
import com.lnwazg.dbkit.tools.graphtool.bean.GTable;
import com.lnwazg.dbkit.tools.graphtool.vo.ColumnData;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.excel.ExcelKit;
import com.lnwazg.kit.file.FileKit;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;

/**
 * Bi分析引擎 <br>
 * 一个强大的工具，可用于数据抽取、汇总、分析！超强的透视功能！<br>
 * 能否支持在json中写等于号等特殊符号？经过验证完全可以！因此该BI分析工具的能力又得到了巨大的提升！<br>
 * 做成独立运行的jar包，显示运行日志
 * @author nan.li
 * @version 2017年12月20日
 */
public class BiEngine
{
    /**
     * 解析这个对象，自上而下去解析执行
     * @author nan.li
     * @param biMetaInfo
     */
    public static void process(BiMetaInfo biMetaInfo)
    {
        Logs.i("begin to process biMetaInfo...");
        if (biMetaInfo == null)
        {
            Logs.w("biMetaInfo is null! exit!");
            return;
        }
        //1.解析数据源
        List<BiDataSource> biDataSources = biMetaInfo.getDatasources();
        if (Lists.isEmpty(biDataSources))
        {
            Logs.w("biDataSources is empty! exit!");
            return;
        }
        Map<String, BiDataSource> biDataSourceMap = new LinkedHashMap<>();
        for (BiDataSource biDataSource : biDataSources)
        {
            String url = biDataSource.getUrl();
            if (StringUtils.isEmpty(url))
            {
                Logs.w("invalid  empty datasource url, dsName is " + biDataSource.getDsName());
                return;
            }
            else
            {
                if (url.startsWith("jdbc:"))
                {
                    //如果是mysql或者sqlite这样的数据源，那么直接加载数据源即可
                    biDataSource.setJdbc(DbKit.getJdbc(biDataSource.getUrl(), biDataSource.getUsername(), biDataSource.getPassword()));
                }
                else
                {
                    //数据源可能是一个文件
                    //交由具体的去处理
                }
            }
            //置入表中
            biDataSourceMap.put(biDataSource.getDsName(), biDataSource);
        }

        //2.选择结果数据源
        //验证目标数据源是否存在
        String resultDsRef = biMetaInfo.getResultDsRef();
        if (StringUtils.isEmpty(resultDsRef))
        {
            Logs.w("resultDsRef is empty! exit!");
            return;
        }
        if (!biDataSourceMap.containsKey(resultDsRef))
        {
            Logs.w("resultDsRef not found in datasources definition!");
            return;
        }

        MyJdbc targetJdbc = biDataSourceMap.get(resultDsRef).getJdbc();
        if (targetJdbc == null)
        {
            Logs.w("targetJdbc is empty!");
            return;
        }

        //3.依次解析并计算出结果
        List<BiView> biViews = biMetaInfo.getViews();
        for (BiView biView : biViews)
        {
            String viewName = biView.getViewName();
            String refDsName = biView.getRefDsName();
            String sql = biView.getSql();

            if (StringUtils.isEmpty(refDsName))
            {
                //如果没有显式指定ds名称，那么就是结果的ds名称
                refDsName = resultDsRef;
            }
            MyJdbc thisJdbc = biDataSourceMap.get(refDsName).getJdbc();

            //视图都需要落到目标表中
            if (StringUtils.isNotEmpty(sql))
            {
                Logs.i("开始生成视图: viewName=" + viewName + " ,refDsName=" + refDsName + " ,sql=" + sql);
                try
                {
                    //查询出结果集
                    List<Map<String, Object>> listmap = thisJdbc.listMap(sql);
                    //结果集中已经自带了视图列的信息

                    //                    //视图列
                    //                    List<String> viewColumns = new ArrayList<>();
                    //                    String dbType = JdbcConstants.MYSQL;
                    //                    List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
                    //                    if (Lists.isEmpty(stmtList) || stmtList.size() != 1)
                    //                    {
                    //                        Logs.w("invalid view sql: " + sql);
                    //                    }
                    //                    for (int i = 0; i < stmtList.size(); i++)
                    //                    {
                    //                        SQLStatement stmt = stmtList.get(i);
                    //                        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
                    //                        stmt.accept(visitor);
                    //                        for (Column column : visitor.getColumns())
                    //                        {
                    //                            //列名称加入进来
                    //                            viewColumns.add(column.getName());
                    //                        }
                    //                        //columns [card.*, card.aaa, card.bbb, card.ccc, card.eee, card.ddd]
                    //                        //conditions [card.aaa = 1, card.bbb = 2, card.ccc = 3]
                    //                        //groupByColumns [card.eee]
                    //                        //orderByColumns [card.ddd]
                    //
                    //                        //排除掉该排除的列，就是参数列
                    //                        for (Condition condition : visitor.getConditions())
                    //                        {
                    //                            viewColumns.remove(condition.getColumn().getName());
                    //                        }
                    //                        for (Column column : visitor.getGroupByColumns())
                    //                        {
                    //                            viewColumns.remove(column.getName());
                    //                        }
                    //                        for (Column column : visitor.getOrderByColumns())
                    //                        {
                    //                            viewColumns.remove(column.getName());
                    //                        }
                    //                    }
                    //                    if (Lists.isEmpty(viewColumns))
                    //                    {
                    //                        viewColumns.add("result");
                    //                    }
                    //                    Logs.i("视图列:" + viewColumns);

                    //词法分析还是不够靠谱先进。而直接从结果集中拿到字段列名，还是靠谱给力的！

                    //刷新数据到目标表中
                    targetJdbc.createTableAndRefreshColumnData(viewName, listmap);
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                //无sql，那么此处都是高阶操作
                String action = biView.getAction();
                switch (action)
                {
                    case "txtRead":
                        Logs.i("开始生成视图: viewName=" + viewName + " ,refDsName=" + refDsName + " ,action=" + action);
                        //读取txt表格内容，并入库
                        String columnSplitter = biView.getColumnSplitter();
                        List<String> viewColumns = biView.getColumns();
                        String fileUrl = biDataSourceMap.get(refDsName).getUrl();
                        Logs.i("begin to read txt, fileUrl=" + fileUrl + " columnSplitter=" + columnSplitter + " columns=" + viewColumns);
                        List<Map<String, Object>> listmap = FileKit.readListMapFromTxt(fileUrl, viewColumns, columnSplitter);
                        System.out.println("listmap=" + listmap);
                        //刷新数据到目标表中
                        try
                        {
                            targetJdbc.createTableAndRefreshColumnData(viewName, viewColumns, listmap);
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                        break;
                    case "excelRead":
                        Logs.i("开始生成视图: viewName=" + viewName + " ,refDsName=" + refDsName + " ,action=" + action);
                        //读取excel表格内容，并入库
                        viewColumns = biView.getColumns();
                        fileUrl = biDataSourceMap.get(refDsName).getUrl();
                        Logs.i("begin to read excel, fileUrl=" + fileUrl + " columns=" + viewColumns);
                        listmap = ExcelKit.readListMapFromExcel(fileUrl, viewColumns);
                        System.out.println("listmap=" + listmap);
                        //刷新数据到目标表中
                        try
                        {
                            targetJdbc.createTableAndRefreshColumnData(viewName, viewColumns, listmap);
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                        break;
                    case "tableCopy":
                        //表复制功能
                        String tables = biView.getTables();
                        Logs.i("开始表复制: tables=" + tables + " ,refDsName=" + refDsName + " ,action=" + action);
                        if (StringUtils.isEmpty(tables))
                        {
                            //表为空
                            Logs.w("tables定义为空，无法执行表复制任务！");
                            continue;
                        }
                        String[] tableArray = tables.trim().split(",");
                        for (String tableName : tableArray)
                        {
                            copyTableFromMysqlDb(thisJdbc, targetJdbc, tableName);
                        }
                        break;
                    case "tableCopyAll":
                        //复制所有的表
                        Logs.i("开始复制所有表: refDsName=" + refDsName + " ,action=" + action);
                        try
                        {
                            List<GTable> tableList = DbTableStructureKit.getTableList(thisJdbc);
                            for (GTable gTable : tableList)
                            {
                                copyTableFromMysqlDb(thisJdbc, targetJdbc, gTable.getTableName());
                            }
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        Logs.w("无法识别的action: " + action);
                        break;
                }
            }
        }
        Logs.i("Process OK!");
    }

    /**
     * 表结构与数据的完全拷贝，仅支持mysql<br>
     * 拷贝的目的地列的数据类型都是String
     * @author nan.li
     * @param thisJdbc
     * @param targetJdbc
     * @param tableName
     */
    private static void copyTableFromMysqlDb(MyJdbc thisJdbc, MyJdbc targetJdbc, String tableName)
    {
        Logs.i("开始复制表:" + tableName);
        try
        {
            //列出该表的字段列表
            List<String> viewColumns = new ArrayList<>();
            List<ColumnData> columnDatas = thisJdbc.list(ColumnData.class,
                String.format(
                    "SELECT table_name tableName,column_name columnName, CONCAT(IFNULL(data_type,''),'(',IFNULL(CHARACTER_MAXIMUM_LENGTH,''),')') dataType,column_comment columnComment "
                        + "FROM information_schema.columns "
                        + "WHERE table_schema ='%s' "
                        + "and table_name='%s'",
                    thisJdbc.getSchemaName(),
                    tableName));
            for (ColumnData columnData : columnDatas)
            {
                viewColumns.add(columnData.getColumnName());
            }
            //查询该表的所有数据
            List<Map<String, Object>> listmap = thisJdbc.listMap(String.format("select * from %s", tableName));

            //插入到目标表中
            targetJdbc.createTableAndRefreshColumnData(tableName, viewColumns, listmap);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
