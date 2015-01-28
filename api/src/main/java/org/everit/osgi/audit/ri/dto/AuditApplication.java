/**
 * This file is part of Everit - Audit RI API.
 *
 * Everit - Audit RI API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Audit RI API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Audit RI API.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.audit.ri.dto;

import java.util.Objects;

public class AuditApplication {

    private final long applicationId;

    private final String applicationName;

    private final long resourceId;

    public AuditApplication(final long applicationId, final String applicationName, final long resourceId) {
        this.applicationId = applicationId;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName cannot be null");
        this.resourceId = resourceId;
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

    public long getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public long getResourceId() {
        return resourceId;
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
        return "AuditApplication [applicationId=" + applicationId + ", applicationName=" + applicationName
                + ", resourceId=" + resourceId + "]";
    }

}
