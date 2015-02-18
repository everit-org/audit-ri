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
 * This interface helps to check permissions used by the Audit RI module.
 */
public interface AuditRiPermissionChecker {

    /**
     * Returns the resourceId used for authorization and permission checking in case of
     * {@link AuditRiPermissionConstants#INIT_AUDIT_APPLICATION}.
     *
     * @return the resourceId of the audit application type
     */
    long getAuditApplicationTypeTargetResourceId();

    /**
     * Checks if the authenticated resource has a permission for the
     * {@link AuditRiPermissionConstants#INIT_AUDIT_APPLICATION} permission action.
     *
     * @return <code>true</code> if the permission is allowed, <code>false</code> otherwise
     */
    boolean hasPermissionToInitAuditApplication();

    /**
     * Checks if the authenticated resource has a permission for the
     * {@link AuditRiPermissionConstants#LOG_TO_AUDIT_APPLICATION} permission action.
     *
     * @param applicationName
     *            the name of the application to check, cannot be <code>null</code>
     * @return <code>true</code> if the permission is allowed, <code>false</code> otherwise
     * @throws NullPointerException
     *             if <code>applicationName</code> is <code>null</code>
     * @throws org.everit.osgi.audit.ri.UnknownAuditApplicationException
     *             if the given application not exists
     */
    boolean hasPermissionToLogToAuditApplication(String applicationName);

}
