package com.tanaguru.domain.entity.audit.parameter;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.tanaguru.domain.entity.audit.Audit;

import javax.persistence.*;

/**
 * @author rcharre
 */
@Table(name = "audit_audit_parameter_value")
@Entity
public class AuditAuditParameterValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Audit audit;

    @ManyToOne
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private AuditParameterValue auditParameterValue;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public AuditParameterValue getAuditParameterValue() {
        return auditParameterValue;
    }

    public void setAuditParameterValue(AuditParameterValue auditParameterValue) {
        this.auditParameterValue = auditParameterValue;
    }
}
