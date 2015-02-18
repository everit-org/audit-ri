/**
 * This file is part of org.everit.osgi.audit.ri.
 *
 * org.everit.osgi.audit.ri is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * org.everit.osgi.audit.ri is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with org.everit.osgi.audit.ri.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.audit.ri.internal;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.audit.AuditEventTypeManager;
import org.everit.osgi.audit.LoggingService;
import org.everit.osgi.audit.dto.AuditEvent;
import org.everit.osgi.audit.ri.AuditApplicationManager;
import org.everit.osgi.audit.ri.AuditRiComponentConstants;
import org.everit.osgi.audit.ri.InternalAuditEventTypeManager;
import org.everit.osgi.audit.ri.InternalLoggingService;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authorization.PermissionChecker;
import org.osgi.framework.Constants;

@Component(name = AuditRiComponentConstants.EMBEDDED_SERVICE_FACTORY_PID, metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = Constants.SERVICE_DESCRIPTION, propertyPrivate = false,
                value = AuditRiComponentConstants.EMBEDDED_DEFAULT_SERVICE_DESCRIPTION),
        @Property(name = AuditRiComponentConstants.PROP_EMBEDDED_AUDIT_APPLICATION_NAME),
        @Property(name = AuditRiComponentConstants.PROP_AUDIT_APPLICATION_MANAGER),
        @Property(name = AuditRiComponentConstants.PROP_INTERNAL_AUDIT_EVENT_TYPE_MANAGER),
        @Property(name = AuditRiComponentConstants.PROP_INTERNAL_LOGGING_SERVICE),
        @Property(name = AuditRiComponentConstants.PROP_AUTHENTICATION_PROPAGATOR),
        @Property(name = AuditRiComponentConstants.PROP_PERMISSION_CHECKER)
})
@Service
public class EmbeddedAuditCompnent implements
        AuditEventTypeManager,
        LoggingService {

    @Reference(bind = "setAuditApplicationManager")
    private AuditApplicationManager auditApplicationManager;

    @Reference(bind = "setInternalAuditEventTypeManager")
    private InternalAuditEventTypeManager internalAuditEventTypeManager;

    @Reference(bind = "setInternalLoggingService")
    private InternalLoggingService internalLoggingService;

    @Reference(bind = "setAuthenticationPropagator")
    private AuthenticationPropagator authenticationPropagator;

    @Reference(bind = "setPermissionChecker")
    private PermissionChecker permissionChecker;

    private String embeddedAuditApplicationName;

    @Activate
    public void activate(final Map<String, Object> componentProperties) {

        embeddedAuditApplicationName = String.valueOf(
                componentProperties.get(AuditRiComponentConstants.PROP_EMBEDDED_AUDIT_APPLICATION_NAME));

        long systemResourceId = permissionChecker.getSystemResourceId();

        authenticationPropagator.runAs(systemResourceId, () -> {
            auditApplicationManager.initAuditApplication(embeddedAuditApplicationName);
            return null;
        });

    }

    @Override
    public void initAuditEventTypes(final String... eventTypeNames) {

        long systemResourceId = permissionChecker.getSystemResourceId();

        authenticationPropagator.runAs(systemResourceId, () -> {
            internalAuditEventTypeManager.initAuditEventTypes(embeddedAuditApplicationName, eventTypeNames);
            return null;
        });
    }

    @Override
    public void logEvent(final AuditEvent auditEvent) {

        long systemResourceId = permissionChecker.getSystemResourceId();

        authenticationPropagator.runAs(systemResourceId, () -> {
            internalLoggingService.logEvent(embeddedAuditApplicationName, auditEvent);
            return null;
        });
    }

    public void setAuditApplicationManager(final AuditApplicationManager auditApplicationManager) {
        this.auditApplicationManager = auditApplicationManager;
    }

    public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
        this.authenticationPropagator = authenticationPropagator;
    }

    public void setInternalAuditEventTypeManager(final InternalAuditEventTypeManager internalAuditEventTypeManager) {
        this.internalAuditEventTypeManager = internalAuditEventTypeManager;
    }

    public void setInternalLoggingService(final InternalLoggingService internalLoggingService) {
        this.internalLoggingService = internalLoggingService;
    }

    public void setPermissionChecker(final PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

}
