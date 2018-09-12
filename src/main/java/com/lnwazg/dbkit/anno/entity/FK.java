package com.lnwazg.dbkit.anno.entity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 外键字段的标记， Foreign Key的缩写<br>
 * 那么就可以直接根据主键类的实例，与外键类，直接查询出一个外键关联列表<br>
 * 参考：getForeignList(obj, class);<br>
 * getForeignObj(obj, class);
 * @author nan.li
 * @version 2016年5月12日
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface FK
{
}