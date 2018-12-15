package com.github.linushp.orm.model;


import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityInfo {
    String table();
    boolean isUnderlineKey() default true;
    boolean isIgnoreNull() default true;
    String idFieldName() default  "id";
    String schemaName() default "";
    String selectFields() default "*";
}