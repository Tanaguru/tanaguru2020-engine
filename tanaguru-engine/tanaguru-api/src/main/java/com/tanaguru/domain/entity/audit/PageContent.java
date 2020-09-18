package com.tanaguru.domain.entity.audit;


import com.tanaguru.domain.converter.AESConverter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author rcharre
 */
@Entity
@Table(name = "page_content")
public class PageContent implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Convert(converter = AESConverter.class)
    @Column
    private String screenshot;

    @Convert(converter = AESConverter.class)
    @Column
    private String source;

    @OneToOne
    private Page page;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }
}
