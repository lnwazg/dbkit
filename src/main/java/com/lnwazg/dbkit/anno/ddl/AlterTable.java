package com.lnwazg.dbkit.anno.ddl;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 修改表结构的指令
 * @author nan.li
 * @version 2017年4月8日
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface AlterTable
{
    //ADD
    //DROP
    //MODIFY
    /**
     * 操作类型
     * @author nan.li
     * @return
     */
    AlterTableEnum value();
}
