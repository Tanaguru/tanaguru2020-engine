package com.tanaguru.runner.factory;

import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.TanaguruTest;

import java.util.Collection;
import java.util.HashMap;

public interface ScriptFactory {
    HashMap<String,HashMap<String, StringBuilder>> create(String coreScript, Collection<TanaguruTest> tanaguruTestList, Collection<AuditReference> references);
}
