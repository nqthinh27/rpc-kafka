package vn.com.rpckafka.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
//    @Value("${kafka-rpc.reply-topics}")
//    private String REPLY_TOPICS;
//    @Value("${kafka-rpc.consumer-group}")
//    private String CONSUMER_GROUPS;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value("${spring.kafka.is-authen}")
    private String isAuthen;

    @Value("${spring.kafka.password}")
    private String password;

    @Value("${spring.kafka.keystore_location}")
    private String keyLocation;

    @Value("${spring.kafka.truststore_location}")
    private String trustLocation;

//    @Bean
//    public ReplyingKafkaTemplate<String, Object, String> replyKafkaTemplate(ProducerFactory<String, Object> pf, KafkaMessageListenerContainer<String, String> replyContainer) {
//        ReplyingKafkaTemplate<String, Object, String> replyTemplate = new ReplyingKafkaTemplate<>(pf, replyContainer);
//
//        replyTemplate.setDefaultReplyTimeout(Duration.ofSeconds(60));
//        replyTemplate.setSharedReplyTopic(true);
//        return replyTemplate;
//    }
//
//    // Listener Container to be set up in ReplyingKafkaTemplate
//    @Bean
//    public KafkaMessageListenerContainer<String, String> replyContainer(ConsumerFactory<String, String> cf) {
//        ContainerProperties containerProperties = new ContainerProperties(REPLY_TOPICS);
//
//        KafkaMessageListenerContainer<String, String> repliesContainer = new KafkaMessageListenerContainer<>(cf, containerProperties);
//        repliesContainer.getContainerProperties().setGroupId(UUID.randomUUID().toString());
//        repliesContainer.setAutoStartup(false);
//        Properties props = new Properties();
//        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//        repliesContainer.getContainerProperties().setKafkaConsumerProperties(props);
//
//        return repliesContainer;
//    }

    // Default Producer Factory to be used in ReplyingKafkaTemplate

    public KafkaProducerConfig() {
    }

    @Bean
    public ProducerFactory<String,Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    // Standard KafkaProducer settings - specifying brokerand serializer
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "20971520");

        return props;
    }
}
