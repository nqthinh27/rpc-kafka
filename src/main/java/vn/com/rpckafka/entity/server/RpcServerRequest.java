package vn.com.rpckafka.entity.server;

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
public class RpcServerRequest {
    private String method;
    private String path;
    private Map<String, Object> header;
    private Map<String, Object> body;
}
