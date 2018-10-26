package com.lnwazg.dbkit.tools.db.collection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.db.collection.entity.DbHashMapTable;
import com.lnwazg.dbkit.utils.TableUtils;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * 将数据库作为HashMap的实现，支持Map接口的所有操作，但是数据是存放在数据库中，程序重启后数据也不会丢失，并且还便于检索<br>
 * 像使用HashMap一样操作数据库！
 * @author nan.li
 * @version 2017年8月4日
 */
public class DbHashMap<K, V> extends CollectionBase implements Map<K, V>
{
    protected Class<? extends DbHashMapTable> tableClazz;
    
    private Class<K> keyClazz;
    
    private Class<V> valueClazz;
    
    public DbHashMap(MyJdbc myJdbc, Class<K> keyClazz, Class<V> valueClazz)
    {
        this(myJdbc, null, keyClazz, valueClazz);
    }
    
    /**
     * 构造函数 
     * @param myJdbc      jdbc对象，便于自动持久化表数据
     * @param tableClazz  表对象类，便于自动持久化表数据
     * @param keyClazz    传入Clazz，主要是为了便于JSON反序列化
     * @param valueClazz  传入Clazz，主要是为了便于JSON反序列化
     */
    public DbHashMap(MyJdbc myJdbc, Class<? extends DbHashMapTable> tableClazz, Class<K> keyClazz, Class<V> valueClazz)
    {
        this.myJdbc = myJdbc;
        if (tableClazz == null)
        {
            tableClazz = DbHashMapTable.class;
            Logs.w("未显式指定数据库映射Bean类tableClazz，将使用默认的tableClazz：" + tableClazz);
        }
        this.tableClazz = tableClazz;
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
        this.tableName = TableUtils.getTableName(tableClazz);
        try
        {
            //（如果表不存在，则）自动建表
            myJdbc.createTable(tableClazz);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean containsKey(Object key)
    {
        return get(key) != null;
    }
    
    @Override
    public V get(Object key)
    {
        try
        {
            DbHashMapTable dbHashMapTable = myJdbc.findOneNoSql(tableClazz, "strKey", parseObject2StoreString(key));
            if (dbHashMapTable != null)
            {
                String value = dbHashMapTable.getStrValue();
                if (StringUtils.isNotEmpty(value))
                {
                    return parseStoreString2Object(value, valueClazz);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean containsValue(Object value)
    {
        try
        {
            return myJdbc.count("select count(1) from " + TableUtils.getTableName(tableClazz) + " where strValue=?", parseObject2StoreString(value)) > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public synchronized V put(K key, V value)
    {
        V oldValue = get(key);
        try
        {
            DbHashMapTable dbHashMapTable = myJdbc.findOneNoSql(tableClazz, "strKey", parseObject2StoreString(key));
            if (dbHashMapTable == null)
            {
                //该键值对不存在，则直接插入即可
                dbHashMapTable = ClassKit.newInstance(tableClazz);
                dbHashMapTable.setStrKey(parseObject2StoreString(key));
                dbHashMapTable.setStrValue(parseObject2StoreString(value));
                myJdbc.insert(dbHashMapTable);
            }
            else
            {
                //该键值对已经存在，则更新之
                dbHashMapTable.setStrValue(parseObject2StoreString(value));
                myJdbc.updateEntity(dbHashMapTable);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return oldValue;
    }
    
    @Override
    public synchronized V remove(Object key)
    {
        V oldValue = get(key);
        try
        {
            myJdbc.execute(String.format("delete from %s where strKey='%s'", tableName, parseObject2StoreString(key)));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return oldValue;
    }
    
    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> m)
    {
        if (!Maps.isEmpty(m))
        {
            for (K k : m.keySet())
            {
                put(k, m.get(k));
            }
        }
    }
    
    @Override
    public Set<K> keySet()
    {
        Set<java.util.Map.Entry<K, V>> entrySet = entrySet();
        if (entrySet != null)
        {
            Set<K> resultSet = new HashSet<>();
            for (java.util.Map.Entry<K, V> entry : entrySet)
            {
                resultSet.add(entry.getKey());
            }
            return resultSet;
        }
        return null;
    }
    
    @Override
    public Collection<V> values()
    {
        Set<java.util.Map.Entry<K, V>> entrySet = entrySet();
        if (entrySet != null)
        {
            Collection<V> resultCollection = new ArrayList<>();
            for (java.util.Map.Entry<K, V> entry : entrySet)
            {
                resultCollection.add(entry.getValue());
            }
            return resultCollection;
        }
        return null;
    }
    
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet()
    {
        try
        {
            Map<K, V> map = new HashMap<>();
            List<? extends DbHashMapTable> list = myJdbc.listAll(tableClazz);
            if (Lists.isNotEmpty(list))
            {
                for (DbHashMapTable dbHashMapTable : list)
                {
                    map.put(parseStoreString2Object(dbHashMapTable.getStrKey(), keyClazz), parseStoreString2Object(dbHashMapTable.getStrValue(), valueClazz));
                }
                return map.entrySet();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
