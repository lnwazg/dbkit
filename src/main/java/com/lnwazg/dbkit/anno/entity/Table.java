package com.lnwazg.dbkit.anno.entity;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 数据表的别名<br>
 * 表名称加上别名可以帮助数据库表更加有利于在数据库视图中分类展示<br>
 * 例如：T_SYS_ROLE、T_SYS_USER、T_ALARM_LIST、T_PROJECT，根据不同的前缀，很容易就能区分出不同的表的大致功能<br>
 * 另外，JavaBean的字段建议和数据库表字段完全一致！不建议使用别名！<br>
 * 因为字段加了别名，便引入了框架层的代价，并且就得手动区分SQL与HQL，大大增加了维护成本，提高了开发难度，降低了开发体验！<br>
 * 因此，仅可以为数据库表名加上别名，但是不要为字段作别名映射！
 * @author nan.li
 * @version 2017年5月3日
 */
@Target(TYPE)
@Retention(RUNTIME)
@Deprecated
public @interface Table
{
    /**
     * 如果写了，则采用该值作为表名称；否则，以类名称作为表名<br>
     * 若写了该注解，那么该值不能为空
     * @author nan.li
     * @return
     */
    String value();
}
