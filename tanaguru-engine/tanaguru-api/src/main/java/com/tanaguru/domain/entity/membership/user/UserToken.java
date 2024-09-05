package com.tanaguru.domain.entity.membership.user;

import java.util.Date;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_token")
public class UserToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @Valid
    @JoinColumn(name = "app_user_id")
    @NotNull
    private User user;

    @Column
    private String token;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiration;

    public UserToken() {
    }

    public UserToken(User user, String token, Date expiration) {
        this.user = user;
        this.token = token;
        this.expiration = expiration;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
}
