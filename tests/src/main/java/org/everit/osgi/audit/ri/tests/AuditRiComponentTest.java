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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.audit.AuditEventTypeManager;
import org.everit.osgi.audit.LoggingService;
import org.everit.osgi.audit.dto.AuditEvent;
import org.everit.osgi.audit.dto.AuditEventType;
import org.everit.osgi.audit.dto.EventData;
import org.everit.osgi.audit.dto.EventData.Builder;
import org.everit.osgi.audit.dto.EventDataType;
import org.everit.osgi.audit.ri.AuditApplicationManager;
import org.everit.osgi.audit.ri.AuditRiPermissions;
import org.everit.osgi.audit.ri.AuditRiProps;
import org.everit.osgi.audit.ri.AuditRiScr;
import org.everit.osgi.audit.ri.InternalAuditEventTypeManager;
import org.everit.osgi.audit.ri.dto.AuditApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authnr.permissionchecker.UnauthorizedException;
import org.everit.osgi.authorization.AuthorizationManager;
import org.everit.osgi.authorization.PermissionChecker;
import org.everit.osgi.authorization.ri.schema.qdsl.QPermission;
import org.everit.osgi.authorization.ri.schema.qdsl.QPermissionInheritance;
import org.everit.osgi.dev.testrunner.TestRunnerConstants;
import org.everit.osgi.props.PropertyManager;
import org.everit.osgi.props.ri.schema.qdsl.QProperty;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.resource.ResourceService;
import org.everit.osgi.resource.ri.schema.qdsl.QResource;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mysema.query.QueryException;
import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;

@Component(name = "AuditRiComponentTest", immediate = true, configurationFactory = false,
        policy = ConfigurationPolicy.OPTIONAL)
@Properties({
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TESTRUNNER_ENGINE_TYPE, value = "junit4"),
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TEST_ID, value = "auditTest"),
        @Property(name = "auditEventTypeManager.target"),
        @Property(name = "internalAuditEventTypeManager.target"),
        @Property(name = "loggingService.target"),
        @Property(name = "auditApplicationManager.target"),
        @Property(name = "resourceService.target"),
        @Property(name = "querydslSupport.target"),
        @Property(name = "transactionHelper.target"),
        @Property(name = "auditApplicationCache.target", value = "(service.description=audit-application-cache)"),
        @Property(name = "auditEventTypeCache.target", value = "(service.description=audit-event-type-cache)"),
        @Property(name = "permissionChecker.target"),
        @Property(name = "authenticationPropagator.target"),
        @Property(name = "authorizationManager.target"),
        @Property(name = "propertyManager.target")
})
@Service(AuditRiComponentTest.class)
public class AuditRiComponentTest {

    private static final Instant TIMESTAMP_V = Instant.now();

    private static final double NUMBER_V = 10.75;

    private static final String TIMESTAMP_N = "timestamp";

    private static final String NUMBER_N = "number";

    private static final String TEXT_N = "text";

    private static final String STRING_N = "string";

    private static final String STRING_V = "string-value";

    private static final String TEXT_V = "text-value";

    @Reference(bind = "setResourceService")
    private ResourceService resourceService;

    @Reference(bind = "setQuerydslSupport")
    private QuerydslSupport querydslSupport;

    @Reference(bind = "setTransactionHelper")
    private TransactionHelper transactionHelper;

    @Reference(bind = "setAuditEventTypeManager")
    private AuditEventTypeManager auditEventTypeManager;

    @Reference(bind = "setInternalAuditEventTypeManager")
    private InternalAuditEventTypeManager internalAuditEventTypeManager;

    @Reference(bind = "setLoggingService")
    private LoggingService loggingService;

    @Reference(bind = "setAuditApplicationManager")
    private AuditApplicationManager auditApplicationManager;

    @Reference(bind = "setAuditApplicationCache")
    private Map<?, ?> auditApplicationCache;

