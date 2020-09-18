package com.tanaguru.config.kafka;

import com.tanaguru.config.kafka.deserializer.AuditRequestDeserializer;
import com.tanaguru.domain.dto.AuditRequest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Properties;


@Configuration
@ConditionalOnProperty(
        name = "auditrunner.profile",
        havingValue = "consumer"
)
public class AuditRequestConsumerConfig {

    @Value("${auditrunner.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value("${auditrunner.kafka.group}")
    private String auditRunnerGroupName;

    @Value("${auditrunner.kafka.topic.auditrequest}")
    private String auditRequestTopicName;

    @Bean(destroyMethod = "close")
    public Consumer<String, AuditRequest> auditRequestConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                auditRunnerGroupName);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                AuditRequestDeserializer.class.getName());

        final Consumer<String, AuditRequest> consumer = new KafkaConsumer<>(props);

        ArrayList<String> topics = new ArrayList<>();
        topics.add(auditRequestTopicName);
        consumer.subscribe(topics);

        return consumer;
    }
}
