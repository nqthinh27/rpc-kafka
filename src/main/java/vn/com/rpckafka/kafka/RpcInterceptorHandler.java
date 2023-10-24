package vn.com.rpckafka.kafka;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import vn.com.rpckafka.entity.server.RpcServerRequest;
import vn.com.rpckafka.entity.server.RpcServerResponse;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RpcInterceptorHandler {

    private final List<RpcInterceptor> rpcInterceptors = new ArrayList<>();

    public RpcInterceptorHandler() {
        this((RpcInterceptor[]) null);
    }

    public RpcInterceptorHandler(@Nullable RpcInterceptor... rpcInterceptors) {
        this(rpcInterceptors != null ? Arrays.asList(rpcInterceptors) : Collections.emptyList());
    }

    public RpcInterceptorHandler(List<RpcInterceptor> rpcInterceptors) {
        this.rpcInterceptors.addAll(rpcInterceptors);
    }

    @Autowired
    public RpcInterceptorHandler(ApplicationContext applicationContext) {
        Map<String, RpcInterceptor> interceptors = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RpcInterceptor.class);
        if (interceptors.isEmpty()) {
            rpcInterceptors.add(getDefaultRpcInterceptor());
        } else {
            rpcInterceptors.addAll(interceptors.values().stream().sorted().collect(Collectors.toList()));
        }
    }

    protected RpcInterceptor getDefaultRpcInterceptor() {
        return new DefaultRpcInterceptor();
    }

    boolean applyPreHandle(RpcServerRequest request, RpcServerResponse<?> response, RpcControllerInvoker invoker) {
        for (RpcInterceptor interceptor: rpcInterceptors) {
            if (!interceptor.preHandle(request, response, invoker)) {
                return false;
            }
        }
        return true;
    }

    void applyPostHandle(RpcServerRequest request, RpcServerResponse<?> response, RpcControllerInvoker invoker) {
        for (int i = rpcInterceptors.size() - 1; i >= 0; i--) {
            RpcInterceptor interceptor = rpcInterceptors.get(i);
            interceptor.postHandle(request, response, invoker);
        }
    }

    public static class DefaultRpcInterceptor implements RpcInterceptor {
    }
}
