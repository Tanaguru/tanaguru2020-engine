package com.tanaguru.domain.entity.membership.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanaguru.domain.constant.EContractRole;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "contract_role")
public class ContractRole implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EContractRole name;

    @Column
    @JsonIgnore
    private boolean isHidden;

    @ManyToMany(targetEntity = ContractAuthority.class, fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(
            name = "contract_role_contract_authority",
            joinColumns = {@JoinColumn(name = "contract_role_id")},
            inverseJoinColumns = @JoinColumn(name = "contract_authority_id"))
    private Collection<ContractAuthority> authorities;

    @OneToMany(mappedBy = "contract")
    private Collection<ContractAppUser> contractAppUsers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EContractRole getName() {
        return name;
    }

    public void setName(EContractRole name) {
        this.name = name;
    }

    public Collection<ContractAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<ContractAuthority> authorities) {
        this.authorities = authorities;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }
}
