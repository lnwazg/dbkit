package com.lnwazg.dbkit.anno.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 事务管理的标记<br>
 * 若Service内的方法加上该注解，则该方法将进行事务控制；否则不进行事务控制<br>
 * benchmark 研究发现，开启事务会明显降低程序的执行速度<br>
 * 但这也是合乎预期的，因为多了一个开启事务以及commit的过程。
 * @author nan.li
 * @version 2016年5月12日
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Transactional
{
}