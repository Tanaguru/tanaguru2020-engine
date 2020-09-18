package com.tanaguru.config.kafka.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.domain.dto.AuditRequest;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AuditRequestDeserializer implements Deserializer<AuditRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRequestDeserializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public AuditRequest deserialize(String topic, byte[] data) {
        ObjectMapper mapper = new ObjectMapper();
        AuditRequest object = null;
        try {
            object = mapper.readValue(data, AuditRequest.class);
        } catch (Exception exception) {
            LOGGER.error("Error in deserializing bytes " + exception);
        }
        return object;
    }

    @Override
    public void close() {
    }
}
