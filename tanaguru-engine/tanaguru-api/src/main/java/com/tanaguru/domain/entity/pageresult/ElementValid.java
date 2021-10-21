package com.tanaguru.domain.entity.pageresult;

import java.io.Serializable;

/**
 * @author lpedrau
 */
public class ElementValid implements Serializable {
    
    private Double target;
    private Double status;
    
    public Double getTarget() {
        return target;
    }
    public void setTarget(Double target) {
        this.target = target;
    }
    public Double getStatus() {
        return status;
    }
    public void setStatus(Double status) {
        this.status = status;
    }
}
