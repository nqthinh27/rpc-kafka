package com.provision.rpckafka.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcRequestHeader {
    String name() default "";
    String value() default "";
}
