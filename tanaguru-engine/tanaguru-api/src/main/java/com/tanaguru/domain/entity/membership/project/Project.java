package com.tanaguru.domain.entity.membership.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanaguru.domain.entity.audit.Resource;
import com.tanaguru.domain.entity.audit.Scenario;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.contract.Contract;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "project")
public class Project implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    @NotBlank
    private String name;

    @Column
    private String domain;

    @Valid
    @NotNull
    @ManyToOne(targetEntity = Contract.class)
    private Contract contract;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private Collection<ProjectAppUser> projectAppUsers;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private Collection<Act> acts;

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    private Collection<Resource> resources;

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    private Collection<Scenario> scenarios;

    @Column
    private boolean allowPageAudit = true;
    
    @Column
    private boolean allowSiteAudit = true;
    
    @Column
    private boolean allowScenarioAudit = true;
    
    @Column
    private boolean allowUploadAudit = true;
    
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

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Collection<ProjectAppUser> getProjectAppUsers() {
        return projectAppUsers;
    }

    public void setProjectAppUsers(Collection<ProjectAppUser> projectAppUsers) {
        this.projectAppUsers = projectAppUsers;
    }

    public Collection<Act> getActs() {
        return acts;
    }

    public void setActs(Collection<Act> acts) {
        this.acts = acts;
    }

    public Collection<Resource> getResources() {
        return resources;
    }

    public void setResources(Collection<Resource> resources) {
        this.resources = resources;
    }

    public Collection<Scenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(Collection<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isAllowPageAudit() {
        return allowPageAudit;
    }

    public void setAllowPageAudit(boolean allowPageAudit) {
        this.allowPageAudit = allowPageAudit;
    }

    public boolean isAllowSiteAudit() {
        return allowSiteAudit;
    }

    public void setAllowSiteAudit(boolean allowSiteAudit) {
        this.allowSiteAudit = allowSiteAudit;
    }

    public boolean isAllowScenarioAudit() {
        return allowScenarioAudit;
    }

    public void setAllowScenarioAudit(boolean allowScenarioAudit) {
        this.allowScenarioAudit = allowScenarioAudit;
    }

    public boolean isAllowUploadAudit() {
        return allowUploadAudit;
    }

    public void setAllowUploadAudit(boolean allowUploadAudit) {
        this.allowUploadAudit = allowUploadAudit;
    }
    
}
