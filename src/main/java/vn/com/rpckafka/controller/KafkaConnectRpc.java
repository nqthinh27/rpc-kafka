package vn.com.rpckafka.controller;//package vn.com.viettel.rpckafka.controller;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import vn.com.viettel.rpckafka.annotation.RpcController;
//import vn.com.viettel.rpckafka.annotation.RpcRequestBody;
//import vn.com.viettel.rpckafka.annotation.RpcRequestMapping;
//import vn.com.viettel.rpckafka.entity.message.RequestMessage;
//import vn.com.viettel.rpckafka.service.KafkaConnectService;
//
//@RpcController(path = "/v1.0/rpc/api/demo")
//public class KafkaConnectRpc {
//
//    private final KafkaConnectService connectService;
//
//    public KafkaConnectRpc(KafkaConnectService connectService) {
//        this.connectService = connectService;
//    }
//
//    @RpcRequestMapping(method = "POST")
//    public ResponseEntity<?> getFromList(@RpcRequestBody RequestMessage requestMessage) {
//        boolean check = connectService.checkConnector();
//        return ResponseEntity.status(HttpStatus.OK).body("ok");
//    }
//
//}
