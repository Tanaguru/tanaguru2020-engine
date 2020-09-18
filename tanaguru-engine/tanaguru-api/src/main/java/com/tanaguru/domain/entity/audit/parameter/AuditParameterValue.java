package com.tanaguru.domain.entity.audit.parameter;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author rcharre
 */
@Table(name = "audit_parameter_value")
@Entity
public class AuditParameterValue implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(targetEntity = AuditParameter.class)
    @Valid
    @NotNull
    @JoinColumn
    private AuditParameter auditParameter;

    @Column(nullable = false)
    private boolean isDefault = false;

    @Column(nullable = false)
    private String value;

    public AuditParameterValue() {
    }

    public AuditParameterValue(AuditParameter auditParameter, String value, boolean isDefault) {
        this.setAuditParameter(auditParameter);
        this.setValue(value);
        this.setDefault(isDefault);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public AuditParameter getAuditParameter() {
        return auditParameter;
    }

    public void setAuditParameter(AuditParameter auditParameter) {
        this.auditParameter = auditParameter;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
