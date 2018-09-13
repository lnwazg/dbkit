package com.lnwazg.dbkit.anno.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 加入事务管理控制<br>
 * benchmark 研究发现，开启事务会明显降低程序的执行速度！<br>
 * 但这也是合乎理论的！因为多了一个开启事务以及commit的过程！<br>
 * 框架的事务管理过程已经完美，再也不需要自己手工干预！
 * @author nan.li
 * @version 2016年5月12日
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Transactional
{
}