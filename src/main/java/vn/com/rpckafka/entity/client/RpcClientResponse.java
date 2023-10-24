package vn.com.rpckafka.entity.client;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Builder
@Data
@Jacksonized
public class RpcClientResponse<T> {
    private int status;
    private String message;

    private Map<String, String> header;

    private T data;
}
