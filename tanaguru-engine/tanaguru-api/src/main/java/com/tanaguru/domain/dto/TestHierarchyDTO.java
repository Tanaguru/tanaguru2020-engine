package com.tanaguru.domain.dto;

import com.tanaguru.domain.entity.audit.TestHierarchy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;

public class TestHierarchyDTO {
    private Long id;
    private Long referenceId;
    private Long parentId;
    private Collection<String> urls = new ArrayList<>();
    private String name;
    private String code;
    private Integer rank;
    private Boolean isDeleted;
    private long webextEngineId;

    public TestHierarchyDTO() {
    }

    public TestHierarchyDTO(TestHierarchy testHierarchy) {
        this.id = testHierarchy.getId();

        if(testHierarchy.getReference() != null) {
            this.referenceId = testHierarchy.getReference().getId();
        }
        if(testHierarchy.getParent() != null){
            this.parentId = testHierarchy.getParent().getId();
        }
        this.urls.addAll(testHierarchy.getUrls());
        this.name = testHierarchy.getName();
        this.code = testHierarchy.getCode();
        this.rank = testHierarchy.getRank();
        this.isDeleted = testHierarchy.isDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Collection<String> getUrls() {
        return urls;
    }

    public void setUrls(Collection<String> urls) {
        this.urls = urls;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
    
    public long getWebextEngineId() {
        return webextEngineId;
    }
    
    public void setWebextEngineId(long webextEngineId) {
        this.webextEngineId = webextEngineId;
    }
}
