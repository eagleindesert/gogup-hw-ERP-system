package com.example.demo.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.example.demo.kafka.dto.ApprovalRequestMessage;
import com.example.demo.kafka.dto.ApprovalResultMessage;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public static final String TOPIC_APPROVAL_REQUEST = "approval-request";
    public static final String TOPIC_APPROVAL_RESULT = "approval-result";

    // ==================== Topics ====================

    @Bean
    public NewTopic approvalRequestTopic() {
        return TopicBuilder.name(TOPIC_APPROVAL_REQUEST)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic approvalResultTopic() {
        return TopicBuilder.name(TOPIC_APPROVAL_RESULT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // ==================== Producer ====================

    @Bean
    public ProducerFactory<String, ApprovalRequestMessage> approvalRequestProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // 타입 헤더 비활성화 - 수신측 패키지가 다를 수 있으므로
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ApprovalRequestMessage> approvalRequestKafkaTemplate() {
        return new KafkaTemplate<>(approvalRequestProducerFactory());
    }

    // ==================== Consumer ====================

    @Bean
    public ConsumerFactory<String, ApprovalResultMessage> approvalResultConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "approval-request-service");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ApprovalResultMessage.class.getName());
        // 타입 헤더 무시 - 송신측과 수신측의 패키지가 다를 수 있으므로
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApprovalResultMessage> approvalResultListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ApprovalResultMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(approvalResultConsumerFactory());
        return factory;
    }
}
