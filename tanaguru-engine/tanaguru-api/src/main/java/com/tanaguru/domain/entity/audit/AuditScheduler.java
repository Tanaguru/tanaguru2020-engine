package com.tanaguru.domain.entity.audit;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @author rcharre
 */
@Entity
@Table(name = "audit_scheduler")
public class AuditScheduler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @OneToOne
    private Audit audit;

    @Column
    @Min(86400)
    private int scheduler;

    @Column
    private Date lastExecution;

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

    public int getScheduler() {
        return scheduler;
    }

    public void setScheduler(int scheduler) {
        this.scheduler = scheduler;
    }

    public Date getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }
}
