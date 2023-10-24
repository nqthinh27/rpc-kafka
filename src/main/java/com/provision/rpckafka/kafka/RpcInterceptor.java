package com.provision.rpckafka.kafka;

import com.provision.rpckafka.entity.server.RpcServerRequest;
import com.provision.rpckafka.entity.server.RpcServerResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

public interface RpcInterceptor extends Comparable<RpcInterceptor>{
    default boolean preHandle(RpcServerRequest request, RpcServerResponse response, RpcControllerInvoker invoker) {
        return true;
    }

    default void postHandle(RpcServerRequest request, RpcServerResponse response, RpcControllerInvoker invoker) {
    }

    @Override
    default int compareTo(RpcInterceptor o) {
        int val1 = this.getClass().getAnnotation(Order.class) != null ? this.getClass().getAnnotation(Order.class).value() : Ordered.LOWEST_PRECEDENCE;
        int val2 = o.getClass().getAnnotation(Order.class) != null ? o.getClass().getAnnotation(Order.class).value() : Ordered.LOWEST_PRECEDENCE;
        return Integer.compare(val1, val2);
    }
}
