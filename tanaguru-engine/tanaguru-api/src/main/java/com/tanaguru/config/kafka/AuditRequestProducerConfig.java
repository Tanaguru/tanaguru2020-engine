package com.tanaguru.config.kafka;

import com.tanaguru.config.kafka.serializer.AuditRequestSerializer;
import com.tanaguru.domain.dto.AuditRequest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConditionalOnProperty(
        name = "auditrunner.profile",
        havingValue = "producer"
)
public class AuditRequestProducerConfig {

    @Value("${auditrunner.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public Producer<String, AuditRequest> auditRequestProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "AuditRequestProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                AuditRequestSerializer.class.getName());
        return new KafkaProducer<>(props);
    }
}
