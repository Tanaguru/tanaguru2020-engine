package com.tanaguru.domain.entity.audit;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.tanaguru.domain.entity.pageresult.StatusResult;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author rcharre
 */
@Entity
@Table(name = "page")
public class Page implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    @NotNull
    private int rank;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @NotNull
    private Audit audit;

    @JsonIgnore
    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    private Collection<TestHierarchyResult> testHierarchyResults;

    @JsonIgnore
    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    private Collection<StatusResult> statusResults;

    @JsonIgnore
    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    private Collection<TestResult> testResults;

    @Column
    @NotNull
    @NotEmpty
    private String name;

    @Column
    @NotNull
    @NotEmpty
    private String url;

    @JsonIgnore
    @OneToOne(mappedBy = "page", cascade = CascadeType.REMOVE)
    private PageContent pageContent;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PageContent getPageContent() {
        return pageContent;
    }

    public void setPageContent(PageContent pageContent) {
        this.pageContent = pageContent;
    }

    public Collection<TestHierarchyResult> getTestHierarchyResults() {
        return testHierarchyResults;
    }

    public void setTestHierarchyResults(Collection<TestHierarchyResult> testHierarchyResults) {
        this.testHierarchyResults = testHierarchyResults;
    }

    public Collection<TestResult> getTestResults() {
        return testResults;
    }

    public void setTestResults(Collection<TestResult> testResults) {
        this.testResults = testResults;
    }

    public Collection<StatusResult> getStatusResults() {
        return statusResults;
    }

    public void setStatusResults(Collection<StatusResult> statusResults) {
        this.statusResults = statusResults;
    }
}
