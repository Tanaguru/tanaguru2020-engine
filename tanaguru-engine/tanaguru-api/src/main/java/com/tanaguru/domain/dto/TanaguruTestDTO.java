package com.tanaguru.domain.dto;

import com.tanaguru.domain.entity.audit.TanaguruTest;

import java.util.Collection;

public class TanaguruTestDTO {
    private long id;

    private String name;

    private String description;

    private Collection<String> tags;

    public TanaguruTestDTO() {
    }

    public TanaguruTestDTO(TanaguruTest tanaguruTest) {
        this.id = tanaguruTest.getId();
        this.name = tanaguruTest.getName();
        this.description = tanaguruTest.getDescription();
        this.tags = tanaguruTest.getTags();
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
}
