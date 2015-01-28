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

    public long getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public long getResourceId() {
        return resourceId;
    }

}
