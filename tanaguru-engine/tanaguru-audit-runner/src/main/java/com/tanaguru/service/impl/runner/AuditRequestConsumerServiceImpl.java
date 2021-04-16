package com.tanaguru.service.impl.runner;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.dto.AuditRequest;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.repository.*;
import com.tanaguru.runner.factory.AuditRunnerFactory;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.MailService;
import com.tanaguru.service.ResultAnalyzerService;
import com.tanaguru.service.impl.MessageService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@ConditionalOnProperty(
        name = "auditrunner.profile",
        havingValue = "consumer"
)
@EnableScheduling
public class AuditRequestConsumerServiceImpl extends AuditRequestServiceSyncStandaloneImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRequestConsumerServiceImpl.class);

    private final Consumer<String, AuditRequest> auditRequestConsumer;

    @Value("${auditrunner.kafka.topic.auditrequest}")
    private String auditRequestTopicName;

    @Autowired
    public AuditRequestConsumerServiceImpl(
            PageRepository pageRepository,
            AuditRepository auditRepository,
            AuditService auditService,
            Consumer<String, AuditRequest> auditRequestConsumer,
            AuditRunnerFactory auditRunnerFactory,
            PageContentRepository pageContentRepository,
            TestResultRepository testResultRepository,
            TestHierarchyResultRepository testHierarchyResultRepository,
            ResultAnalyzerService resultAnalyzerService,
            TestHierarchyRepository testHierarchyRepository,
            ElementResultRepository elementResultRepository,
            MailService mailService,
            MessageService messageService,
            ActRepository actRepository,
            ContractUserRepository contractUserRepository) {

        super(pageRepository,
                auditRepository,
                auditService,
                auditRunnerFactory,
                pageContentRepository,
                testResultRepository,
                testHierarchyResultRepository,
                resultAnalyzerService,
                testHierarchyRepository,
                elementResultRepository,
                mailService,
                messageService,
                actRepository,
                contractUserRepository);
        this.auditRequestConsumer = auditRequestConsumer;
    }


    /**
     * Check for kafka AuditRequest records
     */
    @Scheduled(fixedDelay = 5000)
    protected void checkRecords() {
        LOGGER.debug("Checking for audit request record...");
        ConsumerRecords<String, AuditRequest> consumerRecords = auditRequestConsumer.poll(Duration.ofMillis(100L));
        if (!consumerRecords.isEmpty()) {
            for (ConsumerRecord<String, AuditRequest> consumerRecord : consumerRecords) {
                AuditRequest auditRequest = consumerRecord.value();
                Map<TopicPartition, OffsetAndMetadata> partitionOffsetAndMetadataMap = new HashMap<>();
                partitionOffsetAndMetadataMap.put(
                        new TopicPartition(auditRequestTopicName, consumerRecord.partition()),
                        new OffsetAndMetadata(consumerRecord.offset() + 1));

                Audit audit = auditRepository.findById(auditRequest.getIdAudit())
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, auditRequest.getIdAudit() ));

                runAudit(audit);
                auditRequestConsumer.commitSync(partitionOffsetAndMetadataMap);
            }
        }
    }
}
