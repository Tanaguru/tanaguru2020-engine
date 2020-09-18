package com.tanaguru.domain.entity.membership;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.user.User;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author rcharre
 */
@Entity
@Table(name = "act")
public class Act implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @Valid
    @NotNull
    private Project project;

    @OneToOne
    @Valid
    @NotNull
    private Audit audit;

    @Column
    private Date date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
