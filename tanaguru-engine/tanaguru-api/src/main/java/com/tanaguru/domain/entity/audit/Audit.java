package com.tanaguru.domain.entity.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditStatus;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.parameter.AuditAuditParameterValue;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author rcharre
 */
@Entity
@Table(name = "audit")
public class Audit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EAuditType type;

    @Column(nullable = false)
    @NotEmpty
    private String name;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStart;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnd;

    @Column(nullable = false)
    private boolean isPrivate;

    @Column(nullable = false)
    @NotEmpty
    private String shareCode;

    @JsonIgnore
    @OneToMany(mappedBy = "audit")
    private Collection<Page> pages;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EAuditStatus status = EAuditStatus.PENDING;

    @Column
    private boolean deleted = false;

    @JsonIgnore
    @OneToMany(mappedBy = "audit", cascade = CascadeType.REMOVE)
    private Collection<AuditAuditParameterValue> parameters;

    @JsonIgnore
    @OneToMany(mappedBy = "audit", cascade = CascadeType.REMOVE)
    private Collection<AuditReference> auditReferences;

    @JsonIgnore
    @OneToMany(mappedBy = "audit", cascade = CascadeType.REMOVE)
    private Collection<AuditLog> auditLogs;

    @JsonIgnore
    @OneToOne(mappedBy = "audit", cascade = CascadeType.REMOVE)
    private AuditScheduler auditScheduler;

    @JsonIgnore
    public Map<EAuditParameter, AuditParameterValue> getParametersAsMap() {
        Map<EAuditParameter, AuditParameterValue> result = new EnumMap<>(EAuditParameter.class);
        if (this.parameters != null) {
            for (AuditAuditParameterValue parameterValue : this.parameters) {
                result.put(parameterValue.getAuditParameterValue().getAuditParameter().getCode(), parameterValue.getAuditParameterValue());
            }
        }
        return result;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Collection<Page> getPages() {
        return pages;
    }

    public void setPages(Collection<Page> pages) {
        this.pages = pages;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public EAuditStatus getStatus() {
        return status;
    }

    public void setStatus(EAuditStatus status) {
        this.status = status;
    }

    public Collection<AuditAuditParameterValue> getParameters() {
        return parameters;
    }

    public void setParameters(Collection<AuditAuditParameterValue> parameters) {
        this.parameters = parameters;
    }

    public void setAuditLogs(Collection<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }

    public Collection<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public EAuditType getType() {
        return type;
    }

    public void setType(EAuditType type) {
        this.type = type;
    }

    public AuditScheduler getAuditScheduler() {
        return auditScheduler;
    }

    public void setAuditScheduler(AuditScheduler auditScheduler) {
        this.auditScheduler = auditScheduler;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public Collection<AuditReference> getAuditReferences() {
        return auditReferences;
    }

    public void setAuditReferences(Collection<AuditReference> auditReferences) {
        this.auditReferences = auditReferences;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
