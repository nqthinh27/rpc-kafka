package com.provision.rpckafka.entity.client;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Builder
@Data
@Jacksonized
public class RpcClientRequest<T> {
    private String method;
    private String path;
    private Map<String, Object> header;
    private T body;
}
