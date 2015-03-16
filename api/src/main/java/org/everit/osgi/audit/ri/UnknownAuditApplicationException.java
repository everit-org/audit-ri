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
 * Signs that a method was invoked with an application that does not exist.
 */
public class UnknownAuditApplicationException extends RuntimeException {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 6784626196905796600L;

  /**
   * The name of the application that does not exist.
   */
  private final String applicationName;

  /**
   * Constructor.
   *
   * @param applicationName
   *          the name of the application that does not exist
   */
  public UnknownAuditApplicationException(final String applicationName) {
    super("audit application [" + applicationName + "] does not exist");
    this.applicationName = applicationName;
  }

  public String getApplicationName() {
    return applicationName;
  }

}
