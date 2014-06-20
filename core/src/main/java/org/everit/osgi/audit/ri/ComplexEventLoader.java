package org.everit.osgi.audit.ri;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.everit.osgi.audit.api.dto.DataFilter;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.types.expr.BooleanExpression;

public class ComplexEventLoader {

    private final Connection conn;

    private final SQLTemplates sqlTemplates;

    private SQLQuery query;

    private QEvent evtSubqueryAlias;

    private QEventData evtDataSubqueryAlias;

    private final Collection<Long> selectedAppIds;

    private final Collection<Long> selectedEventTypeIds;

    private final List<String> dataFields;

    private final List<DataFilter> dataFilters;

    private final Calendar eventsFrom;

    private final Calendar eventsTo;

    private final Locale locale;

    private final long offset;

    private final long limit;

    public ComplexEventLoader(final Connection conn, final SQLTemplates sqlTemplates, final Long[] selectedAppIds,
            final Long[] selectedEventTypeIds,
            final List<String> dataFields,
            final List<DataFilter> dataFilters, final Calendar eventsFrom, final Calendar eventsTo,
            final Locale locale, final long offset, final long limit) {
        this.conn = Objects.requireNonNull(conn, "conn cannot be null");
        this.sqlTemplates = Objects.requireNonNull(sqlTemplates, "sqlTemplates cannot be null");
        this.selectedAppIds = Arrays.asList(Objects.requireNonNull(selectedAppIds, "selectedAppIds cannot be null"));
        this.selectedEventTypeIds = selectedEventTypeIds == null ? null : Arrays.asList(selectedEventTypeIds);
        this.dataFilters = dataFilters;
        this.dataFields = dataFields;
        this.eventsFrom = eventsFrom;
        this.eventsTo = eventsTo;
        this.locale = locale;
        this.offset = offset;
        this.limit = limit;
    }

    private Timestamp asTimestamp(final Calendar cal) {
        return new Timestamp(cal.getTimeInMillis());
    }

    private void buildEventDataSubquery() {
        QEventData evtData = QEventData.auditEventData;
        SQLSubQuery subQuery = new SQLSubQuery().from(evtData);
        if (dataFields != null) {
            subQuery = subQuery.where(evtData.eventDataName.in(dataFields));
            // TODO filters
        }
        query.leftJoin(subQuery.list(), evtDataSubqueryAlias = new QEventData("datas"))
                .on(evtSubqueryAlias.eventId.eq(evtDataSubqueryAlias.eventId));
    }

    private BooleanExpression buildEventSubqueryPredicate() {
        QEvent evt = QEvent.auditEvent;
        QEventType evtType = QEventType.auditEventType;
        BooleanExpression rval = evtType.applicationId.in(selectedAppIds);
        if (selectedEventTypeIds != null) {
            rval = rval.and(evtType.eventTypeId.in(selectedEventTypeIds));
        }
        if (eventsFrom != null && eventsTo != null) {
            rval = rval.and(evt.saveTimestamp.between(asTimestamp(eventsFrom), asTimestamp(eventsTo)));
        } else if (eventsFrom != null) {
            rval = rval.and(evt.saveTimestamp.gt(asTimestamp(eventsFrom)));
        } else if (eventsTo != null) {
            rval = rval.and(evt.saveTimestamp.lt(asTimestamp(eventsTo)));
        }
        return rval;
    }

    private void buildFromClause() {
        QEvent evt = QEvent.auditEvent;
        QEventType evtType = QEventType.auditEventType;
        SQLSubQuery subQuery = new SQLSubQuery().from(evt)
                .join(evtType).on(evt.eventTypeId.eq(evtType.eventTypeId))
                .where(buildEventSubqueryPredicate())
                .offset(offset)
                .limit(limit);
        query.from(subQuery.list(), evtSubqueryAlias = new QEvent("events"));
    }

    private void buildQuery() {
        query = new SQLQuery(conn, sqlTemplates);
        buildFromClause();
        buildEventDataSubquery();
    }

}
