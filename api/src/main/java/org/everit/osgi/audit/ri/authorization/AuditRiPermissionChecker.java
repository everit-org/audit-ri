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
package org.everit.osgi.audit.ri.authorization;

/**
 * This interface helps to check permissions used by the Audit RI module.
 */
public interface AuditRiPermissionChecker {

  /**
   * Returns the resourceId used for authorization and permission checking in case of
   * {@link AuditRiPermissionConstants#INIT_AUDIT_APPLICATION}.
   *
   * @return the resourceId of the audit application type
   */
  long getAuditApplicationTypeTargetResourceId();

  /**
   * Checks if the authenticated resource has a permission for the
   * {@link AuditRiPermissionConstants#INIT_AUDIT_APPLICATION} permission action.
   *
   * @return <code>true</code> if the permission is allowed, <code>false</code> otherwise
   */
  boolean hasPermissionToInitAuditApplication();

  /**
   * Checks if the authenticated resource has a permission for the
   * {@link AuditRiPermissionConstants#LOG_TO_AUDIT_APPLICATION} permission action.
   *
   * @param applicationName
   *          the name of the application to check, cannot be <code>null</code>
   * @return <code>true</code> if the permission is allowed, <code>false</code> otherwise
   * @throws NullPointerException
   *           if <code>applicationName</code> is <code>null</code>
   * @throws org.everit.osgi.audit.ri.UnknownAuditApplicationException
   *           if the given application not exists
   */
  boolean hasPermissionToLogToAuditApplication(String applicationName);

}
