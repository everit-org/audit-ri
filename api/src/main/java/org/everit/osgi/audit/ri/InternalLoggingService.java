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

import org.everit.osgi.audit.dto.AuditEvent;

/**
 * Interface for logging {@link AuditEvent}s to a specific audit application.
 */
public interface InternalLoggingService {

  /**
   * Persists the given audit event to the event store. For e.g. to database, file, etc.
   *
   * @param applicationName
   *          the audit event will be logged under this audit application, cannot be
   *          <code>null</code>
   * @param auditEvent
   *          the event to persist, cannot be <code>null</code>
   * @throws NullPointerException
   *           if the <code>applicationName</code> or <code>auditEvent</code> parameter is
   *           <code>null</code>
   * @throws UnknownAuditApplicationException
   *           if the given application not exists
   */
  void logEvent(String applicationName, AuditEvent auditEvent);

}
