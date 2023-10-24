package com.provision.rpckafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.provision.rpckafka.config.RpcKafkaConfig;
import com.provision.rpckafka.constant.ResponseCode;
import com.provision.rpckafka.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import com.provision.rpckafka.entity.client.RpcClientRequest;
import com.provision.rpckafka.entity.client.RpcClientResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class KafkaService {

    @Value("${spring.kafka.timeout:30}")
    private int timeout;

    @Value("${spring.kafka.longTimeout:120}")
    private int longTimeout;

    @Value("${spring.kafka.longTimeoutUrl:''}")
    private String longTimeoutUrl;

    @Autowired
    @Qualifier("replyKafkaTemplates")
    private Map<String, ReplyingKafkaTemplate<String, Object, String>> replyKafkaTemplates;

    @Autowired
    public Map<String, RpcKafkaConfig.SendReplyTopics> sendReplyTopics;

    private ReplyingKafkaTemplate<String, Object, String> getReplyingKafkaTemplate(String serviceName) {
        return replyKafkaTemplates.get(serviceName);
    }

    public Object kafkaRequestReply(Object request, String serviceName) throws InterruptedException, ExecutionException, TimeoutException {
        ReplyingKafkaTemplate<String, Object, String> template = getReplyingKafkaTemplate(serviceName);
        String sendTopic = sendReplyTopics.get(serviceName).getSendTopics();
        ProducerRecord<String, Object> record = new ProducerRecord<>(sendTopic, request);
        if (!template.isRunning()) {
            template.start();
        }
        if (!template.waitForAssignment(Duration.ofSeconds(10))) {
            throw new CustomException(ResponseCode.CODE.ERROR_IN_BACKEND, ResponseCode.MSG.ERROR_IN_BACKEND, "Error in Backend");
        }
        RequestReplyFuture<String, Object, String> replyFuture = template.sendAndReceive(record);
        SendResult<String, Object> sendResult = replyFuture.getSendFuture().get(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> consumerRecord = null;
        if (StringUtils.isNotBlank(longTimeoutUrl)) {
            String [] arrUrl = longTimeoutUrl.split(",");
            for (String url : arrUrl) {
                if (request.toString().contains(url)) {
                    consumerRecord = replyFuture.get(longTimeout, TimeUnit.SECONDS);
                    return consumerRecord.value();
                }
            }
            consumerRecord = replyFuture.get(timeout, TimeUnit.SECONDS);
        } else {
            consumerRecord = replyFuture.get(timeout, TimeUnit.SECONDS);
        }
        return consumerRecord.value();
    }

    public <T> RpcClientResponse<T> kafkaRequestReply(RpcClientRequest request, String serviceName, TypeReference<T> responseTypeReference) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String body = convertObjectToRequest(request);
        Object sendReply = kafkaRequestReply(body, serviceName);
        return convertResponseToObject((String)sendReply, responseTypeReference);
    }

    private static String convertObjectToRequest(RpcClientRequest request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        final byte[] data = objectMapper.writeValueAsBytes(request);
        return new String(data, StandardCharsets.UTF_8);
    }

    private static <T> RpcClientResponse<T> convertResponseToObject(String response, TypeReference<T> objectClass) throws IOException {
        if (response == null) return null;
        RpcClientResponse<T> result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        JsonNode node = objectMapper.readTree(response);
        JavaType jt = objectMapper.getTypeFactory().constructType(objectClass);
        result = RpcClientResponse.<T>builder()
                .status(node.get("status").asInt())
                .message(node.get("message").asText())
                .data(objectMapper.readValue(objectMapper.treeAsTokens(node.get("data")), jt))
                .build();

        return result;
    }
}
