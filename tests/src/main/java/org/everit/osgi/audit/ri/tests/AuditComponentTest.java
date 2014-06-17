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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.audit.api.AuditService;
import org.everit.osgi.audit.api.dto.Application;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mysema.query.QueryException;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.types.ConstructorExpression;

@Component(name = "AuditComponentTest",
        immediate = true,
        metatype = true,
        configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Service(AuditComponentTest.class)
@Properties({
        @Property(name = "eosgi.testEngine", value = "junit4"),
        @Property(name = "eosgi.testId", value = "auditTest"),
        @Property(name = "auditComponent.target"),
        @Property(name = "dataSource.target"),
        @Property(name = "sqlTemplates.target")
})
@TestDuringDevelopment
public class AuditComponentTest {

    @Reference
    private DataSource dataSource;

    @Reference
    private SQLTemplates sqlTemplates;

    @Reference
    private AuditService auditComponent;

    private Connection conn;

    public void bindAuditComponent(final AuditService auditComponent) {
        this.auditComponent = Objects.requireNonNull(auditComponent, "subject cannot be null");
    }

    public void bindDataSource(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource cannot be null");
    }

    public void bindSqlTemplates(final SQLTemplates sqlTemplates) {
        this.sqlTemplates = Objects.requireNonNull(sqlTemplates, "sqlTemplates cannot be null");
    }

    @After
    public void cleanupDatabase() {
        try {
            new SQLDeleteClause(conn, sqlTemplates, QApplication.auditApplication).execute();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = QueryException.class)
    @TestDuringDevelopment
    public void createApplicationConstraintViolation() {
        auditComponent.createApplication("appname");
        auditComponent.createApplication("appname");
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
        QApplication app = QApplication.auditApplication;
        Application result = new SQLQuery(conn, sqlTemplates)
                .from(app)
                .where(app.applicationName.eq("appname"))
                .uniqueResult(ConstructorExpression.create(Application.class,
                        app.applicationId,
                        app.applicationName,
                        app.resourceId));
        Assert.assertEquals("appname", result.getAppName());
        Assert.assertNotNull(result.getResourceId());
    }

    @Test
    @TestDuringDevelopment
    public void findAppByNameSuccess() {
        auditComponent.createApplication("appname");
        Application actual = auditComponent.findAppByName("appname");
        Assert.assertNotNull(actual);
    }

    @Before
    public void openConnection() {
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
