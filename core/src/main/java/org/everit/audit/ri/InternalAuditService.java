/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.audit.ri;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.everit.audit.dto.AuditEvent;
import org.everit.audit.dto.AuditEventType;
import org.everit.audit.dto.EventData;
import org.everit.audit.ri.authorization.AuditRiAuthorizationManager;
import org.everit.audit.ri.authorization.AuditRiPermissionChecker;
import org.everit.audit.ri.authorization.AuditRiPermissionConstants;
import org.everit.audit.ri.dto.AuditApplication;
import org.everit.audit.ri.props.AuditRiPropertyConstants;
import org.everit.audit.ri.schema.qdsl.QApplication;
import org.everit.audit.ri.schema.qdsl.QEvent;
import org.everit.audit.ri.schema.qdsl.QEventData;
import org.everit.audit.ri.schema.qdsl.QEventType;
import org.everit.authnr.permissionchecker.AuthnrPermissionChecker;
import org.everit.authorization.AuthorizationManager;
import org.everit.persistence.querydsl.support.QuerydslSupport;
import org.everit.props.PropertyManager;
import org.everit.resource.ResourceService;
import org.everit.resource.ri.schema.qdsl.QResource;
import org.everit.transaction.propagator.TransactionPropagator;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.types.Projections;

/**
 * The internal implementation of the audit component.
 */
