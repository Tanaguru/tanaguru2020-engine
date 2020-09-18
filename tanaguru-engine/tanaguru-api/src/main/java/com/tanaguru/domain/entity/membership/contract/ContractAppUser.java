package com.tanaguru.domain.entity.membership.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.user.User;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "contract_app_user")
public class ContractAppUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @NotNull
    @Valid
    private Contract contract;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "app_user_id")
    @Valid
    private User user;

    @ManyToOne
    @NotNull
    @Valid
    private ContractRole contractRole;

    @JsonIgnore
    @OneToMany(mappedBy = "contractAppUser", cascade = CascadeType.REMOVE)
    private Collection<ProjectAppUser> projectAppUsers;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ContractRole getContractRole() {
        return contractRole;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Collection<ProjectAppUser> getProjectAppUsers() {
        return projectAppUsers;
    }

    public void setContractRole(ContractRole contractRole) {
        this.contractRole = contractRole;
    }
}
