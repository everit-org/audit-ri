/**
 * This file is part of Everit - Audit RI API.
 *
 * Everit - Audit RI API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Audit RI API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Audit RI API.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.audit.ri.authorization;

/**
 * This interface helps to manage permissions used by the Audit RI module.
 */
public interface AuditRiAuthorizationManager {

    /**
     * Adds the {@link AuditRiPermissionConstants#INIT_AUDIT_APPLICATION} permission action to the given authorized
     * resourceId.
     *
     * @param authorizedResourceId
     *            the permission will be added to this resourceId
     */
    void addPermissionToInitAuditApplication(long authorizedResourceId);

    /**
     * Adds the {@link AuditRiPermissionConstants#LOG_TO_AUDIT_APPLICATION} permission action to the given authorized
     * resourceId.
     *
     * @param authorizedResourceId
     *            the permission will be added to this resourceId
     * @param applicationName
     *            the authorized resourceId can log events and initialize event types under this audit application,
     *            cannot be <code>null</code>
     * @throws NullPointerException
     *             if the <code>applicationName</code> is <code>null</code>
     */
    void addPermissionToLogToAuditApplication(long authorizedResourceId, String applicationName);

    /**
     * Removes the {@link AuditRiPermissionConstants#INIT_AUDIT_APPLICATION} permission action from the given authorized
     * resourceId.
     *
     * @param authorizedResourceId
     *            the permission will be removed from this resourceId
     */
    void removePermissionInitAuditApplication(long authorizedResourceId);

    /**
     * Removes the {@link AuditRiPermissionConstants#LOG_TO_AUDIT_APPLICATION} permission action from the given
     * authorized resourceId.
     *
     * @param authorizedResourceId
     *            the permission will be removed from this resourceId
     * @param applicationName
     *            the authorized resourceId cannot log events or initialize event types under this audit application any
     *            more, cannot be <code>null</code>
     * @throws NullPointerException
     *             if the <code>applicationName</code> is <code>null</code>
     */
    void removePermissionLogToAuditApplication(long authorizedResourceId, String applicationName);

}
