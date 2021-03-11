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
    
    @Column( name = "enginename" )
    private String engineName;
    
    @Column( name = "engineversion" )
    private int engineVersion;
    
    @Column( name = "enginecontent" )
    private byte[] engineContent;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int getEngineVersion() {
        return engineVersion;
    }
    
    public void setEngineVersion(int engineVersion) {
        this.engineVersion = engineVersion;
    }
    
    public byte[] getEngineContent() {
        return engineContent;
    }
    
    public void setEngineContent(byte[] engineContent) {
        this.engineContent = engineContent;
    }
    
    public String getEngineName() {
        return engineName;
    }
    
    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }
}
