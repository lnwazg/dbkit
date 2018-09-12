package com.lnwazg.dbkit.anno.entity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 索引的标记
 * @author nan.li
 * @version 2016年5月12日
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Index
{
    /**
     * 是否是多重索引
     * @author nan.li
     * @return
     */
    boolean multiple() default false;
}