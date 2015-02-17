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

import org.everit.osgi.audit.ri.AuditRiPropertyConstants;

/**
 * Permission actions used by the implementation.
 */
public final class AuditRiPermissionConstants {

    private static final String PREFIX;

    /**
     * Permission to create audit applications. <br>
     * <br>
     * <b>TARGET_RESOURCE_ID</b>: the resourceId returned by the
     * {@link AuditRiPermissionChecker#getAuditApplicationTypeTargetResourceId()} method, it is stored as a property in
     * the {@link org.everit.osgi.props.PropertyManager} with key
     * {@link AuditRiPropertyConstants#AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID}.
     */
    public static final String INIT_AUDIT_APPLICATION;

    // TODO fix javadoc
    /**
     * Permission to log any kind of audit event type to an audit application. <br>
     * <br>
     * <b>TARGET_RESOURCE_ID</b>: the resourceId of the audit application
     */
    public static final String LOG_TO_AUDIT_APPLICATION;

    static {
        PREFIX = AuditRiPropertyConstants.class.getPackage().getName() + ".";
        INIT_AUDIT_APPLICATION = PREFIX + "INIT_AUDIT_APPLICATION";
        LOG_TO_AUDIT_APPLICATION = PREFIX + "LOG_TO_AUDIT_APPLICATION";
    }

    private AuditRiPermissionConstants() {
    }

}
