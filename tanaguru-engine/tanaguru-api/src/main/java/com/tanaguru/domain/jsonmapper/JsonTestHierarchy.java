package com.tanaguru.domain.jsonmapper;

import java.util.Collection;

public class JsonTestHierarchy {
    private String name;
    private String code;
    private Collection<String> urls;
    private Collection<JsonTestHierarchy> children;
    private int rank;

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

    public Collection<String> getUrls() {
        return urls;
    }

    public void setUrls(Collection<String> urls) {
        this.urls = urls;
    }

    public Collection<JsonTestHierarchy> getChildren() {
        return children;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setChildren(Collection<JsonTestHierarchy> children) {
        this.children = children;
    }
}
