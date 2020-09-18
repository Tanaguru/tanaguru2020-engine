package com.tanaguru.webextresult;

public class WebextTagResult {
    private String id;
    private String name;
    private String status;
    private int nbfailures;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNbfailures() {
        return nbfailures;
    }

    public void setNbfailures(int nbfailures) {
        this.nbfailures = nbfailures;
    }
}
