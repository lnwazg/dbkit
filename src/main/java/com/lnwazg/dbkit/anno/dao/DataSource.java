package com.lnwazg.dbkit.anno.dao;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 标记使用的数据源
 * @author nan.li
 * @version 2016年5月23日
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface DataSource
{
    String value();
}
