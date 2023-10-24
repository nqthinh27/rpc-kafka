package com.provision.rpckafka.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcPathVariable {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
