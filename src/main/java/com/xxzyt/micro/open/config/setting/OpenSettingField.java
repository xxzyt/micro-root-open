package com.xxzyt.micro.open.config.setting;


import com.xxzyt.micro.open.config.enums.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据结构属性注解。
 *
 * @author 李吉昆
 * @date 2019-12-17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.METHOD})
public @interface OpenSettingField {

    /**
     * JSON属性映射名称
     **/
    String name();

    /**
     * 描述
     */
    String description() default "";

    /**
     * UI类型
     */
    FieldType type() default FieldType.INPUT;
}
