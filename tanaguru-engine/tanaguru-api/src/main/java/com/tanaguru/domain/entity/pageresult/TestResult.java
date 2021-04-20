package com.tanaguru.domain.entity.pageresult;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * @author rcharre
 */
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Table(name = "test_result")
@Entity
public class TestResult implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private Page page;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private TanaguruTest tanaguruTest;

    @Column
    private String status;

    @Column
    private int nbElementFailed = 0;

    @Column
    private int nbElementPassed = 0;

    @Column
    private int nbElementCantTell = 0;

    @Column
    private int nbElementTested = 0;

    @Column
    private int nbElementUntested = 0;

    @JsonIgnore
    @OneToMany(mappedBy = "testResult", cascade = CascadeType.REMOVE)
    private Collection<ElementResult> elementResults;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Collection<String>> marks;

    public Collection<ElementResult> getElementResults() {
        return elementResults;
    }

    public void setElementResults(Collection<ElementResult> elementResults) {
        this.elementResults = elementResults;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Map<String, Collection<String>> getMarks() {
        return marks;
    }

    public void setMarks(Map<String, Collection<String>> marks) {
        this.marks = marks;
    }

    public int getNbElementFailed() {
        return nbElementFailed;
    }

    public void setNbElementFailed(int nbElementFailed) {
        this.nbElementFailed = nbElementFailed;
    }

    public int getNbElementPassed() {
        return nbElementPassed;
    }

    public void setNbElementPassed(int nbElementPassed) {
        this.nbElementPassed = nbElementPassed;
    }

    public int getNbElementCantTell() {
        return nbElementCantTell;
    }

    public void setNbElementCantTell(int nbElementCantTell) {
        this.nbElementCantTell = nbElementCantTell;
    }

    public int getNbElementTested() {
        return nbElementTested;
    }

    public void setNbElementTested(int nbElementTested) {
        this.nbElementTested = nbElementTested;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public TanaguruTest getTanaguruTest() {
        return tanaguruTest;
    }

    public void setTanaguruTest(TanaguruTest tanaguruTest) {
        this.tanaguruTest = tanaguruTest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNbElementUntested() {
        return nbElementUntested;
    }

    public void setNbElementUntested(int nbElementUntested) {
        this.nbElementUntested = nbElementUntested;
    }

    public boolean hasElementResults(){
        return elementResults.size() > 0;
    }
}
