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

import org.everit.osgi.audit.ri.dto.AuditApplication;

/**
 * Interface to manage audit applications.
 */
public interface AuditApplicationManager {

    /**
     * Creates an application and assigns it to a new resource automatically. If the application already exists, it will
     * be ignored.
     *
     * @param applicationName
     *            the name of the application to create, cannot be <code>null</code>
     * @return the created {@link AuditApplication}
     * @throws NullPointerException
     *             if {@code applicationName} is <code>null</code>
     */
    void initAuditApplication(String applicationName);

}
