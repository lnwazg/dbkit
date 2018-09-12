package com.lnwazg.dbkit.anno.entity;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 表或字段的注释信息
 */
@Target({TYPE, FIELD})
@Retention(RUNTIME)
public @interface Comment
{
    /**
     * 如果写了，则采用该值作为表名称；否则，以类名称作为表名
     * @author nan.li
     * @return
     */
    String value();
}
