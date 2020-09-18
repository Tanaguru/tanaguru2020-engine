package com.tanaguru.webextresult;

import com.tanaguru.domain.entity.pageresult.ElementResult;

import java.util.Collection;
import java.util.Map;

public class WebextTestResult {
    private Long id;
    private String name;
    private String description;
    private String lang;
    private String type;
    private int counter = 0;
    private Collection<String> tags;
    private Collection<ElementResult> data;
    private Map<String, Collection<String>> ressources;
    private Map<String, Collection<String>> marks;

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

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = tags;
    }

    public Collection<ElementResult> getData() {
        return data;
    }

    public void setData(Collection<ElementResult> data) {
        this.data = data;
    }

    public Map<String, Collection<String>> getRessources() {
        return ressources;
    }

    public void setRessources(Map<String, Collection<String>> ressources) {
        this.ressources = ressources;
    }

    public Map<String, Collection<String>> getMarks() {
        return marks;
    }

    public void setMarks(Map<String, Collection<String>> marks) {
        this.marks = marks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
