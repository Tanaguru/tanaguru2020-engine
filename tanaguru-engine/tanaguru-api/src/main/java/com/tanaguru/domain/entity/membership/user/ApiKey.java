package com.tanaguru.domain.entity.membership.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.tanaguru.domain.entity.membership.project.Project;

/**
 * @author lpedrau
 */
@Entity
@Table(name = "api_key")
public class ApiKey implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @Valid
    @NotNull
    private Project project;

    @ManyToOne
    @Valid
    @JoinColumn(name = "app_user_id")
    @NotNull
    private User user;

    @Column
    private String key;
    
    public ApiKey() {}

    public ApiKey(User user, Project project, String key) {
        this.user = user;
        this.project = project;
        this.key = key;
    }
    
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getId() {
        return id;
    }
    
}
