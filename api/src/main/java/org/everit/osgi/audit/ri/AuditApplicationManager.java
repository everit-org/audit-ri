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
 * Interface to manage audit applications.
 */
public interface AuditApplicationManager {

  /**
   * Creates an application and assigns it to a new resource automatically. If the application
   * already exists, it will be ignored.
   *
   * @param applicationName
   *          the name of the application to create, cannot be <code>null</code>
   * @throws NullPointerException
   *           if {@code applicationName} is <code>null</code>
   */
  void initAuditApplication(String applicationName);

}
