/**
 * This file is part of Everit - Audit Reference Implementation Tests.
 *
 * Everit - Audit Reference Implementation Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Audit Reference Implementation Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Audit Reference Implementation Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.audit.ri.tests;

import java.util.Objects;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.audit.api.AuditService;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.junit.Test;

@Component(name = "AuditComponentTest",
        immediate = true,
        metatype = true,
        configurationFactory = true,
policy = ConfigurationPolicy.REQUIRE)
@Service(AuditComponentTest.class)
@Properties({
    @Property(name = "eosgi.testEngine", value = "junit4"),
    @Property(name = "eosgi.testId", value = "auditTest"),
    @Property(name = "auditComponent.target")
})
@TestDuringDevelopment
public class AuditComponentTest {

    // @Reference
    // private DataSource dataSource;

    @Reference
    private AuditService auditComponent;

    public void bindAuditComponent(final AuditService auditComponent) {
        this.auditComponent = Objects.requireNonNull(auditComponent, "subject cannot be null");
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void createApplicationFail() {
        auditComponent.createApplication(null);
    }

    @Test
    @TestDuringDevelopment
    public void createApplicationSuccess() {
        auditComponent.createApplication("appname");
    }

}
