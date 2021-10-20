package com.tanaguru.domain.dto;

import com.tanaguru.domain.entity.audit.TanaguruTest;

import java.util.Collection;

public class TanaguruTestDTO {
    private long id;

    private int number; 
    
    private String name;

    private String description;
    
    private String status;
    
    private String code;

    private Collection<String> tags;

    public TanaguruTestDTO() {
    }

    public TanaguruTestDTO(TanaguruTest tanaguruTest) {
        this.id = tanaguruTest.getId();
        this.number = tanaguruTest.getNumber();
        this.name = tanaguruTest.getName();
        this.description = tanaguruTest.getDescription();
        this.tags = tanaguruTest.getTags();
        this.status = tanaguruTest.getStatus();
        this.code = tanaguruTest.getCode();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
}
