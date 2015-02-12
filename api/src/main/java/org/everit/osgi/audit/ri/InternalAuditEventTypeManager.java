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

import java.util.List;

import org.everit.osgi.audit.dto.AuditEventType;

/**
 * Interface for managing {@link AuditEventType}s belonging to audit applications.
 */
public interface InternalAuditEventTypeManager {

    /**
     * Lazily creates the {@link AuditEventType}s assigned to the given audit applicaiton.
     *
     * @param applicationName
     *            the event types will belong to this audit application
     * @param eventTypeNames
     *            the names of the audit events to create lazily. If an empty array is provided an empty list will be
     *            returned.
     * @return the list of the lazily created event types. The size of the returned list will be the same as the length
     *         of the provided array and the elements are in the same order.
     * @throws NullPointerException
     *             if <code>null</code> array or an array with <code>null</code> element is provided
     */
    List<AuditEventType> getOrCreateAuditEventTypes(String applicationName, String... eventTypeNames);

}
