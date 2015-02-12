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
 * Audit RI Component configuration properties.
 */
public final class AuditRiScr {

    public static final String SERVICE_FACTORY_PID = "org.everit.osgi.audit.ri.AuditComponent";

    public static final String PROP_TRASACTION_HELPER = "transactionHelper.target";

    public static final String PROP_QUERYDSL_SUPPORT = "querydslSupport.target";

    public static final String PROP_RESOURCE_SERVICE = "resourceService.target";

    public static final String PROP_AUDIT_APPLICATION_NAME = "auditApplicationName";

    public static final String PROP_AUDIT_APPLICATION_CACHE = "auditApplicationCache.target";

    public static final String PROP_AUDIT_EVENT_TYPE_CACHE = "auditEventTypeCache.target";

    public static final String PROP_AUTHNR_PERMISSION_CHECKER = "authnrPermissionChecker.target";

    public static final String PROP_AUTHNR_QDSL_UTIL = "authnrQdslUtil.target";

    public static final String PROP_AUTHENTICATION_PROPAGATOR = "authenticationPropagator.target";

    public static final String PROP_PROPERTY_MANAGER = "propertyManager.target";

    private AuditRiScr() {
    }

}
