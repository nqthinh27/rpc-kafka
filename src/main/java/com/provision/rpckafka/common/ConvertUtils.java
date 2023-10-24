package com.provision.rpckafka.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.provision.rpckafka.entity.server.RpcServerRequest;
import com.provision.rpckafka.entity.server.RpcServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConvertUtils {

    public static String convertObjectToResponse(RpcServerResponse response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        final byte[] data = objectMapper.writeValueAsBytes(response);
        return new String(data, StandardCharsets.UTF_8);
    }

    public static RpcServerRequest convertRequestToObject(String request) throws JsonProcessingException {
        if (request == null) return null;
        RpcServerRequest result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        result = objectMapper.readValue(request, RpcServerRequest.class);
        return result;
    }

    public static Object convertObjectToBase(Object value, Class<?> preferClass) {
        if (value == null) {
            return null;
        }
        try {
            switch (preferClass.getName()) {
                case "java.lang.String":
                    return String.valueOf(value);
                case "java.lang.Integer":
                    return Integer.valueOf(String.valueOf(value));
                case "java.lang.Long":
                    return Long.valueOf(String.valueOf(value));
                case "java.lang.Float":
                    return Float.valueOf(String.valueOf(value));
                case "java.lang.Double":
                    return Double.valueOf(String.valueOf(value));
                case "java.lang.Boolean":
                    return Boolean.valueOf(String.valueOf(value));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Unable to convert value to type `%s`", preferClass.getName()));
        }
        return value;
    }

    public static <T> T convertMapToObject(Map<String, Object> body, Class<T> objectClass) {
        T result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        JavaType jt = objectMapper.getTypeFactory().constructType(objectClass);
        result = objectMapper.convertValue(body, jt);

        return result;
    }
}
