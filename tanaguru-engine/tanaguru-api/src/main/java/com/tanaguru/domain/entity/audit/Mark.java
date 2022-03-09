package com.tanaguru.domain.entity.audit;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Column;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@TypeDefs({
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class Mark implements Serializable {
    
    private Collection<String> attrs;

    public Collection<String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Collection<String> attrs) {
        this.attrs = attrs;
    }
    
}
