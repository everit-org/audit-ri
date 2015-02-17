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

import org.everit.osgi.audit.ri.authorization.AuditRiPermissionConstants;

/**
 * Property keys used by the Audit RI.
 */
public final class AuditRiPropertyConstants {

    // TODO static block + package

    private static final String PREFIX = "org.everit.osgi.audit.ri.props.";

    /**
     * Property key to identify the target resourceId used by the
     * {@link AuditRiPermissionConstants#CREATE_AUDIT_APPLICATION}.
     */
    public static final String AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID =
            PREFIX + "AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID";

    private AuditRiPropertyConstants() {
    }

}
