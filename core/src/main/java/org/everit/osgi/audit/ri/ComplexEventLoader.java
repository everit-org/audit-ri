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
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.everit.osgi.audit.api.dto.DataFilter;
import org.everit.osgi.audit.api.dto.EventData;
import org.everit.osgi.audit.api.dto.Operator;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.support.Expressions;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Ops;
import com.mysema.query.types.expr.BooleanExpression;

public class ComplexEventLoader {

    private static final Map<Operator, com.mysema.query.types.Operator<Boolean>> operatorMapping = new HashMap<>();

    static {
        operatorMapping.put(Operator.EQ, Ops.EQ);
        operatorMapping.put(Operator.LT, Ops.LT);
        operatorMapping.put(Operator.GT, Ops.GT);
        operatorMapping.put(Operator.STARTS_WITH, Ops.STARTS_WITH);
    }

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
        subQuery = subQuery.where(buildEventDataSubqueryPredicate());
        query.leftJoin(subQuery.list(), evtDataSubqueryAlias = new QEventData("datas"))
        .on(evtSubqueryAlias.eventId.eq(evtDataSubqueryAlias.eventId));
    }

    private BooleanExpression buildEventDataSubqueryPredicate() {
        QEventData evtData = QEventData.auditEventData;
        BooleanExpression predicate = Expressions.predicate(Ops.EQ, Expressions.constant(1), Expressions.constant(1));
        if (dataFields != null) {
            predicate = predicate.and(evtData.eventDataName.in(dataFields));
        }
        for (DataFilter dataFilter : dataFilters) {
            predicate = predicate.and(buildPredicateForFilter(dataFilter));
        }
        return predicate;
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

    private BooleanExpression buildPredicateForFilter(final DataFilter dataFilter) {
        QEventData evtData = QEventData.auditEventData;
        BooleanExpression pred = null;
        Expression<?> field;
        Object value;
        EventData operands = dataFilter.getOperands();
        switch (operands.getEventDataType()) {
        case NUMBER:
            field = evtData.numberValue;
            value = operands.getNumberValue();
            break;
        case STRING:
            field = evtData.stringValue;
            value = operands.getTextValue();
            break;
        case TEXT:
            field = evtData.textValue;
            value = operands.getTextValue();
            break;
        case TIMESTAMP:
            field = evtData.timestampValue;
            value = new Timestamp(operands.getTimestampValue().getTimeInMillis());
            break;
        case BINARY:
        default:
            throw new IllegalArgumentException("unsupported operator: " + dataFilter.getOperator());
        }
        pred = Expressions.predicate(operatorMapping.get(dataFilter.getOperator()), field, Expressions.constant(value));
        return evtData.eventDataName.ne(operands.getName()).or(pred);
    }

    private void buildQuery() {
        query = new SQLQuery(conn, sqlTemplates);
        buildFromClause();
        buildEventDataSubquery();
    }

}
