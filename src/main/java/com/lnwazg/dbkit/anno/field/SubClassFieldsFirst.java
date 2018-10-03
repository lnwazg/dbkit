package com.lnwazg.dbkit.anno.field;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 子类的字段优先
 * @author linan
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface SubClassFieldsFirst
{
}