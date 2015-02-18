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
package org.everit.osgi.audit.ri;

import org.everit.osgi.audit.dto.AuditEvent;

/**
 * Interface for logging {@link AuditEvent}s to a specific audit application.
 */
public interface InternalLoggingService {

    /**
     * Persists the given audit event to the event store. For e.g. to database, file, etc.
     *
     * @param applicationName
     *            the audit event will be logged under this audit application, cannot be <code>null</code>
     * @param auditEvent
     *            the event to persist, cannot be <code>null</code>
     * @throws NullPointerException
     *             if the <code>applicationName</code> or <code>auditEvent</code> parameter is <code>null</code>
     */
    void logEvent(String applicationName, AuditEvent auditEvent);

}