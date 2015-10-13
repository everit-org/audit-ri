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
package org.everit.audit.ri.props;

/**
 * Property keys used by the Audit RI.
 */
public final class AuditRiPropertyConstants {

  /**
   * Property key to identify the target resourceId used for the
   * {@link org.everit.audit.ri.authorization.AuditRiPermissionConstants#INIT_AUDIT_APPLICATION}
   * permission action.
   */
  public static final String AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID;

  static {
    String prefix = AuditRiPropertyConstants.class.getPackage().getName() + ".";
    AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID = prefix
        + "AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID";
  }

  private AuditRiPropertyConstants() {
  }

}
