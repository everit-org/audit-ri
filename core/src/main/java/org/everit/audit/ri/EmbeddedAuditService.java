/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
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
package org.everit.audit.ri;

import org.everit.audit.AuditEventTypeManager;
import org.everit.audit.LoggingService;
import org.everit.audit.dto.AuditEvent;
import org.everit.authentication.context.AuthenticationPropagator;
import org.everit.authorization.PermissionChecker;

/**
 * The embedded implementation of the {@link AuditEventTypeManager} and the {@link LoggingService}.
 */
public class EmbeddedAuditService implements
    AuditEventTypeManager,
    LoggingService {

  private final AuthenticationPropagator authenticationPropagator;

  private final String embeddedAuditApplicationName;

  private final InternalAuditEventTypeManager internalAuditEventTypeManager;

  private final InternalLoggingService internalLoggingService;

  private final PermissionChecker permissionChecker;

  /**
   * Constructor.
   */
  public EmbeddedAuditService(final AuditApplicationManager auditApplicationManager,
      final InternalAuditEventTypeManager internalAuditEventTypeManager,
      final InternalLoggingService internalLoggingService,
      final AuthenticationPropagator authenticationPropagator,
      final PermissionChecker permissionChecker,
      final String embeddedAuditApplicationName) {
    super();
    this.internalAuditEventTypeManager = internalAuditEventTypeManager;
    this.internalLoggingService = internalLoggingService;
    this.authenticationPropagator = authenticationPropagator;
    this.permissionChecker = permissionChecker;
    this.embeddedAuditApplicationName = embeddedAuditApplicationName;

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

}
