package com.tanaguru.domain.entity.audit;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author lpedrau
 */
@Entity
@Table(name = "webext_engine")
public class WebextEngine implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column( name = "engineversion" )
    private String engineVersion;
    
    @Column( name = "enginecontent" )
    private byte[] engineContent;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getEngineVersion() {
        return engineVersion;
    }
    
    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }
    
    public byte[] getEngineContent() {
        return engineContent;
    }
    
    public void setEngineContent(byte[] engineContent) {
        this.engineContent = engineContent;
    }
}
