package com.tanaguru.domain.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author rcharre
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EAuditType implements Serializable {
    @JsonProperty("SITE")
    SITE,
    @JsonProperty("PAGE")
    PAGE,
    @JsonProperty("UPLOAD")
    UPLOAD,
    @JsonProperty("SCENARIO")
    SCENARIO;

    @JsonCreator
    public static EAuditType fromString(String string) {
        return valueOf(string);
    }
}
