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
package org.everit.audit.ri.authorization;

/**
 * This interface helps to manage permissions used by the Audit RI module.
 */
public interface AuditRiAuthorizationManager {

  /**
   * Adds the {@link AuditRiPermissionConstants#INIT_AUDIT_APPLICATION} permission action to the
   * given authorized resourceId.
   *
   * @param authorizedResourceId
   *          the permission will be added to this resourceId
   */
  void addPermissionToInitAuditApplication(long authorizedResourceId);

  /**
   * Adds the {@link AuditRiPermissionConstants#LOG_TO_AUDIT_APPLICATION} permission action to the
   * given authorized resourceId.
   *
   * @param authorizedResourceId
   *          the permission will be added to this resourceId
   * @param applicationName
   *          the authorized resourceId can log events and initialize event types under this audit
   *          application, cannot be <code>null</code>
   * @throws NullPointerException
   *           if the <code>applicationName</code> is <code>null</code>
   * @throws org.everit.audit.ri.UnknownAuditApplicationException
   *           if the given application not exists
   */
  void addPermissionToLogToAuditApplication(long authorizedResourceId, String applicationName);

  /**
   * Removes the {@link AuditRiPermissionConstants#INIT_AUDIT_APPLICATION} permission action from
   * the given authorized resourceId.
   *
   * @param authorizedResourceId
   *          the permission will be removed from this resourceId
   */
  void removePermissionInitAuditApplication(long authorizedResourceId);

  /**
   * Removes the {@link AuditRiPermissionConstants#LOG_TO_AUDIT_APPLICATION} permission action from
   * the given authorized resourceId.
   *
   * @param authorizedResourceId
   *          the permission will be removed from this resourceId
   * @param applicationName
   *          the authorized resourceId cannot log events or initialize event types under this audit
   *          application any more, cannot be <code>null</code>
   * @throws NullPointerException
   *           if the <code>applicationName</code> is <code>null</code>
   * @throws org.everit.audit.ri.UnknownAuditApplicationException
   *           if the given application not exists
   */
  void removePermissionLogToAuditApplication(long authorizedResourceId, String applicationName);

}
