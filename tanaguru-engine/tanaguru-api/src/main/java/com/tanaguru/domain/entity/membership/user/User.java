package com.tanaguru.domain.entity.membership.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.data.util.Pair;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author rcharre
 */
@Entity
@Table(name = "app_user")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Size(min = 4, max = 100)
    @NotBlank
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    //@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    @NotBlank
    private String password;

    @Column(nullable = false, unique = true)
    @Email
    @JsonIgnore
    private String email;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @JsonIgnore
    @Column(nullable = false)
    @NotNull
    private boolean isEnabled = false;

    @JsonIgnore
    @Column(nullable = false)
    @NotNull
    private boolean accountNonLocked = true;

    @NotNull
    @Valid
    @ManyToOne
    @JoinColumn
    private AppRole appRole;
    
    private String firstname;
    
    private String lastname;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @JsonIgnore
    private Collection<Pair<String, Date>> modificationPasswordTokens = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Collection<ContractAppUser> contractAppUsers;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @JsonIgnore
    private Collection<Attempt> attempts = new ArrayList<>();
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public AppRole getAppRole() {
        return appRole;
    }

    public void setAppRole(AppRole appRole) {
        this.appRole = appRole;
    }

    public Collection<Pair<String, Date>> getModificationPasswordTokens() {
        return modificationPasswordTokens;
    }

    public void setModificationPasswordTokens(Collection<Pair<String, Date>> modificationPasswordTokens) {
        this.modificationPasswordTokens = modificationPasswordTokens;
    }

    public Collection<ContractAppUser> getContractAppUsers() {
        return contractAppUsers;
    }

    public void setContractAppUsers(Collection<ContractAppUser> contractAppUsers) {
        this.contractAppUsers = contractAppUsers;
    }
    
    public void setAccountNonLocked(boolean accountNonLocked) {
    	this.accountNonLocked = accountNonLocked;
    }
    
    public boolean isAccountNonLocked() {
    	return accountNonLocked;
    }
    
    public Collection<Attempt> getAttempts(){
        return attempts;
    }
    
    public void setAttempts(Collection<Attempt> attempts) {
        this.attempts = attempts;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
}
