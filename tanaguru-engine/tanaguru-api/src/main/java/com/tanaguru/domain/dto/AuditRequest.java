package com.tanaguru.domain.dto;

import java.io.Serializable;

/**
 * @author rcharre
 */

public class AuditRequest implements Serializable {
    private long idAudit;

    public long getIdAudit() {
        return idAudit;
    }

    public void setIdAudit(long idAudit) {
        this.idAudit = idAudit;
    }
}
