package com.tanaguru.service.impl.runner;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.dto.AuditRequest;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.service.AuditRunnerService;
import com.tanaguru.service.AuditService;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author rcharre
 */
@Service
@Transactional
@ConditionalOnProperty(
        name = "auditrunner.profile",
        havingValue = "producer"
)
public class AuditRequestProducerServiceImpl implements AuditRunnerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRequestProducerServiceImpl.class);
    private final Producer<String, AuditRequest> auditRequestProducer;
    private final AuditService auditService;

    @Value("${auditrunner.kafka.topic.auditrequest}")
    private String auditRequestTopicName;

    @Autowired
    public AuditRequestProducerServiceImpl(Producer<String, AuditRequest> auditRequestProducer, AuditService auditService) {
        this.auditRequestProducer = auditRequestProducer;
        this.auditService = auditService;
    }

    @Override
    public void runAudit(Audit audit) {
        AuditRequest auditRequest = new AuditRequest();
        auditRequest.setIdAudit(audit.getId());

        ProducerRecord<String, AuditRequest> producerRecord = new ProducerRecord<>(
                auditRequestTopicName,
                auditRequest
        );

        LOGGER.info("[Audit {}]  Send audit request", audit.getId());
        Future<RecordMetadata> future = auditRequestProducer.send(producerRecord, (metdatata, exception) -> {
            if (exception != null) {
                LOGGER.error("[Audit {}] Failed sending AuditRequest record", audit.getId());
            }
        });

        try {
            future.get(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            auditService.log(audit, EAuditLogLevel.ERROR, e.getMessage().substring(0, 99));
            LOGGER.error("[Audit {}] {}", audit.getId(), e.getMessage());
            throw new IllegalStateException("[Audit " + audit.getId() + "] Could not send auditRequest");
        }
    }

    @Override
    public void stopAudit(Audit audit){
        throw new NotImplementedException("Function not implemented yet");
    }
}
