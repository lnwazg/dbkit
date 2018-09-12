package com.lnwazg.dbkit.proxy;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.anno.service.Transactional;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.kit.log.Logs;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Service的aop事务管理代理类
 * @author nan.li
 * @version 2017年3月10日
 */
public class ServiceProxy
{
    /**
     * 当前线程拥有的那个连接对象
     */
    public static ThreadLocal<Connection> currentThreadConnectionMap = new ThreadLocal<Connection>();
    
    /**
     * 当前所处的调用层级
     */
    public static ThreadLocal<Integer> currentThreadCallNumMap = new ThreadLocal<Integer>();
    
    static String BaseStartString = ">>>>>>>>>>>>>>>>";
    
    static String BaseEndString = "<<<<<<<<<<<<<<<<";
    
    /**
     * 代理一个service类，加入自动事务管理控制
     * @author nan.li
     * @param clazz
     * @param jdbc
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T proxyService(Class<T> targetClass, MyJdbc jdbc)
    {
        //检查targetClass上的具体的方法是否加了注解。如果加了，就启用事务管理的方式；否则，直接普通执行
        MethodInterceptor methodInterceptor = new MethodInterceptor()
        {
            //            obj - "this", the enhanced object   obj是生成的那个代理对象实例
            //            method - intercepted Method 
            //            args - argument array; primitive types are wrapped 
            //            proxy - used to invoke super (non-intercepted method); may be called as many times as needed 
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                throws Throwable
            {
                //Logs.i(String.format("Begin to invoke proxy method: %s", method.getName()));
                Object result = null;
                //如果加了事务处理的注解标记
                if (method.isAnnotationPresent(Transactional.class))
                {
                    //是否当前正在处于service调用service的最外层调用中
                    //默认值为false，即正处于嵌套调用中
                    //此为嵌套事务管理的完美解决方案！
                    boolean isInOutMostMethod = false;
                    
                    //记录当前的方法栈的层级：进入时+1，退出时-1
                    if (currentThreadConnectionMap.get() == null)
                    {
                        //第一层
                        currentThreadCallNumMap.set(1);
                    }
                    else
                    {
                        //嵌套的层，每进入一次，就将当前所处的层级+1
                        currentThreadCallNumMap.set(currentThreadCallNumMap.get() + 1);
                    }
                    Logs.i(String.format("%s对%s方法开启事务管理代理...", StringUtils.repeat(BaseStartString, currentThreadCallNumMap.get()), method.getName()));
                    
                    //开启事务，进行调用处理
                    Connection connection = null;
                    if (currentThreadConnectionMap.get() == null)
                    {
                        isInOutMostMethod = true;//当前处于最外层
                        //在最外层开始的时候打开Connection
                        Logs.i(String.format("【事务管理框架】获取新连接..."));
                        connection = jdbc.getNewConnection();
                        connection.setAutoCommit(false);
                        //将该连接对象设置到当前线程的对象表中
                        currentThreadConnectionMap.set(connection);
                    }
                    else
                    {
                        Logs.i(String.format("【事务管理框架】currentThreadConnectionMap已经有connection了，直接重用之..."));
                        //多个声明了@Transactional的方法在嵌套调用的时候，确保在一个事务中被调用。并且事务仅在service的最外层被打开！
                        connection = currentThreadConnectionMap.get();
                    }
                    try
                    {
                        //调用具体的service代码
                        result = proxy.invokeSuper(obj, args);
                        
                        //service代码的底层通过使用特殊的getSmartConnection()和closeSmart()两个方法，配合currentThreadConnectionMap.get()，可以达到以下目的：
                        //可以达到：  A、智能申请连接（1.直接接管当前的事务管理的连接 2.当前无事务，则自己主动创建一个新连接）
                        //可以达到：  B、智能关闭连接（1.如果当前需要事务管理，那么则什么都不做，交由事务管理框架去关闭  2.否则，自己主动关闭该连接）
                        
                        //仅在最外层进行事务提交。嵌套的无视之
                        if (isInOutMostMethod)
                        {
                            //一切完事，提交事务
                            Logs.i(String.format("【事务管理框架】提交事务..."));
                            connection.commit();
                        }
                    }
                    catch (SQLException e)
                    {
                        /**
                         *  什么时候使用事务?
                         *  当你需要一次执行多条SQL语句时，可以使用事务。
                         *  通俗一点说，就是，如果这几条SQL语句全部执行成功，则才对数据库进行一次更新，如果有一条SQL语句执行失败，则这几条SQL语句全部不进行执行，这个时候需要用到事务。
                         */
                        
