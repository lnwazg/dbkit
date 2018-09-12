package com.lnwazg.dbkit.tools.sqlmonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BadSql monitor 记录表
 * @author nan.li
 * @version 2018年5月15日
 */
public class RecordMap<K, V> extends ConcurrentHashMap<K, V>
{
    private static final long serialVersionUID = -2645474937902010224L;
    
    /**
     * map最大展示的数据量
     */
    int maxShowSize = 300;
    
    /**
     * map最大容量（防止爆内存）
     */
    int maxSize = 10000;
    
    /**
     * 最大badSql数量
     */
    int badSqlMaxSize = 5;
    
    /**
     * 键列表
     */
    private List<K> keyList = new LinkedList<>();
    
    public RecordMap()
    {
    }
    
    public RecordMap(int maxShowSize, int maxSize, int badSqlMaxSize)
    {
        this.maxShowSize = maxShowSize;
        this.maxSize = maxSize;
        this.badSqlMaxSize = badSqlMaxSize;
    }
    
    @Override
    public V put(K key, V value)
    {
        V v = super.put(key, value);
        if (!keyList.contains(key))
        {
            keyList.add(key);
        }
        if (size() > maxSize)
        {
            //一旦超过最大值，即删掉
            K k = keyList.get(0);
            remove(k);
            keyList.remove(0);
        }
        //        Logs.i("size=" + size());
        return v;
    }
    
    /**
     * 获取待展示的表（展示最近的maxShowSize条记录）
     */
    public LinkedHashMap<K, V> getShowMap()
    {
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        int fetchSize = size();
        if (fetchSize > maxShowSize)
        {
            fetchSize = maxShowSize;
        }
        for (int i = 0; i < fetchSize; i++)
        {
            int index = keyList.size() - 1 - i;
            result.put(keyList.get(index), get(keyList.get(index)));
        }
        return result;
    }
    
    /**
     * 获取badSql表
     */
    public LinkedHashMap<K, V> getBadSqls()
    {
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        List<Map.Entry<K, V>> entryList = new ArrayList<Map.Entry<K, V>>(
            entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(java.util.Map.Entry<K, V> o1, java.util.Map.Entry<K, V> o2)
            {
                V v1 = o1.getValue();
                V v2 = o2.getValue();
                if (v1 instanceof Long)
                {
                    return ((Long)v2).compareTo((Long)v1);
                }
                else if (v1 instanceof Integer)
                {
                    return ((Integer)v2).compareTo((Integer)v1);
                }
                return -1;
            }
        });
        int fetchBadSqlSize = size();
        if (fetchBadSqlSize > badSqlMaxSize)
        {
            fetchBadSqlSize = badSqlMaxSize;
        }
        for (int i = 0; i < fetchBadSqlSize; i++)
        {
            result.put(entryList.get(i).getKey(), entryList.get(i).getValue());
        }
        return result;
    }
    
    public static void main(String[] args)
    {
        RecordMap<String, Integer> recordMap = new RecordMap<>(5, 10, 5);
        for (int i = 0; i < 205; i++)
        {
            recordMap.put("key" + i + "", i);
        }
        System.out.println(recordMap.getBadSqls());
        System.out.println(recordMap.getShowMap());
    }
    
    public List<K> getKeyList()
    {
        return keyList;
    }
}
