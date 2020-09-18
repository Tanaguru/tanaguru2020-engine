package com.tanaguru.domain.entity.pageresult;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author rcharre
 */
@Table(name = "tag_result")
@Entity
public class TagResult implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String code;

    @Column
    private String name;

    @Column
    private String status;

    @Column
    private int nbFailures;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public int getNbFailures() {
        return nbFailures;
    }

    public void setNbFailures(int nbFailures) {
        this.nbFailures = nbFailures;
    }
}
