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
import org.everit.osgi.audit.ri.AuditRiComponentProps;
import org.everit.osgi.audit.ri.InternalAuditEventTypeManager;
import org.everit.osgi.audit.ri.InternalLoggingService;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authorization.PermissionChecker;

@Component(name = AuditRiComponentProps.EMBEDDED_SERVICE_FACTORY_PID, metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = AuditRiComponentProps.PROP_EMBEDDED_AUDIT_APPLICATION_NAME),
        @Property(name = "auditApplicationManager.target"),
        @Property(name = "internalAuditEventTypeManager.target"),
        @Property(name = "internalLoggingService.target"),
        @Property(name = "authenticationPropagator.target"),
        @Property(name = "permissionChecker.target")
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
                componentProperties.get(AuditRiComponentProps.PROP_EMBEDDED_AUDIT_APPLICATION_NAME));

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
