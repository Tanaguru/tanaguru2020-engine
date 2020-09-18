package com.tanaguru.webextresult;

import java.util.Collection;

public class WebextPageResult {
    private Collection<WebextTagResult> tags;
    private Collection<WebextTestResult> tests;

    public Collection<WebextTagResult> getTags() {
        return tags;
    }

    public void setTags(Collection<WebextTagResult> tags) {
        this.tags = tags;
    }

    public Collection<WebextTestResult> getTests() {
        return tests;
    }

    public void setTests(Collection<WebextTestResult> tests) {
        this.tests = tests;
    }
}
