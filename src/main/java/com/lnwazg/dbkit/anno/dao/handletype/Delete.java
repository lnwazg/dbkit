package com.lnwazg.dbkit.anno.dao.handletype;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 删除
 * @author nan.li
 * @version 2016年5月23日
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Delete
{
    String value();
}
