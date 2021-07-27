package com.tanaguru.domain.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author rcharre
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EAuditStatus {
    PENDING,
    RUNNING,
    PENDING_CONSOLIDATION,
    CONSOLIDATING,
    DONE,
    ERROR,
    STOPPED
}
