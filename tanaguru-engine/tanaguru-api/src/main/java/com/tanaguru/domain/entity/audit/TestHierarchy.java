package com.tanaguru.domain.entity.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author rcharre
 */
@Entity
@Table(name = "test_hierarchy")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class TestHierarchy implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne
    private TestHierarchy reference;

    @ManyToOne
    @JsonIgnore
    private TestHierarchy parent;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Collection<String> urls;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Collection<TestHierarchy> children;

    @ManyToMany(targetEntity = TanaguruTest.class, cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "test_hierarchy_tanaguru_test",
            joinColumns = {@JoinColumn(name = "test_hierarchy_id")},
            inverseJoinColumns = @JoinColumn(name = "tanaguru_test_id"))
    private Collection<TanaguruTest> tanaguruTests;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @NotBlank
    private String code;

    @Column(nullable = false)
    @Min(0)
    private int rank;

    @Column
    private boolean isDeleted = false;
    
    @Column
    private boolean isDefault = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TestHierarchy getParent() {
        return parent;
    }

    public void setParent(TestHierarchy parent) {
        this.parent = parent;
    }

    public Collection<TestHierarchy> getChildren() {
        return children;
    }

    public void setChildren(Collection<TestHierarchy> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public TestHierarchy getReference() {
        return reference;
    }

    public void setReference(TestHierarchy reference) {
        this.reference = reference;
    }

    public Collection<String> getUrls() {
        return urls;
    }

    public void setUrls(Collection<String> urls) {
        this.urls = urls;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Collection<TanaguruTest> getTanaguruTests() {
        return tanaguruTests;
    }

    public void setTanaguruTests(Collection<TanaguruTest> tanaguruTests) {
        this.tanaguruTests = tanaguruTests;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
