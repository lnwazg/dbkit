package com.lnwazg.dbkit.tools.db.collection;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.db.collection.entity.DbArrayListTable;
import com.lnwazg.dbkit.tools.db.collection.entity.DbCollectionTable;
import com.lnwazg.dbkit.utils.TableUtils;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * 用数据库模拟ArrayList
 * @author nan.li
 * @version 2017年8月5日
 */
public class DbArrayList<E> extends CollectionBase implements List<E>
{
    private Class<? extends DbCollectionTable> tableClazz;
    
    private Class<E> elementClazz;
    
    /**
     * 构造函数 
     * @param myJdbc      jdbc对象，便于自动持久化表数据
     * @param tableClazz  表对象类，便于自动持久化表数据
     * @param keyClazz    传入Clazz，主要是为了便于JSON反序列化
     * @param valueClazz  传入Clazz，主要是为了便于JSON反序列化
     */
    @SuppressWarnings("unchecked")
    public DbArrayList(MyJdbc myJdbc, Class<? extends DbCollectionTable> tableClazz, Class<?> elementClazz)
    {
        this.myJdbc = myJdbc;
        if (tableClazz == null)
        {
            tableClazz = DbArrayListTable.class;
            Logs.w("未显式指定数据库映射Bean类tableClazz，将使用默认的tableClazz：" + tableClazz);
        }
        this.tableClazz = tableClazz;
        this.elementClazz = (Class<E>)elementClazz;
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
    
    public DbArrayList(MyJdbc myJdbc, Class<?> elementClazz)
    {
        this(myJdbc, null, elementClazz);
    }
    
    @Override
    public boolean contains(Object o)
    {
        try
        {
            return myJdbc.findOneNoSql(tableClazz, "strValue", parseObject2StoreString(o)) != null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }
    
    @Override
    public Iterator<E> iterator()
    {
        return new Itr();
    }
    
    /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<E>
    {
        int cursor = 0;       // index of next element to return
        
        public boolean hasNext()
        {
            return cursor != size();
        }
        
        public E next()
        {
            return get(cursor++);
        }
    }
    
    @Override
    public Object[] toArray()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <T> T[] toArray(T[] a)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public synchronized boolean add(E e)
    {
        try
        {
            //该键值对不存在，则直接插入即可
            DbCollectionTable dbArrayListTable = ClassKit.newInstance(tableClazz);
            dbArrayListTable.setStrValue(parseObject2StoreString(e));
            myJdbc.insert(dbArrayListTable);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return true;
    }
    
    @Override
    public synchronized void add(int index, E element)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public synchronized boolean remove(Object o)
    {
        try
        {
            myJdbc.execute(String.format("delete from %s where strValue='%s'", tableName, parseObject2StoreString(o)));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return true;
    }
    
    @Override
    public synchronized E remove(int index)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public synchronized boolean removeAll(Collection<?> c)
    {
        if (c != null && c.size() > 0)
        {
            for (Object e : c)
            {
                remove(e);
            }
        }
        return true;
    }
    
    @Override
    public synchronized boolean addAll(Collection<? extends E> c)
    {
        if (c != null && c.size() > 0)
        {
            for (E e : c)
            {
                add(e);
            }
        }
        return true;
    }
    
    @Override
    public synchronized boolean addAll(int index, Collection<? extends E> c)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public synchronized boolean retainAll(Collection<?> c)
    {
        if (c != null && c.size() > 0)
        {
            for (Object e : c)
            {
                //如果原有集合中包含这个元素e，那么就保留之，什么都不做；否则，不包含这个元素，那么就得将其移除掉
                if (!contains(e))
                {
                    remove(e);
                }
            }
        }
        return true;
    }
    
    @Override
    public E get(int index)
    {
        try
        {
            List<? extends DbCollectionTable> list = myJdbc.listAllPaging(tableClazz, index, 1);
            if (Lists.isNotEmpty(list))
            {
                return parseStoreString2Object(list.get(0).getStrValue(), elementClazz);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public synchronized E set(int index, E element)
    {
        try
        {
            List<? extends DbCollectionTable> list = myJdbc.listAllPaging(tableClazz, index, 1);
            if (Lists.isNotEmpty(list))
            {
                DbCollectionTable dbCollectionTable = list.get(0);
                E oldElement = parseStoreString2Object(dbCollectionTable.getStrValue(), elementClazz);
                dbCollectionTable.setStrValue(parseObject2StoreString(element));
                myJdbc.updateEntity(dbCollectionTable);
                return oldElement;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public int indexOf(Object o)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int lastIndexOf(Object o)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ListIterator<E> listIterator()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ListIterator<E> listIterator(int index)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<E> subList(int fromIndex, int toIndex)
    {
        throw new UnsupportedOperationException();
    }
}
