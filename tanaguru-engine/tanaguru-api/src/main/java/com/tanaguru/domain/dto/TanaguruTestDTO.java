package com.tanaguru.domain.dto;

import com.tanaguru.domain.entity.audit.Mark;
import com.tanaguru.domain.entity.audit.TanaguruTest;

import java.util.Collection;

public class TanaguruTestDTO {
    private long id;

    private Integer number; 
    
    private String name;

    private String description;
    
    private String status;
    
    private String testStatus;
    
    private String lang;
    
    private String code;

    private Collection<String> tags;
    
    private String node;
    
    private Mark mark;

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
        this.testStatus = tanaguruTest.getTestStatus();
        this.lang = tanaguruTest.getLang();
        this.mark = tanaguruTest.getMark();
        this.node = tanaguruTest.getNode();
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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(Mark mark) {
        this.mark = mark;
    }
    
}