    @Reference(bind = "setAuditEventTypeCache")
    private Map<?, ?> auditEventTypeCache;

    @Reference(bind = "setPermissionChecker")
    private PermissionChecker permissionChecker;

    @Reference(bind = "setAuthenticationPropagator")
    private AuthenticationPropagator authenticationPropagator;

    @Reference(bind = "setAuthorizationManager")
    private AuthorizationManager authorizationManager;

    @Reference(bind = "setPropertyManager")
    private PropertyManager propertyManager;

    private long auditApplicationTargetResrouceId;

    private String testAuditApplicationName;

    private long testAuditApplicationResourceId;

    private long systemResourceId;

    @Activate
    public void activate() {
        auditApplicationTargetResrouceId =
                Long.valueOf(propertyManager.getProperty(AuditRiProps.AUDIT_APPLICATION_TARGET_RESOURCE_ID));

        systemResourceId = permissionChecker.getSystemResourceId();

        AuditApplication testAuditApplication = authenticationPropagator.runAs(systemResourceId, () -> {
            return auditApplicationManager.getOrCreateApplication(testAuditApplicationName);
        });

        testAuditApplicationResourceId = testAuditApplication.resourceId;

    }

    @After
    public void after() {
        clearPermissions();
        clearAuditCaches();
    }

    private void assertEvent(final String eventTypeName) {
        List<EventData> eventDataList = querydslSupport.execute((connection, configuration) -> {

            QEvent qEvent = QEvent.event;
            QEventType qEventType = QEventType.eventType;
            QEventData qEventData = QEventData.eventData;

            List<Tuple> tuples = new SQLQuery(connection, configuration)
                    .from(qEvent)
                    .innerJoin(qEventType)
                    .on(qEventType.eventTypeId.eq(qEvent.eventTypeId))
                    .innerJoin(qEventData)
                    .on(qEventData.eventId.eq(qEvent.eventId))
                    .where(qEventType.eventTypeName.eq(eventTypeName))
                    .orderBy(qEventData.eventDataId.asc())
                    .list(qEventData.eventDataName, qEventData.eventDataType,
                            qEventData.stringValue,
                            qEventData.textValue,
                            qEventData.numberValue,
                            qEventData.timestampValue);

            List<EventData> rval = new ArrayList<>();
            for (Tuple tuple : tuples) {
                Builder eventDataBuilder = new EventData.Builder(tuple.get(qEventData.eventDataName));
                String eventDataTypeString = tuple.get(qEventData.eventDataType);
                EventDataType eventDataType = EventDataType.valueOf(eventDataTypeString);
                switch (eventDataType) {
                case STRING:
                    String stringValue = tuple.get(qEventData.stringValue);
                    rval.add(eventDataBuilder.buildStringValue(stringValue));
                    break;
                case TEXT:
                    String textValue = tuple.get(qEventData.textValue);
                    rval.add(eventDataBuilder.buildTextValue(false, textValue));
                    break;
                case NUMBER:
                    double numberValue = tuple.get(qEventData.numberValue);
                    rval.add(eventDataBuilder.buildNumberValue(numberValue));
                    break;
                case TIMESTAMP:
                    Instant timestampValue = tuple.get(qEventData.timestampValue).toInstant();
                    rval.add(eventDataBuilder.buildTimestampValue(timestampValue));
                    break;
                }
            }

            return rval;
        });

        Assert.assertEquals(4, eventDataList.size());
        Assert.assertEquals(new EventData.Builder(STRING_N).buildStringValue(STRING_V), eventDataList.get(0));
        Assert.assertEquals(new EventData.Builder(TEXT_N).buildTextValue(false, TEXT_V), eventDataList.get(1));
        Assert.assertEquals(new EventData.Builder(NUMBER_N).buildNumberValue(NUMBER_V), eventDataList.get(2));
        Assert.assertEquals(new EventData.Builder(TIMESTAMP_N).buildTimestampValue(TIMESTAMP_V), eventDataList.get(3));
    }

