package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import com.tanaguru.domain.entity.membership.user.User;

import java.util.Date;
import java.util.Map;

public interface AuditSchedulerService {

    /**
     * Create an @see AuditScheduler and add it to the running list
     * @param audit The @see Audit to add @see AuditScheduler to
     * @param timer The time between 2 audits
     * @return The @see AuditScheduler
     */
    AuditScheduler createAuditScheduler(Audit audit, int timer);

    /**
     * Modify an @see AuditScheduler and update the running list
     * @param auditScheduler The updated @see AuditScheduler
     * @param timer The timer between 2 @see Audit
     * @param lastExecution The last execution of the audit scheduler
     * @return The updated @see AuditScheduler
     */
    AuditScheduler modifyAuditScheduler(AuditScheduler auditScheduler, int timer, Date lastExecution);

    /**
     * Delete an @see AuditScheduler and update the running list
     * @param auditScheduler The @see AuditScheduler to delete
     */
    void deleteAuditScheduler(AuditScheduler auditScheduler);

    /**
     *
     * @return The running @see AuditScheduler map
     */
    Map<Long, AuditScheduler> getSchedulersMap();

    /**
     * Check if a given @see User can add an @see AuditScheduler on a given @see Audit
     * @param user The @see User
     * @param audit The @see Audit
     * @return True id the @see User has the authority
     */
    boolean userCanScheduleOnAudit(User user, Audit audit);
}
