package com.tanaguru.domain.dto;

import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author rcharre
 */
public class AuditCommandDTO implements Serializable {
    @NotNull
    private EAuditType type;

    private Long projectId;

    @NotEmpty
    private String name;

    private Map<EAuditParameter, String> parameters;

    @NotNull
    private Collection<Long> references;

    @NotNull
    private Long mainReference;

    public Map<EAuditParameter, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<EAuditParameter, String> parameters) {
        this.parameters = parameters;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public EAuditType getType() {
        return type;
    }

    public void setType(EAuditType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Long> getReferences() {
        return references;
    }

    public void setReferences(Collection<Long> references) {
        this.references = references;
    }

    public Long getMainReference() {
        return mainReference;
    }

    public void setMainReference(Long mainReference) {
        this.mainReference = mainReference;
    }
}
