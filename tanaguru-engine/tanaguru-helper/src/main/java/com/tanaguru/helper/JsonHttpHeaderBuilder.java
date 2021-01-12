package com.tanaguru.helper;

import org.springframework.http.HttpHeaders;

/**
 * 
 * @author lpedrau
 *
 */
public class JsonHttpHeaderBuilder {

    private JsonHttpHeaderBuilder() {
    }
    
    /**
     * Set the headers settings
     * @return httpheaders with the settings
     */
    public static HttpHeaders setUpJsonHeaders(String filename, String extension) {
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Disposition", "attachment; filename=\""+filename+"."+extension+"\"");
        header.add("Cache-Control", "no-store");
        header.add("Pragma", "no-cache");
        return header;
    }
    
}
