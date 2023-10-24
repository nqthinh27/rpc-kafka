package com.provision.rpckafka.entity.server;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Builder
@Data
@Jacksonized
@Accessors(chain = true)
@Setter
@Getter
public class RpcServerResponse<T> {
    private int status;
    private String message;
    private Map<String, String> header;
    private T data;
}
