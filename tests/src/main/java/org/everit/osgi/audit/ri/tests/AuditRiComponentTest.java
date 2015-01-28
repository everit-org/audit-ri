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
import java.util.List;
import java.util.UUID;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.audit.EventTypeManager;
import org.everit.osgi.audit.LoggingService;
import org.everit.osgi.audit.dto.AuditEvent;
import org.everit.osgi.audit.dto.EventData;
import org.everit.osgi.audit.dto.EventType;
import org.everit.osgi.audit.ri.AuditApplicationManager;
import org.everit.osgi.audit.ri.InternalEventTypeManager;
import org.everit.osgi.audit.ri.dto.AuditApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;
import org.everit.osgi.dev.testrunner.TestRunnerConstants;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.resource.ResourceService;
import org.junit.Assert;
import org.junit.Test;

import com.mysema.query.QueryException;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.types.ConstructorExpression;

@Component(name = "AuditRiComponentTest", immediate = true, configurationFactory = false,
        policy = ConfigurationPolicy.OPTIONAL)
@Properties({
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TESTRUNNER_ENGINE_TYPE, value = "junit4"),
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TEST_ID, value = "auditTest"),
        @Property(name = "eventTypeManager.target"),
        @Property(name = "internalEventTypeManager.target"),
        @Property(name = "loggingService.target"),
        @Property(name = "auditApplicationManager.target"),
        @Property(name = "resourceService.target"),
        @Property(name = "querydslSupport.target")
})
@Service(AuditRiComponentTest.class)
public class AuditRiComponentTest {

    private static final String TEST_APPLICATION_NAME = "test-application-2";

    @Reference(bind = "setResourceService")
    private ResourceService resourceService;

    @Reference(bind = "setQuerydslSupport")
    private QuerydslSupport querydslSupport;

    @Reference(bind = "setEventTypeManager")
    private EventTypeManager eventTypeManager;

    @Reference(bind = "setInternalEventTypeManager")
    private InternalEventTypeManager internalEventTypeManager;

    @Reference(bind = "setLoggingService")
    private LoggingService loggingService;

    @Reference(bind = "setAuditApplicationManager")
    private AuditApplicationManager auditApplicationManager;

    @Deactivate
    public void cleanupDatabase() {
        querydslSupport.execute((connection, configuration) -> {
            new SQLDeleteClause(connection, configuration, QEventData.eventData).execute();
            new SQLDeleteClause(connection, configuration, QEvent.event).execute();
            new SQLDeleteClause(connection, configuration, QEventType.eventType).execute();
            new SQLDeleteClause(connection, configuration, QApplication.application).execute();
            return null;
        });
    }

    private AuditApplication createAuditApplication() {
        return auditApplicationManager.getOrCreateApplication(TEST_APPLICATION_NAME);
    }

    private long logDefaultEvent() {
        EventData[] eventDataArray = new EventData[] {
                new EventData("string", "string"),
                new EventData("text", false, "text"),
                new EventData("number", 10.75),
                new EventData("binary", new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }),
                new EventData("timestamp", Instant.now())
        };
        AuditEvent event = new AuditEvent("login", eventDataArray);
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

    public void setAuditApplicationManager(final AuditApplicationManager auditApplicationManager) {
        this.auditApplicationManager = auditApplicationManager;
    }

    public void setEventTypeManager(final EventTypeManager eventTypeManager) {
        this.eventTypeManager = eventTypeManager;
    }

    public void setInternalEventTypeManager(final InternalEventTypeManager internalEventTypeManager) {
        this.internalEventTypeManager = internalEventTypeManager;
    }

