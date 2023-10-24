package vn.com.rpckafka.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.com.rpckafka.constant.ConnectorStatus;
import vn.com.rpckafka.model.Connector;
import vn.com.rpckafka.model.ConnectorTask;
import vn.com.rpckafka.model.KafkaConnectStatusInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaConnectService {

    public static final int LIMIT_LAG = 200;

    @Value("${spring.kafka.connectorUrl:''}")
    String connectorUrl;

    private final AdminClient adminClient;
    private final Consumer<String, String> kafkaConsumer;
    private final RestTemplate restTemplate;

    public KafkaConnectService(AdminClient adminClient, Consumer<String, String> kafkaConsumer, RestTemplate restTemplate) {
        this.adminClient = adminClient;
        this.kafkaConsumer = kafkaConsumer;
        this.restTemplate = restTemplate;
    }

    public boolean checkConnector() {
        try {
            ResponseEntity<String> allConnectorStatus = restTemplate.getForEntity(connectorUrl+ "/connectors?expand=status", String.class);
            if (allConnectorStatus.getStatusCodeValue() != HttpStatus.OK.value()) {
                return false;
            }
            Map<String, KafkaConnectStatusInfo> statusResponse =
                    new Gson().fromJson(allConnectorStatus.getBody(), new TypeToken<HashMap<String, KafkaConnectStatusInfo>>() {}.getType());

            if (statusResponse == null) {
                return false;
            }
            if (isWarning(statusResponse)) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isWarning(Map<String, KafkaConnectStatusInfo> statusResponse) {
        try {
            for (String connector : statusResponse.keySet()) {
                KafkaConnectStatusInfo statusInfo = statusResponse.get(connector);
                Connector kafkaConnector = statusInfo.getStatus().getConnector();
                List<ConnectorTask> listTasks = statusInfo.getStatus().getTasks();

                if (!ConnectorStatus.RUNNING.equals(kafkaConnector.getState())
                        || listTasks.stream().anyMatch(t -> !ConnectorStatus.RUNNING.equals(t.getState()))
                        || isExceedLimit(connector)) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    private Map<String, Long> analyzeLag(String groupId) throws ExecutionException, InterruptedException {
//    public Map<String, Map<Integer, Long>> analyzeLag(String groupId) throws ExecutionException, InterruptedException {
        Map<String, Long> response = new HashMap<>();
//        Map<String, Map<Integer, Long>> response = new HashMap<>();
        Map<TopicPartition, Long> consumerGrpOffsets = getConsumerGrpOffsets(groupId);
        Map<TopicPartition, Long> producerOffsets = getProducerOffsets(consumerGrpOffsets);
        Map<TopicPartition, Long> lags = computeLags(consumerGrpOffsets, producerOffsets);
        for (Map.Entry<TopicPartition, Long> lagEntry : lags.entrySet()) {
            String topic = lagEntry.getKey().topic();
            Long lag = lagEntry.getValue();
            if (lag > LIMIT_LAG) {
                response.put(topic, lag);
            }
//            int partition = lagEntry.getKey().partition();
//            Map<Integer, Long> topicMap = response.getOrDefault(topic, new HashMap<>());
//            topicMap.put(partition, lag);
//            response.put(topic, topicMap);
        }
        return response;
    }

    private boolean isExceedLimit(String groupId) throws ExecutionException, InterruptedException {
        Map<TopicPartition, Long> consumerGrpOffsets = getConsumerGrpOffsets(groupId);
        Map<TopicPartition, Long> producerOffsets = getProducerOffsets(consumerGrpOffsets);
        Map<TopicPartition, Long> lags = computeLags(consumerGrpOffsets, producerOffsets);
        for (Map.Entry<TopicPartition, Long> lagEntry : lags.entrySet()) {
            Long lag = lagEntry.getValue();
            if (lag > LIMIT_LAG) {
                return true;
            }
        }
        return false;
    }

    private Map<TopicPartition, Long> getConsumerGrpOffsets(String groupId) throws ExecutionException, InterruptedException {
        ListConsumerGroupOffsetsResult info = adminClient.listConsumerGroupOffsets(groupId);
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = info.partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, Long> groupOffset = new HashMap<>();
        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : topicPartitionOffsetAndMetadataMap.entrySet()) {
            TopicPartition key = entry.getKey();
            OffsetAndMetadata metadata = entry.getValue();
            groupOffset.putIfAbsent(new TopicPartition(key.topic(), key.partition()), metadata.offset());
        }
        return groupOffset;
    }

    private Map<TopicPartition, Long> getProducerOffsets(Map<TopicPartition, Long> consumerGrpOffset) {
        List<TopicPartition> topicPartitions = new LinkedList<>();
        for (Map.Entry<TopicPartition, Long> entry : consumerGrpOffset.entrySet()) {
            TopicPartition key = entry.getKey();
            topicPartitions.add(new TopicPartition(key.topic(), key.partition()));
        }
        return kafkaConsumer.endOffsets(topicPartitions);
    }

    private Map<TopicPartition, Long> computeLags(Map<TopicPartition, Long> consumerGrpOffsets, Map<TopicPartition, Long> producerOffsets) {
        Map<TopicPartition, Long> lags = new HashMap<>();
        for (Map.Entry<TopicPartition, Long> entry : consumerGrpOffsets.entrySet()) {
            Long producerOffset = producerOffsets.get(entry.getKey());
            Long consumerOffset = consumerGrpOffsets.get(entry.getKey());
            long lag = Math.abs(producerOffset - consumerOffset);
            lags.putIfAbsent(entry.getKey(), lag);
        }
        return lags;
    }

    private List<String> getListConnector() {
        ResponseEntity<String> allConnector = restTemplate.getForEntity("http://10.60.158.170:8083/connectors/", String.class);
        String inputString = allConnector.getBody();
        String[] elements = inputString.substring(1, inputString.length() - 1).split(",");
        for (int i = 0; i < elements.length; i++) {
            elements[i] = elements[i].trim().replaceAll("\"", "");
        }
        return List.of(elements);
    }
}
