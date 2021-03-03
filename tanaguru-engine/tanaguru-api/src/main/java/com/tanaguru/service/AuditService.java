package com.tanaguru.service;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.project.Project;

import java.util.Collection;
import org.json.JSONObject;
import org.springframework.data.domain.Pageable;


/**
 * @author rcharre
 */
public interface AuditService {

    /**
     * Add @see AuditLog to and @see Audit
     *
     * @param audit   The @see Audit to add @see AuditLog to
     * @param level   The @see EAuditLevel
     * @param message The message
     */
    void log(Audit audit, EAuditLogLevel level, String message);

    /**
     * Find all @see Audit for a given @see Project
     *
     * @param project The given @see Project
     * @return An @see Audit list
     */
    Collection<Audit> findAllByProject(Project project);

    /**
     * Find page of @see Audit for a given @see Project
     *
     * @param project The given @see Project
     * @param pageable Pageable
     * @return An @see Audit list
     */
    org.springframework.data.domain.Page<Audit> findAllByProject(Project project, Pageable pageable);

    /**
     *
     * @param audit The @see Audit
     * @param shareCode The optional @see Audit shareCode
     * @return True if the @see User has authority to show the @see Audit
     */
    boolean canShowAudit(Audit audit, String shareCode);

    /**
     * Delete a given @see Audit
     * @param audit The given @see Audit
     */
    void deleteAudit(Audit audit);

    /**
     * Delete all @see Audit for a given @see Project
     *
     * @param project The given @see Project
     */
    void deleteAuditByProject(Project project);
    
    /**
     * Return a json object with the information of the audit
     * @param audit the given @see Audit
     * @return json object
     */
    JSONObject toJson(Audit audit);
}
