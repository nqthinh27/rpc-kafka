package com.provision.rpckafka.kafka;

import com.provision.rpckafka.annotation.*;
import com.provision.rpckafka.common.ConvertUtils;
import com.provision.rpckafka.entity.server.RpcServerRequest;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RpcControllerInvoker {

    private static final Logger log = LoggerFactory.getLogger(RpcControllerInvoker.class);

    private final Object controller;
    // Key is the full path name, value is the method to handle that action
    private final Map<ApiMethod, Method> handlers = new HashMap<>();

    public RpcControllerInvoker(Object controller) {
        Assert.notNull(controller, "Parameter `controller` must not be null");

        Class<?> controllerClass = controller.getClass();
        RpcController controllerAnnotation = controllerClass.getAnnotation(RpcController.class);
        Assert.notNull(controllerAnnotation, "Parameter `controller` must have annotation @RpcController");

        String prefixPath = controllerAnnotation.path();
        this.controller = controller;
        Method[] methods = controllerClass.getMethods();



        for (Method method : methods) {
            RpcRequestMapping requestMappingAnnotation = method.getAnnotation(RpcRequestMapping.class);
            if (requestMappingAnnotation == null) {
                continue;
            }

            ApiMethod key = ApiMethod.builder()
                    .path(prefixPath + requestMappingAnnotation.path())
                    .method(requestMappingAnnotation.method())
                    .build();

            if (handlers.containsKey(key)) {
                throw new IllegalStateException(String.format("Duplicated handlers found for api `%s %s`.", key.getMethod(), key.getPath()));
            }
            handlers.put(key, method);
            log.debug("Mapped api `{} {}` in controller `{}#{}`", key.getMethod(), key.getPath(), controllerClass.getName(), method);
        }
    }

    public boolean canHandle(String path, String method) {
        return handlers.keySet().stream().anyMatch(apiMethod -> path.equals(apiMethod.getPath()) && method.equals(apiMethod.getMethod()));
    }

    public Boolean ifContainsAuthorizationHeader(Method method) {
        Boolean check = false;

        Parameter[] parameters = method.getParameters();

        for(Parameter parameter:parameters) {
            RpcRequestHeader rpcRequestHeader = parameter.getAnnotation(RpcRequestHeader.class);

            if (rpcRequestHeader == null)
                continue;

            if (rpcRequestHeader.name().equals("Authorization")) {
                check = true;
                break;
            }
        }
        return check;
    }

    public Object handle(RpcServerRequest request) throws InvocationTargetException, IllegalAccessException {
        Assert.isTrue(canHandle(request.getPath(), request.getMethod()), "Path and method of the handler must match");
        Method handler = handlers.get(new ApiMethod(request.getPath(), request.getMethod()));
        Assert.notNull(handler, String.format("Handler for `%s` must exist", request.getPath()));

        // Find all required parameters
        Class<?>[] parameterTypes = handler.getParameterTypes();
        String[] parameterNames = Arrays.stream(handler.getParameters()).map(Parameter::getName).toArray(String[]::new);
        // The arguments that will be passed to the handler method
        Object[] args = new Object[parameterTypes.length];

        try {
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if ("com.provision.rpckafka.entity.server.Request".equals(parameterType.getName())) {
                    args[i] = request;
                } else {
                    try {
                        Map<String, Object> body = request.getBody();
                        Object value = body.get(parameterNames[i]);
                        args[i] = ConvertUtils.convertObjectToBase(value, parameterType);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(String.format("Unable to instantiate parameter of type `%s` for handler at %s#%s.",
                                parameterType.getName(),
                                controller.getClass().getName(),
                                handler.getName()));
                    }
                }
            }

            return handler.invoke(controller, args);
        } catch (Exception e) {
            String error = String.format("Failed to invoke handler `%s`", request.getPath());
            log.error(error, e);
            throw e;
        }
    }

    public Object[] getArgs(Method handler, RpcServerRequest request) {
        Class<?>[] parameterTypes = handler.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        Parameter[] parameters = handler.getParameters();
        for (int i = 0; i  < parameters.length; i++) {
            Annotation[] annotations = parameters[i].getAnnotations();
            for( Annotation annotation : annotations) {
                if (annotation instanceof RpcRequestHeader) {
                    if (request.getHeader() == null) {
                        args[i] = null;
                    } else {
                        String annotationName = ((RpcRequestHeader) annotation).name();
                        if (!StringUtils.isEmpty(annotationName)) {
                            args[i] = ConvertUtils.convertObjectToBase(
                                    request.getHeader().get(((RpcRequestHeader) annotation).name()),
                                    parameterTypes[i]
                            );
                        } else {
                            args[i] = ConvertUtils.convertMapToObject(request.getHeader(), parameterTypes[i]);
                        }
                    }
                } else if (annotation instanceof RpcRequestBody) {
                    if (request.getBody() == null) {
                        args[i] = null;
                    } else {
                        args[i] = ConvertUtils.convertMapToObject(request.getBody(), parameterTypes[i]);
                    }
                } else if (annotation instanceof RpcPathVariable) {
                    if (((RpcPathVariable) annotation).name() == null) {
                        //TODO: Handle @RpcPathVariable
                    }
                }
            }
        }
        return args;
    }

    public Object handleDto(RpcServerRequest request) throws InvocationTargetException, IllegalAccessException {
        Assert.isTrue(canHandle(request.getPath(), request.getMethod()), "Path and method of the handler must match");
        Method handler = handlers.get(new ApiMethod(request.getPath(), request.getMethod()));
        Assert.notNull(handler, String.format("Handler for `%s` must exist", request.getPath()));

        Object[] args = getArgs(handler, request);
        return handler.invoke(controller, args);
    }

    @Data
    @Builder
    public static class ApiMethod {
        private String path;
        private String method;
    }
}

