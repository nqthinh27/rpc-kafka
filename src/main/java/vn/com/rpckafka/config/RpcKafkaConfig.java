package vn.com.rpckafka.config;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "rpc-client")
public class RpcKafkaConfig {

    private final Map<String, SendReplyTopics> queueConfig = new HashMap<>();

    public Map<String, SendReplyTopics> getQueueConfig() {
        return queueConfig;
    }

    @Bean
    public Map<String, SendReplyTopics> sendReplyTopics() {
        Map<String, SendReplyTopics> sendReplyTopics = new HashMap<>();
        queueConfig.forEach((key, value) -> sendReplyTopics.put(value.getServiceName(), value));
        return sendReplyTopics;
    }

    @Bean("replyKafkaTemplates")
    public Map<String, ReplyingKafkaTemplate<String, Object, String>> replyKafkaTemplates(ProducerFactory<String, Object> pf,
                                                                                          ConsumerFactory<String, String> cf) {
        Map<String, ReplyingKafkaTemplate<String, Object, String>> replyKafkaTemplates = new HashMap<>();

        queueConfig.forEach((key, value) -> replyKafkaTemplates.put(value.getServiceName(), createReplyingKafkaTemplate(pf, cf, value.getReplyTopics())));

        return replyKafkaTemplates;
    }

    public ReplyingKafkaTemplate<String, Object, String> createReplyingKafkaTemplate(ProducerFactory<String, Object> pf,
                                                                                     ConsumerFactory<String, String> cf,
                                                                                     String replyTopic) {
        ContainerProperties containerProperties = new ContainerProperties(replyTopic);
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        containerProperties.setKafkaConsumerProperties(props);

        KafkaMessageListenerContainer<String, String> repliesContainer = new KafkaMessageListenerContainer<>(cf, containerProperties);
        repliesContainer.getContainerProperties().setGroupId(UUID.randomUUID().toString());
        repliesContainer.setAutoStartup(false);

        ReplyingKafkaTemplate<String, Object, String> replyTemplate = new ReplyingKafkaTemplate<>(pf, repliesContainer);

        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(60));
        replyTemplate.setSharedReplyTopic(true);
        return replyTemplate;
    }

    @Data
    public static class SendReplyTopics {
        private String serviceName;
        private String sendTopics;
        private String replyTopics;
    }
}
