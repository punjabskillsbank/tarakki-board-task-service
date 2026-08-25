package com.tarakki.boardtask.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String eventName();
    String entityName();
    Class<?> entityClass() default void.class;
    String entityIdArgSpel() default "";
    String entityIdResultSpel() default "";
}
