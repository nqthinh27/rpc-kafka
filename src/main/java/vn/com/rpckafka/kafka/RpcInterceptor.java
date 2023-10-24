package vn.com.rpckafka.kafka;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import vn.com.rpckafka.entity.server.RpcServerRequest;
import vn.com.rpckafka.entity.server.RpcServerResponse;

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