                        //e.printStackTrace();
                        //在最外层会捕捉到并处理的！
                        
                        //出现了SQL的异常，则回滚事务
                        
                        //其实是可以设置存储点的 
                        //Savepoint piont = conn.setSavepoint(); 
                        //conn.rollback(point);
                        //如果你没有设置存储点，他会回滚到你设置禁止事务自动提交的时候，因为你是先设置禁止自动提交的，再进行executeUpdate(sql)的，所以他会回滚到你的所有执行的这几个sql语句前的状态。
                        //也就是说，如果没有设置存储点，那么会回滚到最开始的时候！
                        
                        //此处，只对出现的SQLException进行回滚，其他异常一律不回滚！          
                        
                        //事务的回滚规则:
                        //通常情况下，如果在事务中抛出了未检查异常（继承自 RuntimeException 的异常），则默认将回滚事务。
                        //如果没有抛出任何异常，或者抛出了已检查异常，则仍然提交事务。
                        //这通常也是大多数开发者希望的处理方式，也是 EJB 中的默认处理方式。
                        //但是，我们可以根据需要人为控制事务在抛出某些未检查异常时任然提交事务，或者在抛出某些已检查异常时回滚事务。
                        //当前，以上只是通常意义上的处理方式。对于实际情况，更应该是：只对出现的SQLException进行回滚，其他异常一律不回滚！
                        
                        if (isInOutMostMethod)
                        {
                            try
                            {
                                Logs.i(String.format("【事务管理框架】service执行出现异常，回滚事务...", method.getName()));
                                connection.rollback();
                            }
                            catch (Exception e1)
                            {
                                e1.printStackTrace();
                            }
                        }
                        //代理类里出现的异常不可以掩盖，必须全部抛给上层去处理！
                        //因此任何异常都需要抛出给上层。必要的时候甚至可以不抓取任何异常！因为这个代理的方法 throws Throwable
                        throw e;
                    }
                    catch (Exception e)
                    {
                        //e.printStackTrace();
                        //如果不是SQLException，那么就没有必要进行事务回滚！
                        throw e;
                    }
                    finally
                    {
                        if (isInOutMostMethod)
                        {
                            if (connection != null)
                            {
                                try
                                {
                                    Logs.i(String.format("【事务管理框架】关闭连接..."));
                                    connection.close();
                                }
                                catch (Exception e1)
                                {
                                    e1.printStackTrace();
                                }
                            }
                            
                            //在最外层结束的时候，关闭Connection。将当前线程表的连接对象清空掉
                            currentThreadConnectionMap.set(null);
                            //currentThreadConnectionMap.remove();
                        }
                        Logs.i(String.format("%s对%s方法开启事务管理代理结束！", StringUtils.repeat(BaseEndString, currentThreadCallNumMap.get()), method.getName()));
                        //调用完毕后，将当前所处的层级-1
                        currentThreadCallNumMap.set(currentThreadCallNumMap.get() - 1);
                    }
                }
                else
                {
                    //没加注解标记，那么直接调用即可
                    result = proxy.invokeSuper(obj, args);
                }
                return result;
            }
        };
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);//设置动态代理的父类信息
        // 回调方法
        enhancer.setCallback(methodInterceptor);//设置方法过滤器
        // 创建代理对象
        return (T)enhancer.create();
    }
}
