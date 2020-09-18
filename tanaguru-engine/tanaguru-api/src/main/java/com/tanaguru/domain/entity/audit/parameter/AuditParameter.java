package com.tanaguru.domain.entity.audit.parameter;

import com.tanaguru.domain.constant.EAuditParameter;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author rcharre
 */
@Table(name = "audit_parameter")
@Entity
public class AuditParameter implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EAuditParameter code;

    @ManyToOne
    @Valid
    @NotNull
    @JoinColumn
    private AuditParameterFamily auditParameterFamily;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EAuditParameter getCode() {
        return code;
    }

    public void setCode(EAuditParameter code) {
        this.code = code;
    }

    public AuditParameterFamily getAuditParameterFamily() {
        return auditParameterFamily;
    }

    public void setAuditParameterFamily(AuditParameterFamily family) {
        this.auditParameterFamily = family;
    }
}
