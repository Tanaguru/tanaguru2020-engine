package com.tanaguru.runner.factory;

import com.tanaguru.domain.entity.audit.TanaguruTest;

import java.util.Collection;
import java.util.HashMap;

public interface ScriptFactory {
    HashMap<String,String> create(String coreScript, Collection<TanaguruTest> tanaguruTestList);
}
