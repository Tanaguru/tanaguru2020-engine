package com.tanaguru.domain.dto;

public class AuditSchedulerDTO {
    private long auditSchedulerId;
    private long auditId;
    private int timer;

    public long getAuditSchedulerId() {
        return auditSchedulerId;
    }

    public void setAuditSchedulerId(long auditSchedulerId) {
        this.auditSchedulerId = auditSchedulerId;
    }

    public long getAuditId() {
        return auditId;
    }

    public void setAuditId(long auditId) {
        this.auditId = auditId;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }
}
