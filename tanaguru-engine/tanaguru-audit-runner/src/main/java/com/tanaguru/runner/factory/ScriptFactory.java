package com.tanaguru.runner.factory;

import com.tanaguru.domain.entity.audit.TanaguruTest;

import java.util.Collection;

public interface ScriptFactory {
    String create(String coreScript, Collection<TanaguruTest> tanaguruTestList);
}
