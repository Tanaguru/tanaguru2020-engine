package com.tanaguru.domain.entity.pageresult;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Table(name = "element_result")
@Entity
public class ElementResult implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    private TestResult testResult;

    @Column
    private String accessibleName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Collection<String> canBeReachedUsingKeyboardWith;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Collection<String> isNotExposedDueTo;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Collection<String> isNotVisibleDueTo;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private ElementRole role;

    @Column
    private String status;

    @Column
    private String xpath;

    @Column
    private String cssSelector;

    @Column
    private String tag;

    @Column
    private int size = -1;

    @Column
    private int weight = -1;

    @Column
    private float ratio = -1;

    public String getAccessibleName() {
        return accessibleName;
    }

    public void setAccessibleName(String accessibleName) {
        this.accessibleName = accessibleName;
    }

    public Collection<String> getCanBeReachedUsingKeyboardWith() {
        return canBeReachedUsingKeyboardWith;
    }

    public void setCanBeReachedUsingKeyboardWith(Collection<String> canBeReachedUsingKeyboardWith) {
        this.canBeReachedUsingKeyboardWith = canBeReachedUsingKeyboardWith;
    }

    public Collection<String> getIsNotExposedDueTo() {
        return isNotExposedDueTo;
    }

    public void setIsNotExposedDueTo(Collection<String> isNotExposedDueTo) {
        this.isNotExposedDueTo = isNotExposedDueTo;
    }

    public Collection<String> getIsNotVisibleDueTo() {
        return isNotVisibleDueTo;
    }

    public void setIsNotVisibleDueTo(Collection<String> isNotVisibleDueTo) {
        this.isNotVisibleDueTo = isNotVisibleDueTo;
    }

    public ElementRole getRole() {
        return role;
    }

    public void setRole(ElementRole role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public void setTestResult(TestResult testResult) {
        this.testResult = testResult;
    }

    public String getCssSelector() {
        return cssSelector;
    }

    public void setCssSelector(String cssSelector) {
        this.cssSelector = cssSelector;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }
}
