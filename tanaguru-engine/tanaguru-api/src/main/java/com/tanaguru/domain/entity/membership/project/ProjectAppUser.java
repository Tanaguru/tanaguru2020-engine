package com.tanaguru.domain.entity.membership.project;

import com.tanaguru.domain.entity.membership.contract.ContractAppUser;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "project_app_user")
public class ProjectAppUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn
    @NotNull
    @Valid
    private Project project;

    @ManyToOne
    @JoinColumn
    @NotNull
    @Valid
    private ContractAppUser contractAppUser;

    @ManyToOne
    @JoinColumn
    @NotNull
    @Valid
    private ProjectRole projectRole;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ContractAppUser getContractAppUser() {
        return contractAppUser;
    }

    public void setContractAppUser(ContractAppUser contractAppUser) {
        this.contractAppUser = contractAppUser;
    }

    public ProjectRole getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(ProjectRole projectRole) {
        this.projectRole = projectRole;
    }

}