    @Before
    public void before() {
        clearAuditCaches();
    }

    private void clearAuditCaches() {
        auditApplicationCache.clear();
        auditEventTypeCache.clear();
    }

    private void clearPermissions() {
        querydslSupport.execute((connection, configuration) -> {

            new SQLDeleteClause(connection, configuration, QPermission.permission).execute();
            new SQLDeleteClause(connection, configuration, QPermissionInheritance.permissionInheritance).execute();

            return null;
        });
    }

    @Deactivate
    public void deactivate() {
        querydslSupport.execute((connection, configuration) -> {

            new SQLDeleteClause(connection, configuration, QProperty.property).execute();

            new SQLDeleteClause(connection, configuration, QPermission.permission).execute();
            new SQLDeleteClause(connection, configuration, QPermissionInheritance.permissionInheritance).execute();

            new SQLDeleteClause(connection, configuration, QEventData.eventData).execute();
            new SQLDeleteClause(connection, configuration, QEvent.event).execute();
            new SQLDeleteClause(connection, configuration, QEventType.eventType).execute();
            new SQLDeleteClause(connection, configuration, QApplication.application).execute();

            new SQLDeleteClause(connection, configuration, QResource.resource).execute();

            return null;
        });
        clearAuditCaches();
    }

    private long logEvent(final String eventTypeName) {
        AuditEvent event = new AuditEvent.Builder().eventTypeName(eventTypeName)
                .addStringEventData(STRING_N, STRING_V)
                .addTextEventData(TEXT_N, false, TEXT_V)
                .addNumberEventData(NUMBER_N, NUMBER_V)
                .addTimestampEventData(TIMESTAMP_N, TIMESTAMP_V)
                .build();
        loggingService.logEvent(event);
        return querydslSupport.execute((connection, configuration) -> {
            QEvent qEvent = QEvent.event;
            return new SQLQuery(connection, configuration)
                    .from(qEvent)
                    .orderBy(qEvent.eventId.desc())
                    .limit(1)
                    .uniqueResult(qEvent.eventId);
        });
    }

    public void setAuditApplicationCache(final Map<String, AuditApplication> auditApplicationCache) {
        this.auditApplicationCache = auditApplicationCache;
    }

    public void setAuditApplicationManager(final AuditApplicationManager auditApplicationManager,
            final Map<String, Object> serviceProperties) {
        this.auditApplicationManager = auditApplicationManager;
        testAuditApplicationName = String.valueOf(serviceProperties.get(AuditRiScr.PROP_AUDIT_APPLICATION_NAME));
    }

    public void setAuditEventTypeCache(final Map<?, ?> auditEventTypeCache) {
        this.auditEventTypeCache = auditEventTypeCache;
    }

    public void setAuditEventTypeManager(final AuditEventTypeManager auditEventTypeManager) {
        this.auditEventTypeManager = auditEventTypeManager;
    }

