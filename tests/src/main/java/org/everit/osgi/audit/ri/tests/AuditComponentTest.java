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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.audit.api.AuditService;
import org.everit.osgi.audit.api.dto.Application;
import org.everit.osgi.audit.api.dto.DataFilter;
import org.everit.osgi.audit.api.dto.Event;
import org.everit.osgi.audit.api.dto.EventData;
import org.everit.osgi.audit.api.dto.EventDataType;
import org.everit.osgi.audit.api.dto.EventType;
import org.everit.osgi.audit.api.dto.EventUi;
import org.everit.osgi.audit.api.dto.Operator;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.junit.After;
import org.junit.Assert;
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
        @Property(name = "sqlTemplates.target"),
        @Property(name = "localizationService.target")
})
@TestDuringDevelopment
public class AuditComponentTest {

    private static final String APPNAME = "appname";

    @Reference
    private DataSource dataSource;

    @Reference
    private SQLTemplates sqlTemplates;

    @Reference
    private AuditService auditComponent;

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
        try (Connection conn = dataSource.getConnection()) {
            new SQLDeleteClause(conn, sqlTemplates, new QEventData("evtData")).execute();
            new SQLDeleteClause(conn, sqlTemplates, new QEvent("evt")).execute();
            new SQLDeleteClause(conn, sqlTemplates, new QEventType("evtType")).execute();
            new SQLDeleteClause(conn, sqlTemplates, new QApplication("app")).execute();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = QueryException.class)
    @TestDuringDevelopment
    public void createApplicationConstraintViolation() {
        createDefaultApp();
        createDefaultApp();
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void createApplicationFail() {
        auditComponent.createApplication(null);
    }

    @Test(timeout = 6000)
    @TestDuringDevelopment
    public void createApplicationSuccess() {
        createDefaultApp();
        QApplication app = new QApplication("app");
        try (Connection conn = dataSource.getConnection()) {
            Application result = new SQLQuery(conn, sqlTemplates)
                    .from(app)
                    .where(app.applicationName.eq(APPNAME))
                    .uniqueResult(ConstructorExpression.create(Application.class,
                            app.applicationId,
                            app.applicationName,
                            app.resourceId));
            Assert.assertEquals(APPNAME, result.getAppName());
            Assert.assertNotNull(result.getResourceId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Application createDefaultApp() {
        return auditComponent.createApplication(APPNAME);
    }

    @Test
    @TestDuringDevelopment
    public void createEventType() {
        Application app = createDefaultApp();
        EventType actual = auditComponent.getOrCreateEventType(APPNAME, "login");
        Assert.assertNotNull(actual);
        Assert.assertEquals("login", actual.getName());
        Assert.assertEquals(app.getApplicationId(), actual.getApplicationId());
    }

    @Test(expected = IllegalArgumentException.class)
    @TestDuringDevelopment
    public void createEventTypeNonexistentApp() {
        auditComponent.getOrCreateEventType("nonexistent", "login");
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void findAppByNameFilure() {
        auditComponent.findAppByName(null);
    }

    @Test
    @TestDuringDevelopment
    public void findAppByNameSuccess() {
        createDefaultApp();
        Application actual = auditComponent.findAppByName(APPNAME);
        Assert.assertNotNull(actual);
    }

    @Test
    @TestDuringDevelopment
    public void findEvents() {
        Application app = createDefaultApp();
        Long[] appIds = new Long[] { app.getApplicationId() };
        EventType[] evtTypes = auditComponent.getOrCreateEventTypes(app.getAppName(), new String[] { "evtType0",
            "evtType1", "evtType2" });
        auditComponent.logEvent(new Event("evtType0", APPNAME, new EventData[] { new EventData("strData", "aaa"),
                new EventData("intData", 10) }));
        auditComponent.logEvent(new Event("evtType0", APPNAME, new EventData[] { new EventData("strData", "aaa"),
                new EventData("intData", 20) }));
        auditComponent.logEvent(new Event("evtType1", APPNAME, new EventData[] { new EventData("strData", "bbb"),
                new EventData("textData", false, "longtext") }));
        auditComponent.logEvent(new Event("evtType2", APPNAME, new EventData[] { new EventData("strData", "ccc") }));
        Long[] eventTypeIds = new Long[] { evtTypes[0].getId(), evtTypes[1].getId() };
        List<String> dataFields = Arrays.asList("strData", "intData");
        List<DataFilter> dataFilters = Arrays.asList(new DataFilter(Operator.GT, new EventData("intData", 15)));
        List<EventUi> actual = auditComponent.findEvents(appIds, eventTypeIds, dataFields, dataFilters, null, null,
                null, 0, 100);
        Assert.assertNotNull(actual);
        Assert.assertEquals(2, actual.size());
        EventUi firstResult = actual.get(0);
        Assert.assertEquals("evtType1", firstResult.getName());
        Assert.assertEquals("bbb", firstResult.getEventData().get("strData").getTextValue());
        Assert.assertNull(firstResult.getEventData().get("textData"));
        EventUi secondResult = actual.get(1);
        Assert.assertEquals("evtType0", secondResult.getName());
        Assert.assertEquals("aaa", secondResult.getEventData().get("strData").getTextValue());
        Assert.assertEquals(20.0, secondResult.getEventData().get("intData").getNumberValue(), 0.1);
    }

    @Test
    @TestDuringDevelopment
    public void findEventsEmptyResult() {
        long appId = createDefaultApp().getApplicationId();
        List<EventUi> actual = auditComponent.findEvents(new Long[] { new Long(appId) }, null, null, null, null, null,
                null, 0, 10000);
        Assert.assertNotNull(actual);
        Assert.assertEquals(0, actual.size());
    }

    @Test
    @TestDuringDevelopment
    public void getApplications() {
        List<Application> actual = auditComponent.getApplications();
        Assert.assertEquals(0, actual.size());
        auditComponent.createApplication("app1");
        auditComponent.createApplication("app2");
        actual = auditComponent.getApplications();
        Assert.assertEquals(2, actual.size());
    }

    @Test
    @TestDuringDevelopment
    public void getEventById() {
        createDefaultApp();
        long eventId = logDefaultEvent();
        EventUi event = auditComponent.getEventById(eventId);
        Assert.assertNotNull(event);
        Assert.assertEquals(eventId, event.getId().longValue());
        Assert.assertEquals("login", event.getName());
        Assert.assertEquals(APPNAME, event.getApplicationName());
        Assert.assertNotNull(event.getSaveTimeStamp());
        Assert.assertNotNull(event.getEventData());
        Assert.assertEquals(2, event.getEventData().size());
        EventData hostData = event.getEventData().get("host");
        Assert.assertEquals("example.org", hostData.getTextValue());
        Assert.assertEquals(EventDataType.STRING, hostData.getEventDataType());
        EventData cpuLoadData = event.getEventData().get("cpuLoad");
        Assert.assertEquals(10.75, cpuLoadData.getNumberValue(), 0.01);
        Assert.assertEquals(EventDataType.NUMBER, cpuLoadData.getEventDataType());
    }

    @Test
    @TestDuringDevelopment
    public void getEventByIdNoData() {
        createDefaultApp();
        Event event = new Event("login", APPNAME, new EventData[] {});
        auditComponent.logEvent(event);
        try (Connection conn = dataSource.getConnection()) {
            QEvent evt = new QEvent("evt");
            long eventId = new SQLQuery(conn, sqlTemplates).from(evt)
                    .orderBy(evt.eventId.desc())
                    .limit(1).uniqueResult(evt.eventId);
            EventUi result = auditComponent.getEventById(eventId);
            Assert.assertNotNull(result);
            Assert.assertTrue(result.getEventData().isEmpty());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @TestDuringDevelopment
    public void getEventByIdNotFound() {
        Assert.assertNull(auditComponent.getEventById(-1));
    }

    @Test
    @TestDuringDevelopment
    public void getEventType() {
        createDefaultApp();
        EventType firstEvtType = auditComponent.getOrCreateEventType(APPNAME, "login");
        EventType secondEvtType = auditComponent.getOrCreateEventType(APPNAME, "login");
        Assert.assertEquals(firstEvtType.getId(), secondEvtType.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    @TestDuringDevelopment
    public void getEventTypeByNameForApplicationFail() {
        createDefaultApp();
        auditComponent.getEventTypeByNameForApplication(0L, "nonexistent");
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void getEventTypeByNameForApplicationNullEventName() {
        auditComponent.getEventTypeByNameForApplication(0, null);
    }

    @Test
    @TestDuringDevelopment
    public void getEventTypeByNameForApplicationSuccess() {
        Application app = auditComponent.createApplication(APPNAME);
        auditComponent.getOrCreateEventType(APPNAME, "login");
        auditComponent.getOrCreateEventType(APPNAME, "logout");
        EventType actual = auditComponent.getEventTypeByNameForApplication(app.getApplicationId(), "login");
        Assert.assertNotNull(actual);
        Assert.assertEquals(app.getApplicationId(), actual.getApplicationId());
        Assert.assertEquals("login", actual.getName());
    }

    @Test
    @TestDuringDevelopment
    public void getEventTypesByApplication() {
        Application app = auditComponent.createApplication(APPNAME);
        auditComponent.getOrCreateEventType(APPNAME, "login");
        auditComponent.getOrCreateEventType(APPNAME, "logout");
        List<EventType> events = auditComponent.getEventTypesByApplication(app.getApplicationId());
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());
    }

    @Test
    @TestDuringDevelopment
    public void getOrCreateApplication() {
        Application newApp = auditComponent.getOrCreateApplication(APPNAME);
        Assert.assertNotNull(newApp);
        Application existingApp = auditComponent.getOrCreateApplication(APPNAME);
        Assert.assertNotNull(existingApp);
        Assert.assertEquals(newApp.getApplicationId(), existingApp.getApplicationId());
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void getOrCreateApplicationNullName() {
        auditComponent.getOrCreateApplication(null);
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void getOrCreateEventTypeNullAppName() {
        auditComponent.getOrCreateEventType(null, null);
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void getOrCreateEventTypeNullEventName() {
        auditComponent.getOrCreateEventType(APPNAME, null);
    }

    @Test(expected = IllegalArgumentException.class)
    @TestDuringDevelopment
    public void getOrCreateEventTypesForAppNonexistentApplication() {
        auditComponent.getOrCreateEventTypes("nonexistent", new String[] {});
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void getOrCreateEventTypesForAppNullAppName() {
        auditComponent.getOrCreateEventTypes(null, null);
    }

    @Test(expected = NullPointerException.class)
    @TestDuringDevelopment
    public void getOrCreateEventTypesForAppNullEventTypeNames() {
        auditComponent.getOrCreateEventTypes(APPNAME, null);
    }

    @Test
    @TestDuringDevelopment
    public void getOrCreateEventTypesForAppSuccess() {
        Application app = auditComponent.createApplication(APPNAME);
        auditComponent.getOrCreateEventType(APPNAME, "login");
        auditComponent.getOrCreateEventType(APPNAME, "logout");
        auditComponent.getOrCreateEventTypes(APPNAME, new String[] { "addComment", "balanceUpdate", "viewProduct" });
        List<EventType> actual = auditComponent.getEventTypesByApplication(app.getApplicationId());
        Assert.assertEquals(5, actual.size());
    }

    private long logDefaultEvent() {
        EventData[] eventDataArray = new EventData[] {
                new EventData("host", "example.org"),
                new EventData("cpuLoad", 10.75)
        };
        Event event = new Event("login", APPNAME, eventDataArray);
        auditComponent.logEvent(event);
        try (Connection conn = dataSource.getConnection()) {
            QEvent evt = new QEvent("evt");
            return new SQLQuery(conn, sqlTemplates).from(evt)
                    .orderBy(evt.eventId.desc())
                    .limit(1).uniqueResult(evt.eventId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @TestDuringDevelopment
    public void logEvent() {
        createDefaultApp();
        logDefaultEvent();
        QEvent evt = new QEvent("evt");
        try (Connection conn = dataSource.getConnection()) {
            Long eventId = new SQLQuery(conn, sqlTemplates).from(evt).limit(1)
                    .uniqueResult(ConstructorExpression.create(Long.class, evt.eventId));
            Assert.assertNotNull(eventId);
            QEventData evtData = new QEventData("evtData");
            long dataCount = new SQLQuery(conn, sqlTemplates).from(evtData).where(evtData.eventId.eq(eventId)).count();
            Assert.assertEquals(2, dataCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    @TestDuringDevelopment
    public void logEventMissingApplication() {
        EventData[] eventDataArray = new EventData[] {};
        Event evt = new Event("login", APPNAME, eventDataArray);
        auditComponent.logEvent(evt);
    }

    @Test
    @TestDuringDevelopment
    public void notFindAppByName() {
        Assert.assertNull(auditComponent.findAppByName("nonexistent"));
    }

    @Test
    @TestDuringDevelopment
    public void readEventAllDataFields() {
        createDefaultApp();
        long eventId = logDefaultEvent();
        Set<String> dataTypes = new HashSet<String>(2);
        dataTypes.add("host");
        dataTypes.add("cpuLoad");
        EventUi event = auditComponent.readEvent(eventId, dataTypes);
        Assert.assertNotNull(event);
        Assert.assertEquals(eventId, event.getId().longValue());
        Assert.assertEquals("login", event.getName());
        Assert.assertEquals(APPNAME, event.getApplicationName());
        Assert.assertNotNull(event.getSaveTimeStamp());
        Assert.assertNotNull(event.getEventData());
        Assert.assertEquals(2, event.getEventData().size());
        EventData hostData = event.getEventData().get("host");
        Assert.assertEquals("example.org", hostData.getTextValue());
        Assert.assertEquals(EventDataType.STRING, hostData.getEventDataType());
        EventData cpuLoadData = event.getEventData().get("cpuLoad");
        Assert.assertEquals(10.75, cpuLoadData.getNumberValue(), 0.01);
        Assert.assertEquals(EventDataType.NUMBER, cpuLoadData.getEventDataType());
    }

    @Test
    @TestDuringDevelopment
    public void readEventMissingData() {
        createDefaultApp();
        long eventId = logDefaultEvent();
        Set<String> dataTypes = new HashSet<String>(1);
        dataTypes.add("nonexistentDataType");
        EventUi event = auditComponent.readEvent(eventId, dataTypes);
        Assert.assertNotNull(event);
        Assert.assertEquals(eventId, event.getId().longValue());
        Assert.assertEquals("login", event.getName());
        Assert.assertEquals(APPNAME, event.getApplicationName());
        Assert.assertNotNull(event.getSaveTimeStamp());
        Assert.assertNotNull(event.getEventData());
        Assert.assertEquals(0, event.getEventData().size());
    }

    @Test
    @TestDuringDevelopment
    public void readEventOneDataField() {
        createDefaultApp();
        long eventId = logDefaultEvent();
        Set<String> dataTypes = new HashSet<String>(1);
        dataTypes.add("host");
        EventUi event = auditComponent.readEvent(eventId, dataTypes);
        Assert.assertNotNull(event);
        Assert.assertEquals(eventId, event.getId().longValue());
        Assert.assertEquals("login", event.getName());
        Assert.assertEquals(APPNAME, event.getApplicationName());
        Assert.assertNotNull(event.getSaveTimeStamp());
        Assert.assertNotNull(event.getEventData());
        Assert.assertEquals(1, event.getEventData().size());
        EventData hostData = event.getEventData().get("host");
        Assert.assertEquals("example.org", hostData.getTextValue());
        Assert.assertEquals(EventDataType.STRING, hostData.getEventDataType());
    }
}
