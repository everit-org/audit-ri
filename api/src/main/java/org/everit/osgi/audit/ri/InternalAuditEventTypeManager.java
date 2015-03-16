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
package org.everit.osgi.audit.ri;

/**
 * Interface for managing {@link org.everit.osgi.audit.dto.AuditEventType}s belonging to audit
 * applications.
 */
public interface InternalAuditEventTypeManager {

  /**
   * Creates the {@link org.everit.osgi.audit.dto.AuditEventType}s by their names. If an event type
   * already exists, it will be skipped.
   *
   * @param applicationName
   *          the event types will belong to this audit application
   * @param eventTypeNames
   *          the names of the audit events to create
   * @throws NullPointerException
   *           if the <code>applicationName</code>, a <code>null</code> array or an array with
   *           <code>null</code> element is provided
   * @throws UnknownAuditApplicationException
   *           if the given application not exists
   */
  void initAuditEventTypes(String applicationName, String... eventTypeNames);

}
