package com.tanaguru.domain.entity.membership.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanaguru.domain.entity.membership.project.Project;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author rcharre
 */
@Entity
@Table(name = "contract")
public class Contract implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column
    private int auditLimit = 0;

    @Column
    private int projectLimit = 0;

    @Column
    private boolean restrictDomain = true;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStart;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnd;

    @JsonIgnore
    @OneToMany(mappedBy = "contract")
    private Collection<Project> projects;

    @JsonIgnore
    @OneToMany(mappedBy = "contract")
    private Collection<ContractAppUser> contractAppUsers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAuditLimit() {
        return auditLimit;
    }

    public void setAuditLimit(int auditLimit) {
        this.auditLimit = auditLimit;
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

    public Collection<Project> getProjects() {
        return projects;
    }

    public void setProjects(Collection<Project> projects) {
        this.projects = projects;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<ContractAppUser> getContractAppUsers() {
        return contractAppUsers;
    }

    public void setContractAppUsers(Collection<ContractAppUser> contractAppUsers) {
        this.contractAppUsers = contractAppUsers;
    }

    public int getProjectLimit() {
        return projectLimit;
    }

    public void setProjectLimit(int projectLimit) {
        this.projectLimit = projectLimit;
    }

    public boolean isRestrictDomain() {
        return restrictDomain;
    }

    public void setRestrictDomain(boolean restrictDomain) {
        this.restrictDomain = restrictDomain;
    }
}
