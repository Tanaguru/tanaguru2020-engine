package com.tanaguru.config.kafka.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.domain.dto.AuditRequest;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class AuditRequestSerializer implements Serializer<AuditRequest> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, AuditRequest data) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsString(data).getBytes();
        } catch (Exception exception) {
            System.out.println("Error in serializing object" + data);
        }
        return retVal;
    }

    @Override
    public void close() {
    }
}
