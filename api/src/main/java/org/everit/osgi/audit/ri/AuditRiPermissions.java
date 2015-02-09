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

public final class AuditRiPermissions {

    private static final String PREFIX = "org.everit.osgi.audit.ri.permission.";

    /**
     * Olvasasi jog: adott application, az ala tartozo event typeok
     */
    public static final String READ_AUDIT_APPLICATION = PREFIX + "READ_AUDIT_APPLICATION";

    /**
     * Letrehozasi jog: application
     */
    public static final String CREATE_AUDIT_APPLICATION = PREFIX + "CREATE_AUDIT_APPLICATION";

    /**
     * Olvasasi jog: adott event type
     */
    public static final String READ_AUDIT_EVENT_TYPE = PREFIX + "READ_AUDIT_EVENT_TYPE";

    /**
     * Letrehozasi jog: adott application ala event type
     */
    public static final String CREATE_AUDIT_EVENT_TYPE = PREFIX + "CREATE_AUDIT_EVENT_TYPE";

    /**
     * Logozasi jog: adott application ala tetszoleges event typeal
     */
    public static final String LOG_TO_AUDIT_APPLICATION = PREFIX + "LOG_TO_AUDIT_APPLICATION";

    /**
     * Logozasi jog: adott event typeal
     */
    public static final String LOG_TO_EVENT_TYPE = PREFIX + "LOG_TO_EVENT_TYPE";

    private AuditRiPermissions() {
    }

}
