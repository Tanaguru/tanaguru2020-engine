package com.tanaguru.domain.entity.membership.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanaguru.domain.constant.EProjectRole;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "project_role")
public class ProjectRole implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EProjectRole name;

    @Column
    @JsonIgnore
    private boolean isHidden;

    @ManyToMany(targetEntity = ProjectAuthority.class, fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(
            name = "project_role_project_authority",
            joinColumns = {@JoinColumn(name = "project_role_id")},
            inverseJoinColumns = @JoinColumn(name = "project_authority_id"))
    private Collection<ProjectAuthority> authorities;

    @OneToMany(mappedBy = "project")
    private Collection<ProjectAppUser> projectAppUsers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EProjectRole getName() {
        return name;
    }

    public void setName(EProjectRole name) {
        this.name = name;
    }

    public Collection<ProjectAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<ProjectAuthority> authorities) {
        this.authorities = authorities;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

}