    public void setLoggingService(final LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public void setQuerydslSupport(final QuerydslSupport querydslSupport) {
        this.querydslSupport = querydslSupport;
    }

    public void setResourceService(final ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Test(expected = QueryException.class)
    public void testCreateApplicationConstraintViolation() {
        long resourceId = resourceService.createResource();
        String applicationName = UUID.randomUUID().toString();
        auditApplicationManager.createApplication(resourceId, applicationName);
        auditApplicationManager.createApplication(resourceId, applicationName);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateApplicationFail() {
        auditApplicationManager.createApplication(0, null);
    }

    @Test
    public void testCreateApplicationSuccess() {
        createAuditApplication();
        QApplication qApplication = QApplication.application;
        querydslSupport.execute((connection, configuration) -> {
            AuditApplication result = new SQLQuery(connection, configuration)
                    .from(qApplication)
                    .where(qApplication.applicationName.eq(TEST_APPLICATION_NAME))
                    .uniqueResult(ConstructorExpression.create(AuditApplication.class,
                            qApplication.applicationId,
                            qApplication.applicationName,
                            qApplication.resourceId));
            Assert.assertEquals(TEST_APPLICATION_NAME, result.getApplicationName());
            Assert.assertNotNull(result.getResourceId());
            return null;
        });
    }

    @Test
    public void testCreateEventType() {
        EventType actual = eventTypeManager.getOrCreateEventTypes("login").get(0);
        Assert.assertNotNull(actual);
        Assert.assertEquals("login", actual.getName());
    }

    @Test
    public void testFindApplicationByNameSuccess() {
        createAuditApplication();
        AuditApplication actual = auditApplicationManager.getApplicationByName(TEST_APPLICATION_NAME);
        Assert.assertNotNull(actual);
    }

    @Test(expected = NullPointerException.class)
    public void testGetApplicationByNameFailure() {
        auditApplicationManager.getApplicationByName(null);
    }

    @Test
    public void testGetApplications() {
        List<AuditApplication> actual = auditApplicationManager.getApplications();
        int initialSize = actual.size();
        auditApplicationManager.createApplication(resourceService.createResource(), "app1");
        auditApplicationManager.createApplication(resourceService.createResource(), "app2");
        actual = auditApplicationManager.getApplications();
        Assert.assertEquals(initialSize + 2, actual.size());
    }

    @Test
    public void testGetEventTypeByNameFail() {
        Assert.assertNull(eventTypeManager.getEventTypeByName(UUID.randomUUID().toString()));
    }

    @Test(expected = NullPointerException.class)
    public void testGetEventTypeByNameNPE() {
        eventTypeManager.getEventTypeByName(null);
    }

    @Test
    public void testGetEventTypeByNameSuccess() {
        EventType loginEventType = eventTypeManager.getOrCreateEventTypes("login", "logout").get(0);
        EventType actual = eventTypeManager.getEventTypeByName("login");
        Assert.assertNotNull(actual);
        Assert.assertEquals(loginEventType.getId(), actual.getId());
        Assert.assertEquals(loginEventType.getResourceId(), actual.getResourceId());
        Assert.assertEquals(loginEventType.getName(), actual.getName());
    }

    @Test
    public void testGetOrCreateApplication() {
        AuditApplication newApp = auditApplicationManager.getOrCreateApplication(TEST_APPLICATION_NAME);
        Assert.assertNotNull(newApp);
        AuditApplication existingApp = auditApplicationManager.getOrCreateApplication(TEST_APPLICATION_NAME);
        Assert.assertNotNull(existingApp);
        Assert.assertEquals(newApp.getApplicationId(), existingApp.getApplicationId());
        Assert.assertEquals(newApp.getApplicationName(), existingApp.getApplicationName());
        Assert.assertEquals(newApp.getResourceId(), existingApp.getResourceId());
    }

    @Test(expected = NullPointerException.class)
    public void testGetOrCreateApplicationNullName() {
        auditApplicationManager.getOrCreateApplication(null);
    }

    @Test
    public void testGetOrCreateEventTypesEmpty() {
        Assert.assertTrue(eventTypeManager.getOrCreateEventTypes().isEmpty());
    }

    @Test
    public void testGetOrCreateEventTypesForApplicationFail() {
        String nonExistentApplicationName = UUID.randomUUID().toString();
        try {
            internalEventTypeManager.getOrCreateEventTypesForApplication(nonExistentApplicationName, "login");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            Assert.assertTrue(message,
                    message.contains(nonExistentApplicationName) && message.contains("does not exist"));
        }
    }

    @Test(expected = NullPointerException.class)
    public void testGetOrCreateEventTypesNullEventName() {
        eventTypeManager.getOrCreateEventTypes((String) null);
    }

    @Test
    public void testGetOrCreateEventTypesSame() {
        EventType firstEvtType = eventTypeManager.getOrCreateEventTypes("login").get(0);
        EventType secondEvtType = eventTypeManager.getOrCreateEventTypes("login").get(0);
        Assert.assertEquals(firstEvtType.getId(), secondEvtType.getId());
    }

    @Test
    public void testGetOrCreateEventTypesUnique() {
        int initialSize = eventTypeManager.getEventTypes().size();
        eventTypeManager.getOrCreateEventTypes(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        List<EventType> actual = eventTypeManager.getEventTypes();
        Assert.assertEquals(initialSize + 3, actual.size());
    }

    @Test
    public void testLogEvent() {
        createAuditApplication();
        logDefaultEvent();
        querydslSupport.execute((connection, configuration) -> {
            QEvent qEvent = QEvent.event;
            Long eventId = new SQLQuery(connection, configuration)
                    .from(qEvent).limit(1)
                    .uniqueResult(ConstructorExpression.create(Long.class, qEvent.eventId));
            Assert.assertNotNull(eventId);
            QEventData qEventData = QEventData.eventData;
            long dataCount = new SQLQuery(connection, configuration)
                    .from(qEventData)
                    .where(qEventData.eventId.eq(eventId))
                    .count();
            Assert.assertEquals(5, dataCount);
            return null;
        });
    }

    @Test
    public void testNullGetApplicationByName() {
        Assert.assertNull(auditApplicationManager.getApplicationByName("nonexistent"));
    }

}