    public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
        this.authenticationPropagator = authenticationPropagator;
    }

    public void setAuthorizationManager(final AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    public void setInternalAuditEventTypeManager(final InternalAuditEventTypeManager internalAuditEventTypeManager) {
        this.internalAuditEventTypeManager = internalAuditEventTypeManager;
    }

    public void setLoggingService(final LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public void setPermissionChecker(final PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
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

    @Test
    public void testGetOrCreateAuditApplication() {

        String applicationName = "application-1";

        // no permission to read and create
        try {
            auditApplicationManager.getOrCreateApplication(applicationName);
            Assert.fail();
        } catch (UnauthorizedException e) {
            Assert.assertTrue(e.getMessage().contains("not authorized"));
        }

        long authorizedResourceId = resourceService.createResource();

        // add permission to create
        authorizationManager.addPermission(authorizedResourceId, auditApplicationTargetResrouceId,
                AuditRiPermissions.CREATE_AUDIT_APPLICATION);

        AuditApplication newApplication = authenticationPropagator.runAs(authorizedResourceId, () -> {
            // create
                AuditApplication auditApplication = auditApplicationManager.getOrCreateApplication(applicationName);
                Assert.assertNotNull(auditApplication);
                return auditApplication;
            });

        // no permission to read and already exists
        authenticationPropagator.runAs(authorizedResourceId, () -> {

            try {
                auditApplicationManager.getOrCreateApplication(applicationName);
                Assert.fail();
            } catch (QueryException e) {
                Assert.assertTrue(e.getCause().getMessage().contains("unique_application_name"));
            }
            return null;
        });

        // add permission to read
        authorizationManager.addPermission(authorizedResourceId, newApplication.resourceId,
                AuditRiPermissions.READ_AUDIT_APPLICATION);

        authenticationPropagator.runAs(authorizedResourceId, () -> {

            // read from db
                AuditApplication existingApplication = auditApplicationManager.getOrCreateApplication(applicationName);
                Assert.assertNotNull(existingApplication);
                Assert.assertEquals(newApplication, existingApplication);
                Assert.assertEquals(newApplication.applicationId, existingApplication.applicationId);
                Assert.assertEquals(newApplication.applicationName, existingApplication.applicationName);
                Assert.assertEquals(newApplication.resourceId, existingApplication.resourceId);

                // read from cache
                AuditApplication cachedApplication = auditApplicationManager.getOrCreateApplication(applicationName);
                Assert.assertEquals(existingApplication, cachedApplication);

                return null;
            });

        // no permission to read but cached already
        try {
            auditApplicationManager.getOrCreateApplication(applicationName);
            Assert.fail();
        } catch (UnauthorizedException e) {
            Assert.assertTrue(e.getMessage().contains("not authorized"));
        }

    }

    @Test
    public void testGetOrCreateAuditApplicationNullName() {
        try {
            auditApplicationManager.getOrCreateApplication(null);
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.assertEquals("applicationName cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetOrCreateAuditEventType() {
        String eventTypeName = "test-event-type";

        // no permission to read and create
        try {
            auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeName);
            Assert.fail();
        } catch (UnauthorizedException e) {
            Assert.assertTrue(e.getMessage().contains("not authorized"));
        }

        long authorizedResourceId = resourceService.createResource();

        // add permission to create
        authorizationManager.addPermission(authorizedResourceId, testAuditApplicationResourceId,
                AuditRiPermissions.CREATE_AUDIT_EVENT_TYPE);

        AuditEventType newEventType = authenticationPropagator.runAs(authorizedResourceId, () -> {
            AuditEventType auditEventType = auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeName).get(0);
            Assert.assertNotNull(auditEventType);
            Assert.assertEquals(eventTypeName, auditEventType.eventTypeName);
            return auditEventType;
        });

        // no permission to read and already exists
        authenticationPropagator.runAs(authorizedResourceId, () -> {
            try {
                auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeName);
            } catch (QueryException e) {
                Assert.assertTrue(e.getCause().getMessage().contains("unique_application_id_event_type_name"));
            }
            return null;
        });

        // add permission to read
        authorizationManager.addPermission(authorizedResourceId, newEventType.resourceId,
                AuditRiPermissions.READ_AUDIT_EVENT_TYPE);

        authenticationPropagator.runAs(authorizedResourceId, () -> {

            // read from db
                AuditEventType existingEventType = auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeName)
                        .get(0);
                Assert.assertNotNull(existingEventType);
                Assert.assertEquals(newEventType, existingEventType);
                Assert.assertEquals(newEventType.eventTypeId, existingEventType.eventTypeId);
                Assert.assertEquals(newEventType.eventTypeName, existingEventType.eventTypeName);
                Assert.assertEquals(newEventType.resourceId, existingEventType.resourceId);

                // read from cache
                AuditEventType cachedEventType = auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeName)
                        .get(0);
                Assert.assertEquals(existingEventType, cachedEventType);

                return null;
            });

        // no permission to read but cached already
        try {
            auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeName);
            Assert.fail();
        } catch (UnauthorizedException e) {
            Assert.assertTrue(e.getMessage().contains("not authorized"));
        }

        // invoke with multiple event types
        authenticationPropagator.runAs(authorizedResourceId, () -> {

            List<AuditEventType> auditEventTypes =
                    auditEventTypeManager.getOrCreateAuditEventTypes("et1", "et2", "et3");
            Assert.assertEquals(3, auditEventTypes.size());

            return null;
        });

        // add permission to read application only
        clearPermissions();
        authorizationManager.addPermission(authorizedResourceId, testAuditApplicationResourceId,
                AuditRiPermissions.READ_AUDIT_APPLICATION);

        authenticationPropagator.runAs(authorizedResourceId, () -> {
            AuditEventType cachedEventType = auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeName).get(0);
            Assert.assertEquals(newEventType, cachedEventType);
            return null;
        });

    }

    @Test
    public void testGetOrCreateAuditEventTypesEmpty() {
        Assert.assertTrue(auditEventTypeManager.getOrCreateAuditEventTypes().isEmpty());
    }

    @Test
    public void testGetOrCreateAuditEventTypesForApplicationFail() {
        String nonExistentApplicationName = "non-existent-application";
        String eventTypeName = "random-event-type";
        try {
            internalAuditEventTypeManager.getOrCreateAuditEventTypes(nonExistentApplicationName, eventTypeName);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            Assert.assertTrue(message, message.contains(nonExistentApplicationName)
                    && message.contains("does not exist"));
        }
    }

    @Test
    public void testGetOrCreateAuditEventTypesNullEventName() {
        try {
            String[] eventTypeNames = null;
            auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeNames);
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.assertEquals("eventTypeNames cannot be null", e.getMessage());
        }
        try {
            String[] eventTypeNames = new String[] { null };
            auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeNames);
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.assertEquals("eventTypeName cannot be null", e.getMessage());
        }
    }

    @Test
    public void testLogEvent() {
        String eventTypeNameA = "event-type-a";
        String eventTypeNameB = "event-type-b";

        // no permission to log application and event type
        try {
            logEvent(eventTypeNameA);
            Assert.fail();
        } catch (UnauthorizedException e) {
            Assert.assertTrue(e.getMessage().contains("not authorized"));
        }

        long authorizedResourceId = resourceService.createResource();

        // add permission to log event type A
        long systemResourceId = permissionChecker.getSystemResourceId();
        AuditEventType auditEventTypeA = authenticationPropagator.runAs(systemResourceId, () -> {
            return auditEventTypeManager.getOrCreateAuditEventTypes(eventTypeNameA).get(0);
        });
        authorizationManager.addPermission(authorizedResourceId, auditEventTypeA.resourceId,
                AuditRiPermissions.LOG_TO_EVENT_TYPE);

        authenticationPropagator.runAs(authorizedResourceId, () -> {

            logEvent(eventTypeNameA);
            assertEvent(eventTypeNameA);

            return null;
        });

        clearPermissions();

        // add permission to log application
        authorizationManager.addPermission(authorizedResourceId, testAuditApplicationResourceId,
                AuditRiPermissions.LOG_TO_AUDIT_APPLICATION);

        authenticationPropagator.runAs(authorizedResourceId, () -> {

            logEvent(eventTypeNameA);
            logEvent(eventTypeNameB);
            assertEvent(eventTypeNameB);

            return null;
        });

    }

    @Test
    public void testLogEventNull() {
        try {
            loggingService.logEvent(null);
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.assertTrue(e.getMessage().equals("auditEvent cannot be null"));
        }
    }

}
