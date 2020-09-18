package com.tanaguru.domain.entity.pageresult;

import java.io.Serializable;

/**
 * @author rcharre
 */
public class ElementRole implements Serializable {
    private String implicit;
    private String explicit;

    public String getImplicit() {
        return implicit;
    }

    public void setImplicit(String implicit) {
        this.implicit = implicit;
    }

    public String getExplicit() {
        return explicit;
    }

    public void setExplicit(String explicit) {
        this.explicit = explicit;
    }
}
