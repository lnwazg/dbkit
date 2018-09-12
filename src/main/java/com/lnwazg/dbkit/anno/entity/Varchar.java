package com.lnwazg.dbkit.anno.entity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 指定varchar字段的长度
 * @author nan.li
 * @version 2017年4月5日
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Varchar
{
    int value();
}
