package vn.com.rpckafka.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import vn.com.rpckafka.annotation.RpcController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class RpcControllerResolver {

    private static final Logger log = LoggerFactory.getLogger(RpcControllerResolver.class);

    // The key is the prefix path pattern, value is the corresponding invoker
    private final Map<String, RpcControllerInvoker> invokers = new HashMap<>();

    private final ApplicationContext applicationContext;

    public RpcControllerResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.bootstrap();
    }

    public RpcControllerInvoker findInvoker(String path, String method) {
        RpcControllerInvoker invoker = null;
        Set<String> prefixPaths = invokers.keySet();
        for (String prefix : prefixPaths) {
            if (path.startsWith(prefix)) {
                invoker = invokers.get(prefix);
                if (invoker.canHandle(path, method))
                    return invoker;
            }
        }
        return null;
    }

    private void bootstrap() {
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RpcController.class);
        for (String controllerName : controllers.keySet()) {
            Object controller = controllers.get(controllerName);
            Class<?> rpcController = controller.getClass();

            RpcController controllerAnnotation = rpcController.getAnnotation(RpcController.class);
            String prefixPath = controllerAnnotation.path();
            if (invokers.containsKey(prefixPath)) {
                throw new IllegalStateException(String.format("Duplicated controllers found for path `%s`.", prefixPath));
            }
            invokers.put(prefixPath, new RpcControllerInvoker(controller));
            log.debug("Mapped path `{}` to controller controller `{}`", prefixPath, rpcController.getName());
        }
    }

}
