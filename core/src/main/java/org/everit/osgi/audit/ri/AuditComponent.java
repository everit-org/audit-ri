/**
 * This file is part of org.everit.osgi.audit.ri.
 *
 * org.everit.osgi.audit.ri is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * org.everit.osgi.audit.ri is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with org.everit.osgi.audit.ri.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.audit.ri;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
import org.everit.osgi.audit.api.dto.EventType;
import org.everit.osgi.audit.api.dto.EventUi;
import org.everit.osgi.audit.api.dto.FieldWithType;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.resource.api.ResourceService;
import org.everit.osgi.transaction.helper.api.Callback;
import org.everit.osgi.transaction.helper.api.TransactionHelper;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.types.ConstructorExpression;

@Component(name = "AuditComponent",
        immediate = true,
        metatype = true,
        configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = "sqlTemplates.target"),
        @Property(name = "dataSource.target"),
        @Property(name = "resourceService.target")
})
@Service
public class AuditComponent implements AuditService {

    @Reference
    private SQLTemplates sqlTemplates;

    @Reference
    private DataSource dataSource;

    @Reference
    private ResourceService resourceService;

    @Reference
    private TransactionHelper transactionHelper;

    public void bindDataSource(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource cannot be null");
    }

    public void bindResourceService(final ResourceService resourceService) {
        this.resourceService = Objects.requireNonNull(resourceService, "resourceService cannot be null");
    }

    public void bindSqlTemplates(final SQLTemplates sqlTemplates) {
        this.sqlTemplates = Objects.requireNonNull(sqlTemplates, "sqlTemplates cannot be null");
    }

    public void bindTransactionHelper(final TransactionHelper transactionHelper) {
        this.transactionHelper = Objects.requireNonNull(transactionHelper, "transactionHelper cannot be null");
    }

    @Override
    public void createApplication(final String appName) {
        Objects.requireNonNull(appName, "appName cannot be null");
        long resourceId = resourceService.createResource();
        createApplication(appName, resourceId);
    }

    @Override
    public void createApplication(final String appName, final Long resourceId) {
        Objects.requireNonNull(appName, "appName cannot be null");
        Objects.requireNonNull(resourceId, "resourceId cannot be null");
        transactionHelper.required(new Callback<Void>() {

            @Override
            public Void execute() {
                try (Connection conn = dataSource.getConnection()) {
                    QApplication app = QApplication.auditApplication;
                    new SQLInsertClause(conn, sqlTemplates, app)
                    .set(app.resourceId, resourceId)
                    .set(app.applicationName, appName)
                    .execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }

    @Override
    public Application findAppByName(final String appName) {
        try (Connection conn = dataSource.getConnection()) {
            QApplication app = QApplication.auditApplication;
            return new SQLQuery(conn, sqlTemplates)
                    .from(app)
                    .where(app.applicationName.eq(appName))
                    .uniqueResult(ConstructorExpression.create(Application.class,
                    app.applicationId,
                    app.applicationName,
                    app.resourceId));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Application> getApplications() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventUi getEventById(final long eventId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<EventType> getEventTypeByNameForApplication(final Long selectedAppId, final String eventName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<EventType> getEventTypesByApplication(final Long selectedAppId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Application getOrCreateApplication(final String applicationName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventType getOrCreateEventType(final String applicationName, final String eventTypeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventType[] getOrCreateEventTypes(final String applicationName, final String[] eventTypeNames) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FieldWithType> getResultFieldsWithTypes(final Long[] selectedAppId, final Long[] selectedEventTypeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventUi readEvent(final long eventId, final List<String> dataFields) {
        // TODO Auto-generated method stub
        return null;
    }
}
