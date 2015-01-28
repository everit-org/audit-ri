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
package org.everit.osgi.audit.ri.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.audit.EventTypeManager;
import org.everit.osgi.audit.LoggingService;
import org.everit.osgi.audit.dto.AuditEvent;
import org.everit.osgi.audit.dto.EventType;
import org.everit.osgi.audit.ri.AuditApplicationManager;
import org.everit.osgi.audit.ri.InternalEventTypeManager;
import org.everit.osgi.audit.ri.InternalLoggingService;
import org.everit.osgi.audit.ri.conf.AuditRiConstants;
import org.everit.osgi.audit.ri.dto.AuditApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.resource.ResourceService;
import org.everit.osgi.transaction.helper.api.TransactionHelper;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.types.ConstructorExpression;

@Component(name = AuditRiConstants.SERVICE_FACTORY_PID, metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = AuditRiConstants.PROP_TRASACTION_HELPER),
        @Property(name = AuditRiConstants.PROP_QUERYDSL_SUPPORT),
        @Property(name = AuditRiConstants.PROP_RESOURCE_SERVICE),
        @Property(name = AuditRiConstants.PROP_AUDIT_APPLICATION_NAME)
})
@Service
public class AuditComponent implements
        AuditApplicationManager,
        EventTypeManager,
        InternalEventTypeManager,
        LoggingService,
        InternalLoggingService {

    @Reference(bind = "setTransactionHelper")
    private TransactionHelper transactionHelper;

    @Reference(bind = "setQuerydslSupport")
    private QuerydslSupport querydslSupport;

    @Reference(bind = "setResourceService")
    private ResourceService resourceService;

    private String auditApplicationName;

    @Activate
    public void activate(final Map<String, Object> componentProperties) {
        auditApplicationName = String.valueOf(componentProperties.get(AuditRiConstants.PROP_AUDIT_APPLICATION_NAME));
        getOrCreateApplication(auditApplicationName);
    }

    @Override
    public AuditApplication createApplication(final long resourceId, final String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName cannot be null");

        return transactionHelper.required(() -> {

            return querydslSupport.execute((connection, configuration) -> {

                QApplication qApplication = QApplication.application;
                long applicationId = new SQLInsertClause(connection, configuration, qApplication)
                        .set(qApplication.resourceId, resourceId)
                        .set(qApplication.applicationName, applicationName)
                        .executeWithKey(qApplication.applicationId);

                return new AuditApplication(applicationId, applicationName, resourceId);
            });
        });
    }

    private EventType createEventType(final long applicationId, final String eventTypeName) {
        return transactionHelper.required(() -> {

            return querydslSupport.execute((connection, configuration) -> {

                Long resourceId = resourceService.createResource();

                QEventType qEventType = QEventType.eventType;

                Long eventTypeId = new SQLInsertClause(connection, configuration, qEventType)
                        .set(qEventType.name, eventTypeName)
                        .set(qEventType.applicationId, applicationId)
                        .set(qEventType.resourceId, resourceId)
                        .executeWithKey(qEventType.eventTypeId);

                return new EventType(eventTypeId, eventTypeName, resourceId);
            });
        });
    }

    @Override
    public AuditApplication getApplicationByName(final String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName cannot be null");

        return querydslSupport.execute((connection, configuration) -> {

            QApplication qApplication = QApplication.application;

            return new SQLQuery(connection, configuration)
                    .from(qApplication)
                    .where(qApplication.applicationName.eq(applicationName))
                    .uniqueResult(ConstructorExpression.create(AuditApplication.class,
                            qApplication.applicationId,
                            qApplication.applicationName,
                            qApplication.resourceId));
        });
    }

    @Override
    public List<AuditApplication> getApplications() {
        return querydslSupport.execute((connection, configuration) -> {
            QApplication qApplication = QApplication.application;
            return new SQLQuery(connection, configuration)
                    .from(qApplication)
                    .listResults(ConstructorExpression.create(AuditApplication.class,
                            qApplication.applicationId,
                            qApplication.applicationName,
                            qApplication.resourceId)).getResults();
        });
    }

    @Override
    public EventType getEventTypeByName(final String eventTypeName) {
        return getEventTypeByNameForApplication(auditApplicationName, eventTypeName);
    }

    @Override
    public EventType getEventTypeByNameForApplication(final String applicationName, final String eventTypeName) {

        Objects.requireNonNull(applicationName, "applicationName cannot be null");
        Objects.requireNonNull(eventTypeName, "eventTypeName cannot be null");

        return querydslSupport.execute((connection, configuration) -> {

            QEventType qEventType = QEventType.eventType;
            QApplication qApplication = QApplication.application;

            return new SQLQuery(connection, configuration)
                    .from(qEventType)
                    .innerJoin(qApplication).on(qEventType.applicationId.eq(qApplication.applicationId))
                    .where(qApplication.applicationName.eq(applicationName))
                    .where(qEventType.name.eq(eventTypeName))
                    .uniqueResult(ConstructorExpression.create(EventType.class,
                            qEventType.eventTypeId,
                            qEventType.name,
                            qEventType.resourceId));
        });
    }

    @Override
    public List<EventType> getEventTypes() {
        return getEventTypesByApplication(auditApplicationName);
    }

    @Override
    public List<EventType> getEventTypesByApplication(final String applicationName) {
        return querydslSupport.execute((connection, configuration) -> {

            QEventType qEventType = QEventType.eventType;
            QApplication qApplication = QApplication.application;

            return new SQLQuery(connection, configuration)
                    .from(qEventType)
                    .innerJoin(qApplication).on(qEventType.applicationId.eq(qApplication.applicationId))
                    .where(qApplication.applicationName.eq(applicationName))
                    .list(ConstructorExpression.create(EventType.class,
                            qEventType.eventTypeId,
                            qEventType.name,
                            qEventType.resourceId));
        });
    }

    @Override
    public AuditApplication getOrCreateApplication(final String applicationName) {
        return Optional.ofNullable(getApplicationByName(applicationName))
                .orElseGet(() -> {
                    long resourceId = resourceService.createResource();
                    return createApplication(resourceId, applicationName);
                });
    }

    private EventType getOrCreateEventType(final String applicationName, final String eventTypeName) {
        return Optional
                .ofNullable(getEventTypeByNameForApplication(applicationName, eventTypeName))
                .orElseGet(() -> {
                    AuditApplication auditApplication = requireAppByName(applicationName);
                    return createEventType(auditApplication.getApplicationId(), eventTypeName);
                });
    }

    @Override
    public List<EventType> getOrCreateEventTypes(final String... eventTypeNames) {
        return getOrCreateEventTypesForApplication(auditApplicationName, eventTypeNames);
    }

    @Override
    public List<EventType> getOrCreateEventTypesForApplication(final String applicationName,
            final String... eventTypeNames) {

        Objects.requireNonNull(applicationName, "applicationName cannot be null");
        Objects.requireNonNull(eventTypeNames, "eventTypeNames cannot be null");
        if (eventTypeNames.length == 0) {
            return Collections.emptyList();
        }

        return transactionHelper.required(() -> {
            requireAppByName(applicationName);
            List<EventType> rval = new ArrayList<EventType>();
            for (String typeName : eventTypeNames) {
                rval.add(getOrCreateEventType(applicationName, typeName));
            }
            return rval;
        });
    }

    @Override
    public void logEvent(final AuditEvent auditEvent) {
        logEvent(auditApplicationName, auditEvent);
    }

    @Override
    public void logEvent(final String applicationName, final AuditEvent event) {
        transactionHelper.required(() -> {
            EventType eventType = getOrCreateEventType(applicationName, event.getName());
            return new EventPersister(transactionHelper, querydslSupport, eventType.getId(), event).get();
        });
    }

    private AuditApplication requireAppByName(final String applicationName) {
        return Optional
                .ofNullable(getApplicationByName(applicationName))
                .orElseThrow(() -> new IllegalArgumentException("application [" + applicationName + "] does not exist"));
    }

    public void setQuerydslSupport(final QuerydslSupport querydslSupport) {
        this.querydslSupport = querydslSupport;
    }

    public void setResourceService(final ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public void setTransactionHelper(final TransactionHelper transactionHelper) {
        this.transactionHelper = transactionHelper;
    }

}
