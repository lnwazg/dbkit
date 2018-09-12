package com.lnwazg.dbkit.anno.entity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 某个字段的默认值
 * @author lnwazg@126.com
 * @version 2017年4月5日
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface DefaultValue
{
    String value();
}
