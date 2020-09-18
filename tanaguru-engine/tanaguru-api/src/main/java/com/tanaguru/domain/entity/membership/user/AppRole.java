package com.tanaguru.domain.entity.membership.user;


import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.contract.ContractRole;
import com.tanaguru.domain.entity.membership.project.ProjectRole;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "app_role")
public class AppRole implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EAppRole name;

    @ManyToMany(targetEntity = AppAuthority.class, fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(
            name = "app_role_app_authority",
            joinColumns = {@JoinColumn(name = "app_role_id")},
            inverseJoinColumns = @JoinColumn(name = "app_authority_id"))
    private Collection<AppAuthority> authorities;

    @ManyToOne
    @Valid
    @NotNull
    private ContractRole overrideContractRole;

    @ManyToOne
    @Valid
    @NotNull
    private ProjectRole overrideProjectRole;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EAppRole getName() {
        return name;
    }

    public void setName(EAppRole name) {
        this.name = name;
    }

    public Collection<AppAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<AppAuthority> authorities) {
        this.authorities = authorities;
    }

    public ContractRole getOverrideContractRole() {
        return overrideContractRole;
    }

    public void setOverrideContractRole(ContractRole overrideContractRole) {
        this.overrideContractRole = overrideContractRole;
    }

    public ProjectRole getOverrideProjectRole() {
        return overrideProjectRole;
    }

    public void setOverrideProjectRole(ProjectRole overrideProjectRole) {
        this.overrideProjectRole = overrideProjectRole;
    }
}
