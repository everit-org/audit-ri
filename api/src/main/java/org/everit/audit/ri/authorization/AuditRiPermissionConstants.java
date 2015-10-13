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
 * Permission actions used by the implementation.
 */
public final class AuditRiPermissionConstants {

  /**
   * Permission to initialize audit applications. <br>
   * <br>
   * <b>TARGET_RESOURCE_ID</b>: the resourceId returned by the
   * {@link AuditRiPermissionChecker#getAuditApplicationTypeTargetResourceId()} method, it is stored
   * as a property in the a <code>PropertyManager</code> with key
   * {@link org.everit.audit.ri.props.AuditRiPropertyConstants#AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID}
   * .
   */
  public static final String INIT_AUDIT_APPLICATION;

  /**
   * Permission to log and initialize any kind of audit event type to an audit application. <br>
   * <br>
   * <b>TARGET_RESOURCE_ID</b>: the resourceId of the audit application
   */
  public static final String LOG_TO_AUDIT_APPLICATION;

  static {
    String prefix = AuditRiPermissionConstants.class.getPackage().getName() + ".";
    INIT_AUDIT_APPLICATION = prefix + "INIT_AUDIT_APPLICATION";
    LOG_TO_AUDIT_APPLICATION = prefix + "LOG_TO_AUDIT_APPLICATION";
  }

  private AuditRiPermissionConstants() {
  }

}
