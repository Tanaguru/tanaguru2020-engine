package com.tanaguru.domain.entity.membership.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author lpedrau
 */
@Entity
@Table(name = "user_attempts")
public class UserAttempts implements Serializable {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	@Column(nullable = false, unique = true)
    @Size(min = 4, max = 30)
    @NotBlank
    private String username;
	
	@Column(nullable = false)
    @NotNull
    private int attempts;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;
	
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
    
    public int getAttempts() {
    	return attempts;
    }
    
    public void setAttempts(int attempts) {
    	this.attempts = attempts;
    }
    
    public Date getLastModified() {
    	return lastModified;
    }
    
    public void setLastModified(Date lastModified) {
    	this.lastModified = lastModified;
    }
}