public class InternalAuditService implements
    AuditApplicationManager,
    InternalAuditEventTypeManager,
    InternalLoggingService,
    AuditRiAuthorizationManager,
    AuditRiPermissionChecker {

  /**
   * The cache key of a cached event type.
   */
  private static class CachedEventTypeKey {

    private final long applicationId;

    private final String eventTypeName;

    CachedEventTypeKey(final long applicationId, final String eventTypeName) {
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

  private static final int SELECT_AUDIT_EVENT_TYPES_PAGE_SIZE = 50;

  private final Map<String, AuditApplication> auditApplicationCache;

  private long auditApplicationTypeTargetResourceId;

  private final Map<CachedEventTypeKey, AuditEventType> auditEventTypeCache;

  private final AuthnrPermissionChecker authnrPermissionChecker;

  private final AuthorizationManager authorizationManager;

  private final PropertyManager propertyManager;

  private final QuerydslSupport querydslSupport;

  private final ResourceService resourceService;

  private final TransactionPropagator transactionPropagator;

  /**
   * Constructor.
   */
  public InternalAuditService(
      final long auditApplicationTypeTargetResourceId,
      final Map<String, AuditApplication> auditApplicationCache,
      final Map<CachedEventTypeKey, AuditEventType> auditEventTypeCache,
      final AuditRequiredServices auditRequiredServices) {
    super();
    this.auditApplicationCache = auditApplicationCache;
    this.auditApplicationTypeTargetResourceId = auditApplicationTypeTargetResourceId;
    this.auditEventTypeCache = auditEventTypeCache;
    authnrPermissionChecker = auditRequiredServices.authnrPermissionChecker;
    authorizationManager = auditRequiredServices.authorizationManager;
    propertyManager = auditRequiredServices.propertyManager;
    querydslSupport = auditRequiredServices.querydslSupport;
    resourceService = auditRequiredServices.resourceService;
    transactionPropagator = auditRequiredServices.transactionPropagator;
    init();
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

  @Override
  public void addPermissionToInitAuditApplication(final long authorizedResourceId) {
    authorizationManager.addPermission(
        authorizedResourceId, auditApplicationTypeTargetResourceId,
        AuditRiPermissionConstants.INIT_AUDIT_APPLICATION);
  }

  @Override
  public void addPermissionToLogToAuditApplication(final long authorizedResourceId,
      final String applicationName) {
    AuditApplication auditApplication = requireAuditApplication(applicationName);
    authorizationManager.addPermission(
        authorizedResourceId, auditApplication.resourceId,
        AuditRiPermissionConstants.LOG_TO_AUDIT_APPLICATION);
  }

  private void cacheAuditApplication(final AuditApplication auditApplication) {
    auditApplicationCache.put(auditApplication.applicationName, new AuditApplication(
        auditApplication));
  }

  private void cacheAuditEventType(final long applicationId, final AuditEventType auditEventType) {
    auditEventTypeCache.put(
        new CachedEventTypeKey(applicationId, auditEventType.eventTypeName),
        new AuditEventType(auditEventType));
  }

  private void cacheAuditEventTypes(final long applicationId,
      final List<AuditEventType> auditEventTypes) {
    for (AuditEventType auditEventType : auditEventTypes) {
      cacheAuditEventType(applicationId, auditEventType);
    }
  }

  private void checkPermissionToInitAuditApplication() {
    authnrPermissionChecker.checkPermission(auditApplicationTypeTargetResourceId,
        AuditRiPermissionConstants.INIT_AUDIT_APPLICATION);
  }

  private void checkPermissionToLogToAuditApplication(final long auditApplicationResourceId) {
    authnrPermissionChecker.checkPermission(
        auditApplicationResourceId, AuditRiPermissionConstants.LOG_TO_AUDIT_APPLICATION);
  }

  private AuditApplication getAuditApplication(final String applicationName) {

    AuditApplication cachedAuditApplication = auditApplicationCache.get(applicationName);
    if (cachedAuditApplication != null) {
      return new AuditApplication(cachedAuditApplication);
    }

    AuditApplication auditApplication = selectAuditApplication(applicationName);

    if (auditApplication != null) {
      cacheAuditApplication(auditApplication);
    }

    return auditApplication;
  }

  @Override
  public long getAuditApplicationTypeTargetResourceId() {
    return auditApplicationTypeTargetResourceId;
  }

  private List<String> getNonCachedAuditEventTypeNames(final long applicationId,
      final List<String> eventTypeNames) {

    List<String> rval = new ArrayList<>();

    for (String eventTypeName : eventTypeNames) {

      AuditEventType cachedAuditEventType = auditEventTypeCache.get(
          new CachedEventTypeKey(applicationId, eventTypeName));

      if (cachedAuditEventType == null) {
        rval.add(eventTypeName);
      }

    }

    return rval;
  }

  private List<String> getNonCachedAuditEventTypeNames(final long applicationId,
      final String[] eventTypeNames) {

    List<String> rval = new ArrayList<>();

    for (String eventTypeName : eventTypeNames) {

      AuditEventType cachedAuditEventType = auditEventTypeCache.get(
          new CachedEventTypeKey(applicationId, eventTypeName));

      if (cachedAuditEventType == null) {
        rval.add(eventTypeName);
      }

    }

    return rval;
  }

  private List<String> getNonExistentAuditEventTypeNames(final AuditApplication auditApplication,
      final List<String> nonCachedAuditEventTypeNames) {

    List<AuditEventType> selectedAuditEventTypes = selectAuditEventTypes(
        auditApplication.applicationName,
        nonCachedAuditEventTypeNames);

    cacheAuditEventTypes(auditApplication.applicationId, selectedAuditEventTypes);

    return getNonCachedAuditEventTypeNames(auditApplication.applicationId,
        nonCachedAuditEventTypeNames);
  }

  @Override
  public boolean hasPermissionToInitAuditApplication() {
    return authnrPermissionChecker.hasPermission(auditApplicationTypeTargetResourceId,
        AuditRiPermissionConstants.INIT_AUDIT_APPLICATION);
  }

  @Override
  public boolean hasPermissionToLogToAuditApplication(final String applicationName) {

    Objects.requireNonNull(applicationName, "applicationName cannot be null");

    AuditApplication auditApplication = requireAuditApplication(applicationName);

    return authnrPermissionChecker.hasPermission(
        auditApplication.resourceId, AuditRiPermissionConstants.LOG_TO_AUDIT_APPLICATION);
  }

  /**
   * Initializes the target resource ID used for the permission checks according to the audit
   * application type.
   */
  private void init() {

    transactionPropagator.required(() -> {

      String auditApplicationTargetResourceIdString =
          propertyManager
              .getProperty(AuditRiPropertyConstants.AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID);

      if (auditApplicationTargetResourceIdString == null) {

        auditApplicationTypeTargetResourceId = resourceService.createResource();
        propertyManager.addProperty(
            AuditRiPropertyConstants.AUDIT_APPLICATION_TYPE_TARGET_RESOURCE_ID,
            String.valueOf(auditApplicationTypeTargetResourceId));

      } else {
        auditApplicationTypeTargetResourceId = Long
            .parseLong(auditApplicationTargetResourceIdString);
      }

      return null;
    });
  }

  @Override
  public void initAuditApplication(final String applicationName) {

    Objects.requireNonNull(applicationName, "applicationName cannot be null");

    checkPermissionToInitAuditApplication();

    if (getAuditApplication(applicationName) != null) {
      return;
    }

    transactionPropagator.required(() -> {

      lockAuditApplicationTypeTargetResourceId();

      if (getAuditApplication(applicationName) != null) {
        return null;
      }

      AuditApplication auditApplication = insertAuditApplication(applicationName);

      cacheAuditApplication(auditApplication);

      return null;
    });
  }

  private AuditEventType initAuditEventType(final AuditApplication auditApplication,
      final String eventTypeName) {

    AuditEventType auditEventType = auditEventTypeCache
        .get(new CachedEventTypeKey(auditApplication.applicationId, eventTypeName));

    if (auditEventType != null) {
      return auditEventType;
    }

    initAuditEventTypes(auditApplication.applicationName, eventTypeName);

    return auditEventTypeCache
        .get(new CachedEventTypeKey(auditApplication.applicationId, eventTypeName));

  }

  @Override
  public void initAuditEventTypes(final String applicationName, final String... eventTypeNames) {

    Objects.requireNonNull(applicationName, "applicationName cannot be null");
    Objects.requireNonNull(eventTypeNames, "eventTypeNames cannot be null");
    requireNotContainsNull(eventTypeNames);

    if (eventTypeNames.length == 0) {
      return;
    }

    AuditApplication auditApplication = requireAuditApplication(applicationName);

    checkPermissionToLogToAuditApplication(auditApplication.resourceId);

    // check cache
    List<String> nonCachedAuditEventTypeNames =
        getNonCachedAuditEventTypeNames(auditApplication.applicationId, eventTypeNames);

    if (nonCachedAuditEventTypeNames.isEmpty()) {
      return;
    }

    // select db and cache existent values
    List<String> nonExistentEventTypeNames =
        getNonExistentAuditEventTypeNames(auditApplication, nonCachedAuditEventTypeNames);

    if (nonExistentEventTypeNames.isEmpty()) {
      return;
    }

    transactionPropagator.required(() -> {

      lockAuditApplication(auditApplication.applicationId);

      // double check cache
      List<String> nonCachedAuditEventTypeNames2 =
          getNonCachedAuditEventTypeNames(auditApplication.applicationId, eventTypeNames);

      if (nonCachedAuditEventTypeNames2.isEmpty()) {
        return null;
      }

      // select db again and cache existent values
      List<String> nonExistentEventTypeNames2 =
          getNonExistentAuditEventTypeNames(auditApplication, nonCachedAuditEventTypeNames2);

      if (nonExistentEventTypeNames2.isEmpty()) {
        return null;
      }

      // insert non-existent values
      return insertAndCacheAuditEventTypes(auditApplication, nonExistentEventTypeNames2);

    });
  }

  /**
   * Note: transaction must be provided to this method.
   */
  private Void insertAndCacheAuditEventTypes(final AuditApplication auditApplication,
      final List<String> eventTypeNames) {
    return querydslSupport.execute((connection, configuration) -> {

      for (String eventTypeName : eventTypeNames) {

        Long resourceId = resourceService.createResource();

        QEventType qEventType = QEventType.eventType;

        long eventTypeId = new SQLInsertClause(connection, configuration, qEventType)
            .set(qEventType.eventTypeName, eventTypeName)
            .set(qEventType.applicationId, auditApplication.applicationId)
            .set(qEventType.resourceId, resourceId)
            .executeWithKey(qEventType.eventTypeId);

        AuditEventType auditEventType = new AuditEventType.Builder()
            .eventTypeId(eventTypeId)
            .eventTypeName(eventTypeName)
            .resourceId(resourceId)
            .build();

        cacheAuditEventType(auditApplication.applicationId, auditEventType);
      }

      return null;
    });
  }

  /**
   * Note: transaction must be provided to this method.
   */
  private AuditApplication insertAuditApplication(final String applicationName) {

    return querydslSupport.execute((connection, configuration) -> {

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
  }

  /**
   * Note: transaction must be provided to this method.
   */
  private void insertAuditEvent(final long eventTypeId, final AuditEvent auditEvent) {

    querydslSupport.execute((connection, configuration) -> {

      QEvent qEvent = QEvent.event;

      long eventId = new SQLInsertClause(connection, configuration, qEvent)
          .set(qEvent.createdAt, Timestamp.from(Instant.now()))
          .set(qEvent.occuredAt, Timestamp.from(auditEvent.occuredAt))
          .set(qEvent.eventTypeId, eventTypeId)
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
  }

  /**
   * Note: transaction must be provided to this method.
   */
  private void lockAuditApplication(final long applicationId) {
    querydslSupport.execute((connection, configuration) -> {
      QApplication qApplication = QApplication.application;
      return new SQLQuery(connection, configuration)
          .from(qApplication)
          .where(qApplication.applicationId.eq(applicationId))
          .forUpdate();
    });
  }

  /**
   * Note: transaction must be provided to this method.
   */
  private void lockAuditApplicationTypeTargetResourceId() {
    querydslSupport.execute((connection, configuration) -> {
      QResource qResource = QResource.resource;
      return new SQLQuery(connection, configuration)
          .from(qResource)
          .where(qResource.resourceId.eq(auditApplicationTypeTargetResourceId))
          .forUpdate();
    });
  }

  @Override
  public void logEvent(final String applicationName, final AuditEvent auditEvent) {

    Objects.requireNonNull(applicationName, "applicationName cannot be null");
    Objects.requireNonNull(auditEvent, "auditEvent cannot be null");

    AuditApplication auditApplication = requireAuditApplication(applicationName);

    checkPermissionToLogToAuditApplication(auditApplication.resourceId);

    transactionPropagator
        .required(() -> {

          AuditEventType auditEventType = initAuditEventType(auditApplication,
              auditEvent.eventTypeName);

          insertAuditEvent(auditEventType.eventTypeId, auditEvent);

          return null;
        });
  }

  @Override
  public void removePermissionInitAuditApplication(final long authorizedResourceId) {
    authorizationManager.removePermission(
        authorizedResourceId, auditApplicationTypeTargetResourceId,
        AuditRiPermissionConstants.INIT_AUDIT_APPLICATION);
  }

  @Override
  public void removePermissionLogToAuditApplication(final long authorizedResourceId,
      final String applicationName) {
    AuditApplication auditApplication = requireAuditApplication(applicationName);
    authorizationManager.removePermission(
        authorizedResourceId, auditApplication.resourceId,
        AuditRiPermissionConstants.LOG_TO_AUDIT_APPLICATION);
  }

  private AuditApplication requireAuditApplication(final String applicationName) {
    return Optional
        .ofNullable(getAuditApplication(applicationName))
        .orElseThrow(() -> new UnknownAuditApplicationException(applicationName));
  }

  private void requireNotContainsNull(final String... eventTypeNames) {
    for (String eventTypeName : eventTypeNames) {
      if (eventTypeName == null) {
        throw new NullPointerException("eventTypeNames cannot contain null value");
      }
    }
  }

  private AuditApplication selectAuditApplication(final String applicationName) {
    return querydslSupport.execute((connection, configuration) -> {

      QApplication qApplication = QApplication.application;

      return new SQLQuery(connection, configuration)
          .from(qApplication)
          .where(qApplication.applicationName.eq(applicationName))
          .uniqueResult(Projections.fields(AuditApplication.class,
              qApplication.applicationId,
              qApplication.applicationName,
              qApplication.resourceId));
    });
  }

  private List<AuditEventType> selectAuditEventTypes(final String applicationName,
      final List<String> eventTypeNames) {

    return querydslSupport.execute((connection, configuration) -> {

      QEventType qEventType = QEventType.eventType;
      QApplication qApplication = QApplication.application;

      List<AuditEventType> rval = new ArrayList<>();
      int numberOfEventTypeNames = eventTypeNames.size();

      for (int fromIndex = 0; fromIndex < numberOfEventTypeNames; fromIndex = fromIndex
          + SELECT_AUDIT_EVENT_TYPES_PAGE_SIZE) {

        int toIndex = fromIndex + SELECT_AUDIT_EVENT_TYPES_PAGE_SIZE;
        if (toIndex > numberOfEventTypeNames) {
          toIndex = numberOfEventTypeNames;
        }

        List<String> actualEventTypeNames = new ArrayList<>(eventTypeNames.subList(fromIndex,
            toIndex));

        List<AuditEventType> auditEventTypes = new SQLQuery(connection, configuration)
            .from(qEventType)
            .innerJoin(qApplication).on(qEventType.applicationId.eq(qApplication.applicationId))
            .where(qApplication.applicationName.eq(applicationName)
                .and(qEventType.eventTypeName.in(actualEventTypeNames)))
            .list(Projections.fields(AuditEventType.class,
                qEventType.eventTypeId,
                qEventType.eventTypeName,
                qEventType.resourceId));
        rval.addAll(auditEventTypes);
      }

      return rval;
    });
  }

}
