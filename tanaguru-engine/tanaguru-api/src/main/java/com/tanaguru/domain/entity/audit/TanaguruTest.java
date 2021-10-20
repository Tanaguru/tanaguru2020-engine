package com.tanaguru.domain.entity.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author rcharre
 */
@Entity
@Table(name = "tanaguru_test")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class TanaguruTest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private Integer number;
    
    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String contrast;

    @Column
    private String query;

    @Column
    private String expectedNbElements;

    @Column
    private String filter;

    @Column
    private String analyzeElements;
    
    @Column
    private String status;
    
    @Column
    private String code;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Collection<String> tags;


    @JsonIgnore
    @ManyToMany(targetEntity = TestHierarchy.class, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "test_hierarchy_tanaguru_test",
            joinColumns = {@JoinColumn(name = "tanaguru_test_id")},
            inverseJoinColumns = @JoinColumn(name = "test_hierarchy_id"))
    private Collection<TestHierarchy> testHierarchies;

    @Column
    private boolean isDeleted = false;

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

    public Collection<TestHierarchy> getTestHierarchies() {
        return testHierarchies;
    }

    public void setTestHierarchies(Collection<TestHierarchy> testHierarchies) {
        this.testHierarchies = testHierarchies;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getContrast() {
        return contrast;
    }

    public void setContrast(String contrast) {
        this.contrast = contrast;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
    
}

