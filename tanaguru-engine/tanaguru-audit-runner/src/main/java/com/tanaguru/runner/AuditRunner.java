package com.tanaguru.runner;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.runner.listener.AuditRunnerListener;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * @author rcharre
 */
public interface AuditRunner extends Runnable {

    /**
     * Add a listener to the auditRunner
     *
     * @param auditRunnerListener The listener to add
     */
    void addListener(AuditRunnerListener auditRunnerListener);

    /**
     * Remove a listener to the auditRunner
     *
     * @param auditRunnerListener The listener to remove
     */
    void removeListener(AuditRunnerListener auditRunnerListener);

    /**
     * Audit getter
     *
     * @return The audit
     */
    Audit getAudit();

    /**
     * Audit setter
     *
     * @param audit The audit to set
     */
    void setAudit(Audit audit);

    /**
     * Fire a new page event
     *
     * @param url                   of the page
     * @param name                  of the page
     * @param auditIfAlreadyVisited Tag if engine needs to force the audit
     */
    void onGetNewPage(String url, String name, boolean auditIfAlreadyVisited);

    /**
     * @param url the url to get
     */
    void webDriverGet(String url);

    RemoteWebDriver getDriver();
}
