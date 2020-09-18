package com.tanaguru.domain.entity.audit;

import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;

import javax.persistence.*;
import java.util.Collection;

/**
 * @author rcharre
 */
@Table(name = "audit_reference")
@Entity
public class AuditReference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Audit audit;

    @ManyToOne
    private TestHierarchy testHierarchy;

    @Column
    private boolean isMain = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public TestHierarchy getTestHierarchy() {
        return testHierarchy;
    }

    public void setTestHierarchy(TestHierarchy testHierarchy) {
        this.testHierarchy = testHierarchy;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }
}
