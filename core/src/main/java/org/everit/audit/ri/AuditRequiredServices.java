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

import org.everit.authnr.permissionchecker.AuthnrPermissionChecker;
import org.everit.authorization.AuthorizationManager;
import org.everit.persistence.querydsl.support.QuerydslSupport;
import org.everit.props.PropertyManager;
import org.everit.resource.ResourceService;
import org.everit.transaction.propagator.TransactionPropagator;

/**
 * Parameter object to hold the required services for the audit implementation.
 */
public class AuditRequiredServices {

  public final AuthnrPermissionChecker authnrPermissionChecker;

  public final AuthorizationManager authorizationManager;

  public final PropertyManager propertyManager;

  public final QuerydslSupport querydslSupport;

  public final ResourceService resourceService;

  public final TransactionPropagator transactionPropagator;

  /**
   * Constructor.
   */
  public AuditRequiredServices(final AuthnrPermissionChecker authnrPermissionChecker,
      final AuthorizationManager authorizationManager, final PropertyManager propertyManager,
      final ResourceService resourceService, final QuerydslSupport querydslSupport,
      final TransactionPropagator transactionPropagator) {
    this.authnrPermissionChecker = authnrPermissionChecker;
    this.authorizationManager = authorizationManager;
    this.propertyManager = propertyManager;
    this.resourceService = resourceService;
    this.querydslSupport = querydslSupport;
    this.transactionPropagator = transactionPropagator;
  }
}
