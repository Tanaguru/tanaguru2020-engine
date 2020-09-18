package com.tanaguru.domain.dto;

import java.io.Serializable;

public class DemoCommandDTO implements Serializable {
    String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
