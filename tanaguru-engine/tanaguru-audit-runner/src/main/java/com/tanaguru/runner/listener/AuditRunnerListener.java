package com.tanaguru.runner.listener;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.runner.AuditRunner;
import com.tanaguru.webextresult.WebextPageResult;

/**
 * @author rcharre
 */
public interface AuditRunnerListener {
    /**
     * Event fired when audit start
     *
     * @param auditRunner The audit runner
     */
    void onAuditStart(AuditRunner auditRunner);

    /**
     * Event fired when audit get new page
     *
     * @param auditRunner The audit runner
     * @param name        The name of the page (title of the page if not given by default)
     * @param url         The url of the page
     * @param rank        The rank of the page in the audit
     * @param result      The @see WebextPageResult of the page
     * @param screenshot  The screenshot of the page
     * @param source      The Source code of the page
     */
    void onAuditNewPage(AuditRunner auditRunner, String name, String url, int rank, WebextPageResult result, String screenshot, String source);

    /**
     * Event fired when @see Audit end
     *
     * @param auditRunner The audit runner
     */
    void onAuditEnd(AuditRunner auditRunner);

    /**
     * Add a log to an @see Audit
     * @param auditRunner The current @see AuditRunner
     * @param logLevel The @see ELogLevel of the @AuditLog
     * @param message The message of the @see AuditLog
     */
    void onAuditLog(AuditRunner auditRunner, EAuditLogLevel logLevel, String message);
}
