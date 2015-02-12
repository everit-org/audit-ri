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

import java.sql.Timestamp;
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
import org.everit.osgi.audit.dto.EventData;
import org.everit.osgi.audit.ri.AuditApplicationManager;
import org.everit.osgi.audit.ri.AuditRiPermissions;
import org.everit.osgi.audit.ri.AuditRiProps;
import org.everit.osgi.audit.ri.AuditRiScr;
import org.everit.osgi.audit.ri.InternalAuditEventTypeManager;
import org.everit.osgi.audit.ri.InternalLoggingService;
import org.everit.osgi.audit.ri.dto.AuditApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authnr.permissionchecker.AuthnrPermissionChecker;
import org.everit.osgi.authnr.qdsl.util.AuthnrQdslUtil;
import org.everit.osgi.props.PropertyManager;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.resource.ResourceService;
import org.everit.osgi.transaction.helper.api.TransactionHelper;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.types.Projections;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.template.BooleanTemplate;

@Component(name = AuditRiScr.SERVICE_FACTORY_PID, metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = AuditRiScr.PROP_TRASACTION_HELPER),
        @Property(name = AuditRiScr.PROP_QUERYDSL_SUPPORT),
        @Property(name = AuditRiScr.PROP_RESOURCE_SERVICE),
        @Property(name = AuditRiScr.PROP_AUDIT_APPLICATION_NAME),
        @Property(name = AuditRiScr.PROP_AUDIT_APPLICATION_CACHE),
        @Property(name = AuditRiScr.PROP_AUDIT_EVENT_TYPE_CACHE),
        @Property(name = AuditRiScr.PROP_AUTHNR_PERMISSION_CHECKER),
        @Property(name = AuditRiScr.PROP_AUTHNR_QDSL_UTIL),
        @Property(name = AuditRiScr.PROP_AUTHENTICATION_PROPAGATOR),
        @Property(name = AuditRiScr.PROP_PROPERTY_MANAGER)
})
@Service
public class AuditComponent implements
        AuditApplicationManager,
        AuditEventTypeManager,
        InternalAuditEventTypeManager,
        LoggingService,
        InternalLoggingService {

    private static class CachedEventTypeKey {

        private final long applicationId;

        private final String eventTypeName;

        public CachedEventTypeKey(final long applicationId, final String eventTypeName) {
            super();
            this.applicationId = applicationId;
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
            CachedEventTypeKey other = (CachedEventTypeKey) obj;
            if (applicationId != other.applicationId) {
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
            result = (prime * result) + (int) (applicationId ^ (applicationId >>> 32));
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
    private Map<CachedEventTypeKey, AuditEventType> auditEventTypeCache;

    @Reference(bind = "setAuthnrPermissionChecker")
    private AuthnrPermissionChecker authnrPermissionChecker;

    @Reference(bind = "setAuthnrQdslUtil")
    private AuthnrQdslUtil authnrQdslUtil;

    @Reference(bind = "setAuthenticationPropagator")
    private AuthenticationPropagator authenticationPropagator;

    @Reference(bind = "setPropertyManager")
    private PropertyManager propertyManager;

    private String auditApplicationName;

    private long auditApplicationTargetResourceId;

    @Activate
    public void activate(final Map<String, Object> componentProperties) {

        long systemResourceId = authnrPermissionChecker.getSystemResourceId();
        authenticationPropagator.runAs(systemResourceId, () -> {

            return transactionHelper.required(() -> {

                String auditApplicationTargetResourceIdString =
                        propertyManager.getProperty(AuditRiProps.AUDIT_APPLICATION_TARGET_RESOURCE_ID);

                if (auditApplicationTargetResourceIdString == null) {

                    auditApplicationTargetResourceId = resourceService.createResource();
                    propertyManager.addProperty(AuditRiProps.AUDIT_APPLICATION_TARGET_RESOURCE_ID,
                            String.valueOf(auditApplicationTargetResourceId));

                } else {
                    auditApplicationTargetResourceId = Long.valueOf(auditApplicationTargetResourceIdString);
                }

                auditApplicationName = String.valueOf(componentProperties.get(AuditRiScr.PROP_AUDIT_APPLICATION_NAME));
                getOrCreateApplication(auditApplicationName);

                return null;
            });
        });

    }

    private void addEventDataValue(
            final SQLInsertClause insert, final QEventData qEventData, final EventData eventData) {
        switch (eventData.eventDataType) {
        case NUMBER:
            insert.set(qEventData.numberValue, eventData.numberValue);
            break;
        case STRING:
            insert.set(qEventData.stringValue, eventData.textValue);
            break;
        case TEXT:
            insert.set(qEventData.textValue, eventData.textValue);
            break;
        case TIMESTAMP:
            insert.set(qEventData.timestampValue, Timestamp.from(eventData.timestampValue));
            break;
        default:
            throw new UnsupportedOperationException("[" + eventData.eventDataType + "] not supported");
        }
    }

    private AuditApplication createApplication(final String applicationName) {

        Objects.requireNonNull(applicationName, "applicationName cannot be null");

        authnrPermissionChecker.checkPermission(auditApplicationTargetResourceId,
                AuditRiPermissions.CREATE_AUDIT_APPLICATION);

        return transactionHelper.required(() -> {

            AuditApplication auditApplication = querydslSupport.execute((connection, configuration) -> {

                long resourceId = resourceService.createResource();

                QApplication qApplication = QApplication.application;

                long applicationId = new SQLInsertClause(connection, configuration, qApplication)
                        .set(qApplication.resourceId, resourceId)
                        .set(qApplication.applicationName, applicationName)
                        .executeWithKey(qApplication.applicationId);

                return new AuditApplication.Builder()
                        .applicationId(applicationId)
                        .applicationName(applicationName)
                        .resourceId(resourceId)
                        .build();
            });

            return auditApplication;
        });
    }

    private AuditEventType createAuditEventType(final String applicationName, final String eventTypeName,
            final boolean withPermissionCheck) {
        Objects.requireNonNull(eventTypeName, "eventTypeName cannot be null");

        AuditApplication auditApplication = requireAppByName(applicationName);

        if (withPermissionCheck) {
            authnrPermissionChecker.checkPermission(
                    auditApplication.resourceId, AuditRiPermissions.CREATE_AUDIT_EVENT_TYPE);
        }

        return transactionHelper.required(() -> {

            AuditEventType auditEventType = querydslSupport.execute((connection, configuration) -> {

                Long resourceId = resourceService.createResource();

                QEventType qEventType = QEventType.eventType;

                long eventTypeId = new SQLInsertClause(connection, configuration, qEventType)
                        .set(qEventType.eventTypeName, eventTypeName)
                        .set(qEventType.applicationId, auditApplication.applicationId)
                        .set(qEventType.resourceId, resourceId)
                        .executeWithKey(qEventType.eventTypeId);

                return new AuditEventType.Builder()
                        .eventTypeId(eventTypeId)
                        .eventTypeName(eventTypeName)
                        .resourceId(resourceId)
                        .build();
            });

            return auditEventType;
        });
    }

    private AuditApplication getAuditApplication(final String applicationName, final boolean withPermissionCheck) {

        Objects.requireNonNull(applicationName, "applicationName cannot be null");

        AuditApplication cachedAuditApplication = auditApplicationCache.get(applicationName);
        if (cachedAuditApplication != null) {

            if (withPermissionCheck && !authnrPermissionChecker.hasPermission(
                    cachedAuditApplication.resourceId, AuditRiPermissions.READ_AUDIT_APPLICATION)) {
                return null;
            }

            return new AuditApplication(cachedAuditApplication);
        }

        return transactionHelper.required(() -> {

            AuditApplication auditApplication = selectApplication(applicationName, withPermissionCheck);
            if (auditApplication != null) {
                auditApplicationCache.put(applicationName, new AuditApplication(auditApplication));
            }

            return auditApplication;
        });
    }

    private AuditEventType getAuditEventType(final String applicationName, final String eventTypeName,
            final boolean withPermissionCheck) {

        Objects.requireNonNull(eventTypeName, "eventTypeName cannot be null");

        AuditApplication auditApplication = requireAppByName(applicationName);

        AuditEventType cachedAuditEventType = auditEventTypeCache.get(
                new CachedEventTypeKey(auditApplication.applicationId, eventTypeName));

        if (cachedAuditEventType != null) {

            if (withPermissionCheck &&
                    !(authnrPermissionChecker.hasPermission(
                            cachedAuditEventType.resourceId, AuditRiPermissions.READ_AUDIT_EVENT_TYPE)
                    || authnrPermissionChecker.hasPermission(
                            auditApplication.resourceId, AuditRiPermissions.READ_AUDIT_APPLICATION))) {
                return null;
            }

            return new AuditEventType(cachedAuditEventType);
        }

        return transactionHelper.required(() -> {

            AuditEventType auditEventType = selectAuditEventType(applicationName, eventTypeName, withPermissionCheck);
            if (auditEventType != null) {
                auditEventTypeCache.put(new CachedEventTypeKey(auditApplication.applicationId, eventTypeName),
                        new AuditEventType(auditEventType));
            }

            return auditEventType;
        });
    }

    @Override
    public AuditApplication getOrCreateApplication(final String applicationName) {
        return Optional
                .ofNullable(getAuditApplication(applicationName, true))
                .orElseGet(() -> createApplication(applicationName));
    }

    private AuditEventType getOrCreateAuditEventType(final String applicationName, final String eventTypeName,
            final boolean withPermissionCheck) {
        return Optional
                .ofNullable(getAuditEventType(applicationName, eventTypeName, withPermissionCheck))
                .orElseGet(() -> createAuditEventType(applicationName, eventTypeName, withPermissionCheck));
    }

    @Override
    public List<AuditEventType> getOrCreateAuditEventTypes(final String... eventTypeNames) {
        return getOrCreateAuditEventTypes(auditApplicationName, eventTypeNames);
    }

    @Override
    public List<AuditEventType> getOrCreateAuditEventTypes(final String applicationName,
            final String... eventTypeNames) {

        Objects.requireNonNull(eventTypeNames, "eventTypeNames cannot be null");
        if (eventTypeNames.length == 0) {
            return Collections.emptyList();
        }

        return transactionHelper.required(() -> {
            List<AuditEventType> rval = new ArrayList<AuditEventType>();
            for (String typeName : eventTypeNames) {
                rval.add(getOrCreateAuditEventType(applicationName, typeName, true));
            }
            return rval;
        });
    }

    @Override
    public void logEvent(final AuditEvent auditEvent) {
        logEvent(auditApplicationName, auditEvent);
    }

    @Override
    public void logEvent(final String applicationName, final AuditEvent auditEvent) {

        Objects.requireNonNull(auditEvent, "auditEvent cannot be null");

        transactionHelper.required(() -> {

            AuditEventType auditEventType = getOrCreateAuditEventType(applicationName,
                    auditEvent.eventTypeName, false);

            if (!authnrPermissionChecker.hasPermission(
                    auditEventType.resourceId, AuditRiPermissions.LOG_TO_EVENT_TYPE)) {

                AuditApplication auditApplication = requireAppByName(applicationName);
                authnrPermissionChecker.checkPermission(
                        auditApplication.resourceId, AuditRiPermissions.LOG_TO_AUDIT_APPLICATION);
            }

            return querydslSupport.execute((connection, configuration) -> {

                QEvent qEvent = QEvent.event;

                long eventId = new SQLInsertClause(connection, configuration, qEvent)
                        .set(qEvent.occuredAt, Timestamp.from(auditEvent.occuredAt))
                        .set(qEvent.eventTypeId, auditEventType.eventTypeId)
                        .executeWithKey(qEvent.eventId);

                for (EventData eventData : auditEvent.eventDataArray) {

                    QEventData qEventData = QEventData.eventData;
                    SQLInsertClause insert = new SQLInsertClause(connection, configuration, qEventData)
                            .set(qEventData.eventId, eventId)
                            .set(qEventData.eventDataName, eventData.eventDataName)
                            .set(qEventData.eventDataType, eventData.eventDataType.toString());
                    addEventDataValue(insert, qEventData, eventData);
                    insert.execute();
                }

                return null;
            });
        });
    }

    private AuditApplication requireAppByName(final String applicationName) {
        return Optional
                .ofNullable(getAuditApplication(applicationName, false))
                .orElseThrow(() -> new IllegalArgumentException("application [" + applicationName + "] does not exist"));
    }

    private AuditApplication selectApplication(final String applicationName, final boolean withPermissionCheck) {
        return querydslSupport.execute((connection, configuration) -> {

            QApplication qApplication = QApplication.application;

            BooleanExpression readApplicationPermission;
            if (withPermissionCheck) {
                readApplicationPermission = authnrQdslUtil.authorizationPredicate(
                        qApplication.resourceId, AuditRiPermissions.READ_AUDIT_APPLICATION);
            } else {
                readApplicationPermission = BooleanTemplate.TRUE;
            }

            return new SQLQuery(connection, configuration)
                    .from(qApplication)
                    .where(qApplication.applicationName.eq(applicationName)
                            .and(readApplicationPermission))
                    .uniqueResult(Projections.fields(AuditApplication.class,
                            qApplication.applicationId,
                            qApplication.applicationName,
                            qApplication.resourceId));
        });
    }

    private AuditEventType selectAuditEventType(final String applicationName, final String eventTypeName,
            final boolean withPermissionCheck) {
        return querydslSupport.execute((connection, configuration) -> {

            QEventType qEventType = QEventType.eventType;
            QApplication qApplication = QApplication.application;

            BooleanExpression permissionExpression;
            if (withPermissionCheck) {
                permissionExpression =
                        authnrQdslUtil.authorizationPredicate(qEventType.resourceId,
                                AuditRiPermissions.READ_AUDIT_EVENT_TYPE)
                                .or(authnrQdslUtil.authorizationPredicate(qApplication.resourceId,
                                        AuditRiPermissions.READ_AUDIT_APPLICATION));
            } else {
                permissionExpression = BooleanTemplate.TRUE;
            }

            return new SQLQuery(connection, configuration)
                    .from(qEventType)
                    .innerJoin(qApplication).on(qEventType.applicationId.eq(qApplication.applicationId))
                    .where(qApplication.applicationName.eq(applicationName)
                            .and(qEventType.eventTypeName.eq(eventTypeName))
                            .and(permissionExpression))
                    .uniqueResult(Projections.fields(AuditEventType.class,
                            qEventType.eventTypeId,
                            qEventType.eventTypeName,
                            qEventType.resourceId));
        });
    }

    public void setAuditApplicationCache(final Map<String, AuditApplication> auditApplicationCache) {
        this.auditApplicationCache = auditApplicationCache;
    }

    public void setAuditEventTypeCache(final Map<CachedEventTypeKey, AuditEventType> auditEventTypeCache) {
        this.auditEventTypeCache = auditEventTypeCache;
    }

    public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
        this.authenticationPropagator = authenticationPropagator;
    }

    public void setAuthnrPermissionChecker(final AuthnrPermissionChecker authnrPermissionChecker) {
        this.authnrPermissionChecker = authnrPermissionChecker;
    }

    public void setAuthnrQdslUtil(final AuthnrQdslUtil authnrQdslUtil) {
        this.authnrQdslUtil = authnrQdslUtil;
    }

    public void setPropertyManager(final PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
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
