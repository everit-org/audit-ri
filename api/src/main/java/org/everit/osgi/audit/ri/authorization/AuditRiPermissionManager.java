package org.everit.osgi.audit.ri.authorization;

public interface AuditRiPermissionManager {

    void addPermissionToInitAuditApplication(long authorizedResourceId);

    void addPermissionToLogToAuditApplication(long authorizedResourceId, String applicationName);

    void removePermissionInitAuditApplication(long authorizedResourceId);

    void removePermissionLogToAuditApplication(long authorizedResourceId, String applicationName);

}
