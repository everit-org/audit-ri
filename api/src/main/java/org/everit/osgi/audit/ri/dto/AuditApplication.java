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
package org.everit.osgi.audit.ri.dto;

import java.util.Objects;

/**
 * The audit application holds the audit events.
 */
public class AuditApplication {

  /**
   * Builder class to create the audit application easily.
   */
  public static class Builder {

    private long applicationId;

    private String applicationName;

    private long resourceId;

    /**
     * Default constructor.
     */
    public Builder() {
    }

    /**
     * Deep copy constructor.
     *
     * @param auditApplication
     *          the original audit application to copy deeply, cannot be <code>null</code>.
     */
    public Builder(final AuditApplication auditApplication) {
      Objects.requireNonNull(auditApplication, "auditApplication cannot be null");
      applicationId = auditApplication.applicationId;
      applicationName = auditApplication.applicationName;
      resourceId = auditApplication.resourceId;
    }

    public Builder applicationId(final long applicationId) {
      this.applicationId = applicationId;
      return this;
    }

    public Builder applicationName(final String applicationName) {
      this.applicationName = applicationName;
      return this;
    }

    public AuditApplication build() {
      return new AuditApplication(this);
    }

    public Builder resourceId(final long resourceId) {
      this.resourceId = resourceId;
      return this;
    }
  }

  /**
   * The identifier of the audit application.
   */
  public long applicationId;

  /**
   * The name of the audit application.
   */
  public String applicationName;

  /**
   * The resourceId belonging to the application.
   */
  public long resourceId;

  /**
   * Default constructor.
   */
  public AuditApplication() {
  }

  /**
   * Deep copy constructor.
   *
   * @param original
   *          the original instance to copy deeply, cannot be <code>null</code>.
   */
  public AuditApplication(final AuditApplication original) {
    Objects.requireNonNull(original, "original cannot be null");
    applicationId = original.applicationId;
    applicationName = original.applicationName;
    resourceId = original.resourceId;
  }

  private AuditApplication(final Builder builder) {
    applicationId = builder.applicationId;
    applicationName = Objects.requireNonNull(builder.applicationName,
        "applicationName cannot be null");
    resourceId = builder.resourceId;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AuditApplication other = (AuditApplication) obj;
    if (applicationId != other.applicationId) {
      return false;
    }
    if (applicationName == null) {
      if (other.applicationName != null) {
        return false;
      }
    } else if (!applicationName.equals(other.applicationName)) {
      return false;
    }
    if (resourceId != other.resourceId) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (int) (applicationId ^ (applicationId >>> 32));
    result = (prime * result) + ((applicationName == null) ? 0 : applicationName.hashCode());
    result = (prime * result) + (int) (resourceId ^ (resourceId >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "AuditApplication [applicationId=" + applicationId + ", applicationName="
        + applicationName
        + ", resourceId=" + resourceId + "]";
  }

}
