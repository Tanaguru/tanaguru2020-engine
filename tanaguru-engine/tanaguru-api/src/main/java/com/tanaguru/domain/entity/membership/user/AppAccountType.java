package com.tanaguru.domain.entity.membership.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tanaguru.domain.constant.EAppAccountType;

@Entity
@Table(name = "app_account_type")
public class AppAccountType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EAppAccountType name;
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EAppAccountType getName() {
        return name;
    }

    public void setName(EAppAccountType name) {
        this.name = name;
    }
}
