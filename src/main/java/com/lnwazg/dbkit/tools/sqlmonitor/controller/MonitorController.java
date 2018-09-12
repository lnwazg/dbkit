package com.lnwazg.dbkit.tools.sqlmonitor.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.lnwazg.dbkit.tools.sqlmonitor.SQLMonitor;
import com.lnwazg.dbkit.tools.sqlmonitor.entity.LayerUITableDataObj;
import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.kit.controllerpattern.Controller;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.map.Maps;

@Controller("/monitor")
public class MonitorController extends BaseController
{
    void monitorJson()
    {
        okJson(Maps.asMap("allSqls", SQLMonitor.sqlCostMap, "badSqls", SQLMonitor.sqlCostMap.getBadSqls()));
    }
    
    void queryAllSqlData()
    {
        List<MonitorTableColumn> data = new ArrayList<>();
        int page = getParamAsInt("page");
        int limit = getParamAsInt("limit");
        int startIndex = (page - 1) * limit;
        int endIndex = page * limit;
        List<String> keyList = SQLMonitor.sqlCostMap.getKeyList();
        if (Lists.isEmpty(keyList))
        {
            okJson(null);
            return;
        }
        if (startIndex >= 0 && startIndex < keyList.size())
        {
        }
        else
        {
            startIndex = 0;
        }
        if (endIndex > keyList.size())
        {
            endIndex = keyList.size();
        }
        for (int i = startIndex; i < endIndex; i++)
        {
            String key = keyList.get(keyList.size() - 1 - i);
            data.add(new MonitorTableColumn().setCost(SQLMonitor.sqlCostMap.get(key)).setSql(key));
        }
        LayerUITableDataObj layerUITableDataObj = new LayerUITableDataObj().success().setData(data).setCount(keyList.size());
        okJson(layerUITableDataObj);
    }
    
    void queryBadSqlData()
    {
        List<MonitorTableColumn> data = new ArrayList<>();
        LinkedHashMap<String, Long> badSqlMap = SQLMonitor.sqlCostMap.getBadSqls();
        if (Maps.isEmpty(badSqlMap))
        {
            okJson(null);
            return;
        }
        for (String key : badSqlMap.keySet())
        {
            data.add(new MonitorTableColumn().setCost(badSqlMap.get(key)).setSql(key));
        }
        LayerUITableDataObj layerUITableDataObj = new LayerUITableDataObj().success().setData(data).setCount(badSqlMap.size());
        okJson(layerUITableDataObj);
    }
    
    /**
     * 监控SQL的表
     * @author nan.li
     * @version 2018年5月19日
     */
    static class MonitorTableColumn
    {
        String sql;
        
        Long cost;
        
        public String getSql()
        {
            return sql;
        }
        
        public MonitorTableColumn setSql(String sql)
        {
            this.sql = sql;
            return this;
        }
        
        public Long getCost()
        {
            return cost;
        }
        
        public MonitorTableColumn setCost(Long cost)
        {
            this.cost = cost;
            return this;
        }
        
    }
}
