package org.everit.osgi.audit.ri.authorization;

public interface AuditRiPermissionChecker {

    void checkPermissionToInitAuditApplication();

    void checkPermissionToLogToAuditApplication(String applicationName);

    /**
     * Returns the resourceId used for authorization and permission checking in case of
     * {@link AuditRiPermissions#CREATE_AUDIT_APPLICATION}.
     *
     * @return the resourceId of the audit application type
     */
    long getAuditApplicationTypeTargetResourceId();

    boolean hasPermissionToInitAuditApplication();

    boolean hasPermissionToLogToAuditApplication(String applicationName);

}
