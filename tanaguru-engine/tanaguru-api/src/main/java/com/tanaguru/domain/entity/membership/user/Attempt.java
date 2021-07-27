package com.tanaguru.domain.entity.membership.user;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lpedrau
 */
public class Attempt implements Serializable {
	
    private int number;
    
    private Date lastModified;
    
    private String ip;
    
    private Date blockedUntil;
    
    public int getNumber() {
    	return number;
    }
    
    public void setNumber(int number) {
    	this.number = number;
    }
    
    public Date getLastModified() {
    	return lastModified;
    }
    
    public void setLastModified(Date lastModified) {
    	this.lastModified = lastModified;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public Date getBlockedUntil() {
        return blockedUntil;
    }
    
    public void setBlockedUntil(Date blockedUntil) {
        this.blockedUntil = blockedUntil;
    }
}
