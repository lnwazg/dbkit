package com.lnwazg.dbkit.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.singleton.BeanMgr;

/**
 * 数据库负责批量插入数据的后台守护线程<br>
 * 只有插入数据需要批量提交以提高性能，修改和删除操作其sql本身就可以支持批量操作（加where条件进行过滤即可），查询功能原生就是批量的！
 * @author lnwazg@126.com
 * @version 2016年10月9日
 */
public class BatchInsertDBDaemonThread
{
    /**
     * 待批量操作的对象
     */
    public static List<Object> vector = Collections.synchronizedList(new LinkedList<>());//写性能较好  读性能（采用了synchronized关键字的方式）较差
    
    static
    {
        //后台进程自动批量提交数据到数据库，大大减轻数据库的压力
        //延迟批量提交，是提高数据库插入性能的神器
        ExecMgr.startDaemenThread(() -> {
            while (true)
            {
                int size = vector.size();
                if (size > 0)
                {
                    MyJdbc myJdbc = BeanMgr.get(MyJdbc.class);
                    if (myJdbc == null)
                    {
                        Logs.w("警告：MyJdbc尚未注入到BeanMgr，因此无法执行批量插入操作！请保证MyJdbc事先被有效地注入！");
                        return;
                    }
                    try
                    {
                        List<Object> thisTimeRequests = new LinkedList<>();
                        //批量提交所有
                        for (int i = 0; i < size; i++)
                        {
                            thisTimeRequests.add(vector.remove(0));
                        }
                        
                        //对thisTimeRequests列表里面的对象请求进行分组
                        Map<Class<?>, List<Object>> classTypeObjectList = new HashMap<>();
                        for (Object object : thisTimeRequests)
                        {
                            Class<?> clazz = object.getClass();//取出这个请求所属的类
                            List<Object> list = classTypeObjectList.get(clazz);
                            if (list == null)
                            {
                                //该类对应的对象列表先初始化一下
                                list = new LinkedList<>();
                                classTypeObjectList.put(clazz, list);
                            }
                            list.add(object);
                            classTypeObjectList.put(clazz, list);
                        }
                        
                        //即将批量提交请求
                        ExecMgr.singleExec.execute(() -> {
                            try
                            {
                                for (Map.Entry<Class<?>, List<Object>> entry : classTypeObjectList.entrySet())
                                {
                                    String className = entry.getKey().getSimpleName();//当前要批量插入的类名称
                                    List<Object> eachTypeList = entry.getValue();//当前要批量插入的对象列表
                                    if (eachTypeList.size() == 1)
                                    {
                                        Logs.i(String.format("数据库开始插入单个对象: %s", className));
                                        myJdbc.insert(eachTypeList.get(0));
                                    }
                                    else
                                    {
                                        //批量提交
                                        Logs.i(String.format("数据库开始批量插入对象: %s 个  %s", eachTypeList.size(), className));
                                        myJdbc.insertBatch(eachTypeList);
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                
                //间歇500毫秒后再试，500毫秒是一个很理想的批量间隔时间
                try
                {
                    Thread.sleep(500);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
