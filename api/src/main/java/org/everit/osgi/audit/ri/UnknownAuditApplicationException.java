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

/**
 * Signs that a method was invoked with an application that does not exist.
 */
public class UnknownAuditApplicationException extends RuntimeException {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 6784626196905796600L;

    /**
     * The name of the application that does not exist.
     */
    private final String applicationName;

    /**
     * Constructor.
     *
     * @param applicationName
     *            the name of the application that does not exist
     */
    public UnknownAuditApplicationException(final String applicationName) {
        super("audit application [" + applicationName + "] does not exist");
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

}
