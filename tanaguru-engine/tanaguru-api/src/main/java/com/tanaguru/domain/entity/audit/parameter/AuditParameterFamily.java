package com.tanaguru.domain.entity.audit.parameter;

import com.tanaguru.domain.constant.EParameterFamily;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author rcharre
 */
@Entity
@Table(name = "audit_parameter_family")
public class AuditParameterFamily implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EParameterFamily code;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EParameterFamily getCode() {
        return code;
    }

    public void setCode(EParameterFamily code) {
        this.code = code;
    }
}
