package com.tanaguru.domain.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class ContractDTO {

    @NotNull
    @NotEmpty
    private String name;

    private long ownerId;

    @NotNull
    private Date dateEnd;

    private int projectLimit;

    private int auditLimit;

    private boolean restrictDomain;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public int getAuditLimit() {
        return auditLimit;
    }

    public void setAuditLimit(int auditLimit) {
        this.auditLimit = auditLimit;
    }

    public int getProjectLimit() {
        return projectLimit;
    }

    public void setProjectLimit(int projectLimit) {
        this.projectLimit = projectLimit;
    }

    public boolean isRestrictDomain() {
        return restrictDomain;
    }

    public void setRestrictDomain(boolean restrictDomain) {
        this.restrictDomain = restrictDomain;
    }
}
