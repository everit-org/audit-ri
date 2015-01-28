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
import org.everit.osgi.audit.AuditEventTypeManager;
import org.everit.osgi.audit.LoggingService;
import org.everit.osgi.audit.dto.AuditEvent;
import org.everit.osgi.audit.dto.AuditEventType;
import org.everit.osgi.audit.ri.AuditApplicationManager;
import org.everit.osgi.audit.ri.InternalAuditEventTypeManager;
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
        @Property(name = AuditRiConstants.PROP_AUDIT_APPLICATION_NAME),
        @Property(name = AuditRiConstants.PROP_AUDIT_APPLICATION_CACHE),
        @Property(name = AuditRiConstants.PROP_AUDIT_EVENT_TYPE_CACHE)
})
@Service
public class AuditComponent implements
        AuditApplicationManager,
        AuditEventTypeManager,
        InternalAuditEventTypeManager,
        LoggingService,
        InternalLoggingService {

    private static class AuditEventTypeKey {

        private final String applicationName;

        private final String eventTypeName;

        public AuditEventTypeKey(final String applicationName, final String eventTypeName) {
            super();
            this.applicationName = applicationName;
            this.eventTypeName = eventTypeName;
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
            AuditEventTypeKey other = (AuditEventTypeKey) obj;
            if (applicationName == null) {
                if (other.applicationName != null) {
                    return false;
                }
            } else if (!applicationName.equals(other.applicationName)) {
                return false;
            }
            if (eventTypeName == null) {
                if (other.eventTypeName != null) {
                    return false;
                }
            } else if (!eventTypeName.equals(other.eventTypeName)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((applicationName == null) ? 0 : applicationName.hashCode());
            result = (prime * result) + ((eventTypeName == null) ? 0 : eventTypeName.hashCode());
            return result;
        }

    }

    @Reference(bind = "setTransactionHelper")
    private TransactionHelper transactionHelper;

    @Reference(bind = "setQuerydslSupport")
    private QuerydslSupport querydslSupport;

    @Reference(bind = "setResourceService")
    private ResourceService resourceService;

    @Reference(bind = "setAuditApplicationCache")
    private Map<String, AuditApplication> auditApplicationCache;

    @Reference(bind = "setAuditEventTypeCache")
    private Map<AuditEventTypeKey, AuditEventType> auditEventTypeCache;

    private String auditApplicationName;

    @Activate
    public void activate(final Map<String, Object> componentProperties) {
        auditApplicationName = String.valueOf(componentProperties.get(AuditRiConstants.PROP_AUDIT_APPLICATION_NAME));
        getOrCreateApplication(auditApplicationName);
    }

    @Override
    public AuditApplication createApplication(final long resourceId, final String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName cannot be null");

        return querydslSupport.execute((connection, configuration) -> {

            QApplication qApplication = QApplication.application;
            long applicationId = new SQLInsertClause(connection, configuration, qApplication)
                    .set(qApplication.resourceId, resourceId)
                    .set(qApplication.applicationName, applicationName)
                    .executeWithKey(qApplication.applicationId);

            return new AuditApplication(applicationId, applicationName, resourceId);
        });

    }

    private AuditEventType createAuditEventType(final long applicationId, final String eventTypeName) {
        return transactionHelper.required(() -> {

            return querydslSupport.execute((connection, configuration) -> {

                Long resourceId = resourceService.createResource();

                QEventType qEventType = QEventType.eventType;

                Long eventTypeId = new SQLInsertClause(connection, configuration, qEventType)
                        .set(qEventType.name, eventTypeName)
                        .set(qEventType.applicationId, applicationId)
                        .set(qEventType.resourceId, resourceId)
                        .executeWithKey(qEventType.eventTypeId);

                return new AuditEventType(eventTypeId, eventTypeName, resourceId);
            });
        });
    }

    @Override
    public AuditApplication getApplication(final String applicationName) {

        Objects.requireNonNull(applicationName, "applicationName cannot be null");

        AuditApplication cachedAuditApplication = auditApplicationCache.get(applicationName);
        if (cachedAuditApplication != null) {
            return cachedAuditApplication;
        }

        return transactionHelper.required(() -> {

            AuditApplication auditApplication = selectApplication(applicationName);
            if (auditApplication != null) {
                auditApplicationCache.put(applicationName, auditApplication);
            }

            return auditApplication;
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
                            qApplication.resourceId))
                    .getResults();
        });
    }

    @Override
    public AuditEventType getAuditEventType(final String eventTypeName) {
        return getAuditEventType(auditApplicationName, eventTypeName);
    }

    @Override
    public AuditEventType getAuditEventType(final String applicationName, final String eventTypeName) {

        Objects.requireNonNull(applicationName, "applicationName cannot be null");
        Objects.requireNonNull(eventTypeName, "eventTypeName cannot be null");

        AuditEventType cachedAuditEventType = auditEventTypeCache.get(new AuditEventTypeKey(applicationName, eventTypeName));
        if (cachedAuditEventType != null) {
            return cachedAuditEventType;
        }

        return transactionHelper.required(() -> {

            AuditEventType auditEventType = selectAuditEventType(applicationName, eventTypeName);
            if (auditEventType != null) {
                auditEventTypeCache.put(new AuditEventTypeKey(applicationName, eventTypeName), auditEventType);
            }

            return auditEventType;
        });
    }

    @Override
    public List<AuditEventType> getAuditEventTypes() {
        return getAuditEventTypes(auditApplicationName);
    }

    @Override
    public List<AuditEventType> getAuditEventTypes(final String applicationName) {
        return querydslSupport.execute((connection, configuration) -> {

            QEventType qEventType = QEventType.eventType;
            QApplication qApplication = QApplication.application;

            return new SQLQuery(connection, configuration)
                    .from(qEventType)
                    .innerJoin(qApplication).on(qEventType.applicationId.eq(qApplication.applicationId))
                    .where(qApplication.applicationName.eq(applicationName))
                    .list(ConstructorExpression.create(AuditEventType.class,
                            qEventType.eventTypeId,
                            qEventType.name,
                            qEventType.resourceId));
        });
    }

    @Override
    public AuditApplication getOrCreateApplication(final String applicationName) {
        return Optional.ofNullable(getApplication(applicationName))
                .orElseGet(() -> {
                    long resourceId = resourceService.createResource();
                    return createApplication(resourceId, applicationName);
                });
    }

    private AuditEventType getOrCreateAuditEventType(final String applicationName, final String eventTypeName) {
        return Optional
                .ofNullable(getAuditEventType(applicationName, eventTypeName))
                .orElseGet(() -> {
                    AuditApplication auditApplication = requireAppByName(applicationName);
                    return createAuditEventType(auditApplication.getApplicationId(), eventTypeName);
                });
    }

    @Override
    public List<AuditEventType> getOrCreateAuditEventTypes(final String... eventTypeNames) {
        return getOrCreateAuditEventTypes(auditApplicationName, eventTypeNames);
    }

    @Override
    public List<AuditEventType> getOrCreateAuditEventTypes(final String applicationName,
            final String... eventTypeNames) {

        Objects.requireNonNull(applicationName, "applicationName cannot be null");
        Objects.requireNonNull(eventTypeNames, "eventTypeNames cannot be null");
        if (eventTypeNames.length == 0) {
            return Collections.emptyList();
        }

        return transactionHelper.required(() -> {
            requireAppByName(applicationName);
            List<AuditEventType> rval = new ArrayList<AuditEventType>();
            for (String typeName : eventTypeNames) {
                rval.add(getOrCreateAuditEventType(applicationName, typeName));
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
            AuditEventType auditEventType = getOrCreateAuditEventType(applicationName, event.getName());
            return new AuditEventPersister(transactionHelper, querydslSupport, auditEventType.getId(), event).get();
        });
    }

    private AuditApplication requireAppByName(final String applicationName) {
        return Optional
                .ofNullable(getApplication(applicationName))
                .orElseThrow(() -> new IllegalArgumentException("application [" + applicationName + "] does not exist"));
    }

    private AuditApplication selectApplication(final String applicationName) {
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

    private AuditEventType selectAuditEventType(final String applicationName, final String eventTypeName) {
        return querydslSupport.execute((connection, configuration) -> {

            QEventType qEventType = QEventType.eventType;
            QApplication qApplication = QApplication.application;

            return new SQLQuery(connection, configuration)
                    .from(qEventType)
                    .innerJoin(qApplication).on(qEventType.applicationId.eq(qApplication.applicationId))
                    .where(qApplication.applicationName.eq(applicationName))
                    .where(qEventType.name.eq(eventTypeName))
                    .uniqueResult(ConstructorExpression.create(AuditEventType.class,
                            qEventType.eventTypeId,
                            qEventType.name,
                            qEventType.resourceId));
        });
    }

    public void setAuditApplicationCache(final Map<String, AuditApplication> auditApplicationCache) {
        this.auditApplicationCache = auditApplicationCache;
    }

    public void setAuditEventTypeCache(final Map<AuditEventTypeKey, AuditEventType> auditEventTypeCache) {
        this.auditEventTypeCache = auditEventTypeCache;
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
