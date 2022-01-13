package com.tanaguru.domain.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ProjectDTO {

    @NotNull
    @NotEmpty
    @Size(max = 50, min = 3)
    private String name;

    private long contractId;

    private String domain;
    
    private boolean allowPageAudit;
    
    private boolean allowSiteAudit;
    
    private boolean allowScenarioAudit;
    
    private boolean allowUploadAudit;
    
    private Boolean istrial;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getContractId() {
        return contractId;
    }

    public void setContractId(long contractId) {
        this.contractId = contractId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isAllowPageAudit() {
        return allowPageAudit;
    }

    public void setAllowPageAudit(boolean allowPageAudit) {
        this.allowPageAudit = allowPageAudit;
    }

    public boolean isAllowSiteAudit() {
        return allowSiteAudit;
    }

    public void setAllowSiteAudit(boolean allowSiteAudit) {
        this.allowSiteAudit = allowSiteAudit;
    }

    public boolean isAllowScenarioAudit() {
        return allowScenarioAudit;
    }

    public void setAllowScenarioAudit(boolean allowScenarioAudit) {
        this.allowScenarioAudit = allowScenarioAudit;
    }

    public boolean isAllowUploadAudit() {
        return allowUploadAudit;
    }

    public void setAllowUploadAudit(boolean allowUploadAudit) {
        this.allowUploadAudit = allowUploadAudit;
    }

    public Boolean getIstrial() {
        return istrial;
    }

    public void setIstrial(Boolean istrial) {
        this.istrial = istrial;
    }
    
}
