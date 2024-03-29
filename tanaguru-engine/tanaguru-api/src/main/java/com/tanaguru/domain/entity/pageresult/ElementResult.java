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
    private String size;

    @Column
    private Integer weight = -1;

    @Column
    private Float ratio = (float) -1;
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private ElementValid valid;
    
    @Column
    private String text;
    
    @Column
    private String background;
    
    @Column
    private String foreground;

    @Column
    private String sourceCode;
    
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Float getRatio() {
        return ratio;
    }

    public void setRatio(Float ratio) {
        this.ratio = ratio;
    }

    public ElementValid getValid() {
        return valid;
    }

    public void setValid(ElementValid valid) {
        this.valid = valid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    
}
