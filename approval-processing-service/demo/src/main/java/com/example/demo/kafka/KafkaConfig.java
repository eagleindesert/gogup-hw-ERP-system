package com.example.demo.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 설정 클래스
 * - approval-request 토픽 소비 (Approval Request Service로부터)
 * - approval-result 토픽 발행 (Approval Request Service로)
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // ==================== Topics ====================
    
    @Bean
    public NewTopic approvalRequestTopic() {
        return TopicBuilder.name("approval-request")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic approvalResultTopic() {
        return TopicBuilder.name("approval-result")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // ==================== Producer Configuration ====================
    
    @Bean
    public ProducerFactory<String, ApprovalResultMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // 타입 헤더 비활성화 - 수신측 패키지가 다를 수 있으므로
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ApprovalResultMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ==================== Consumer Configuration ====================
    
    @Bean
    public ConsumerFactory<String, ApprovalRequestMessage> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ApprovalRequestMessage.class.getName());
        // 타입 헤더 무시 - 송신측과 수신측의 패키지가 다를 수 있으므로
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApprovalRequestMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ApprovalRequestMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
