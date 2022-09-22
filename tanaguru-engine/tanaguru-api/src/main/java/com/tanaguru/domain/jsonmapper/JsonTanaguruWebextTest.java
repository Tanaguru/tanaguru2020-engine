package com.tanaguru.domain.jsonmapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * @author rcharre
 */
public class JsonTanaguruWebextTest implements Serializable {
    private String name;
    private String description;
    private String query;
    private String expectedNbElements;
    private String filter;
    private String analyzeElements;
    private String status;
    private int number;
    private Collection<String> tags;
    private Map<String, Collection<String>> ressources;

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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getExpectedNbElements() {
        return expectedNbElements;
    }

    public void setExpectedNbElements(String expectedNbElements) {
        this.expectedNbElements = expectedNbElements;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getAnalyzeElements() {
        return analyzeElements;
    }

    public void setAnalyzeElements(String analyzeElements) {
        this.analyzeElements = analyzeElements;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = tags;
    }

    public Map<String, Collection<String>> getRessources() {
        return ressources;
    }

    public void setRessources(Map<String, Collection<String>> ressources) {
        this.ressources = ressources;
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
    
}

