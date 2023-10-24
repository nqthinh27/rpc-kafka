package vn.com.rpckafka.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcRequestBody {
    boolean required() default true;
}
