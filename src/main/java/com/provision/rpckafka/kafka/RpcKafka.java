package com.provision.rpckafka.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.provision.rpckafka.common.ConvertUtils;
import com.provision.rpckafka.entity.server.RpcServerRequest;
import com.provision.rpckafka.entity.server.RpcServerResponse;
import com.provision.rpckafka.exception.ForbiddenException;
import com.provision.rpckafka.exception.NotFoundException;
import com.provision.rpckafka.exception.UnauthorizedException;
import com.provision.rpckafka.exception.UserNotFoundException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.zalando.problem.AbstractThrowableProblem;

import java.lang.reflect.InvocationTargetException;

;

@Component
public class RpcKafka {

    private Logger LOGGER = LoggerFactory.getLogger(RpcKafka.class);
    private final RpcControllerResolver rpcControllerResolver;

    private final RpcInterceptorHandler rpcInterceptorHandler;

    public RpcKafka(RpcControllerResolver rpcControllerResolver, RpcInterceptorHandler rpcInterceptorHandler) {
        this.rpcControllerResolver = rpcControllerResolver;
        this.rpcInterceptorHandler = rpcInterceptorHandler;
    }

    @KafkaListener(groupId = "${rpc-server.consumer-group}",
            topics = "${rpc-server.listen-topics}",
            concurrency = "${rpc-server.listener.concurrency:#{1}}")
    @SendTo
    public Message<?> rpcListen(ConsumerRecord<String, Object> consumerRecord) throws JsonProcessingException {
        RpcServerResponse<Object> response = RpcServerResponse.builder().build();

        try {
            String requestString = String.valueOf(consumerRecord.value());
            RpcServerRequest request = ConvertUtils.convertRequestToObject(requestString);

            RpcControllerInvoker invoker = rpcControllerResolver.findInvoker(request.getPath(), request.getMethod());

            if (rpcInterceptorHandler.applyPreHandle(request, response, invoker)) {
                Object result = invoker.handleDto(request);

//                if (result instanceof ResponseEntity) {
//                    ResponseEntity responseEntity = (ResponseEntity) result;
//                    response.setHeader(responseEntity.getHeaders().toSingleValueMap());
//                    response.setStatus(responseEntity.getStatusCodeValue());
//                    response.setData(responseEntity.getBody());
//                } else {
                    response.setStatus(HttpStatus.OK.value())
                            .setMessage(HttpStatus.OK.getReasonPhrase())
                            .setData(result);
                //}
                rpcInterceptorHandler.applyPostHandle(request, response, invoker);
            }
        } catch (InvocationTargetException invocationTargetException) {
            Throwable innerException = invocationTargetException.getTargetException();
            handleException(response, innerException);
        } catch (Exception ex) {
            handleException(response, ex);
        }

        String responseString = ConvertUtils.convertObjectToResponse(response);

        return MessageBuilder.withPayload(responseString).build();
    }
    protected void handleException(RpcServerResponse<Object> response, Throwable exception) {
        if (exception instanceof AbstractThrowableProblem) {
            AbstractThrowableProblem abstractThrowableProblem = (AbstractThrowableProblem) exception;
            response.setStatus(abstractThrowableProblem.getStatus().getStatusCode())
                    .setMessage(abstractThrowableProblem.getMessage());
            LOGGER.error(abstractThrowableProblem.getMessage());
            abstractThrowableProblem.printStackTrace();
        } else if (exception instanceof ForbiddenException) {
            response.setStatus(HttpStatus.FORBIDDEN.value())
                    .setMessage(HttpStatus.FORBIDDEN.getReasonPhrase());
        } else if (exception instanceof UnauthorizedException) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value())
                    .setMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        } else if (exception instanceof UserNotFoundException) {
            response.setStatus(HttpStatus.NOT_FOUND.value())
                    .setMessage(HttpStatus.NOT_FOUND.getReasonPhrase());
        } else if (exception instanceof NotFoundException) {
            response.setStatus(HttpStatus.NOT_FOUND.value())
                    .setMessage(HttpStatus.NOT_FOUND.getReasonPhrase());
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setMessage(exception.getMessage());
            LOGGER.error(exception.getMessage());
            exception.printStackTrace();
        }

//        ErrorRes dto;
//        if (exception instanceof CustomException) {
//            CustomException customException = (CustomException) exception;
//            dto = new ErrorRes(customException.getErrorCode(), customException.getErrorMessage(), customException.getDetailErrorMessage());
//            response.setData(ResponseEntity.status(HttpStatus.OK).body(dto));
//        } else {
//            dto = new ErrorRes(ResponseCode.CODE.ERROR_IN_BACKEND, ResponseCode.MSG.ERROR_IN_BACKEND, "Error in Backend");
//        }
//        response.setData(ResponseEntity.status(HttpStatus.OK).body(dto));
    }
}