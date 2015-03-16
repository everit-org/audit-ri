/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/**
 * The embedded implementation of the {@link AuditEventTypeManager} and the {@link LoggingService}.
 */
@Component(name = AuditRiComponentConstants.EMBEDDED_SERVICE_FACTORY_PID, metatype = true,
    configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
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

  /**
   * Initializes the embedded audit application configured and activates the OSGi component.
   *
   * @param componentProperties
   *          The properties of the OSGi component.
   */
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
      internalAuditEventTypeManager.initAuditEventTypes(embeddedAuditApplicationName,
          eventTypeNames);
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

  public void setInternalAuditEventTypeManager(
      final InternalAuditEventTypeManager internalAuditEventTypeManager) {
    this.internalAuditEventTypeManager = internalAuditEventTypeManager;
  }

  public void setInternalLoggingService(final InternalLoggingService internalLoggingService) {
    this.internalLoggingService = internalLoggingService;
  }

  public void setPermissionChecker(final PermissionChecker permissionChecker) {
    this.permissionChecker = permissionChecker;
  }

}
