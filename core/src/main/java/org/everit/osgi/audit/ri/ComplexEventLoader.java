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

import static org.everit.osgi.audit.ri.AuditComponent.instantToTimestamp;

import java.sql.Connection;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.everit.osgi.audit.api.dto.DataFilter;
import org.everit.osgi.audit.api.dto.EventData;
import org.everit.osgi.audit.api.dto.EventUi;
import org.everit.osgi.audit.api.dto.Operator;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;

import com.mysema.query.Tuple;
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

    // private final LocalizationService localization;

    private final Connection conn;

    private final SQLTemplates sqlTemplates;

    private SQLQuery query;

    private QEvent evtSubqueryAlias;

    private QEventData evtDataSubqueryAlias;

    private final Collection<Long> selectedAppIds;

    private final Collection<Long> selectedEventTypeIds;

    private final Optional<List<String>> dataFields;

    private final Optional<List<DataFilter>> dataFilters;

    private final Instant eventsFrom;

    private final Instant eventsTo;

    private final Locale locale;

    private final long offset;

    private final long limit;

    private final QEventType evtType = new QEventType("evtType");

    private final QApplication app = new QApplication("app");

    public ComplexEventLoader(final Connection conn, final SQLTemplates sqlTemplates,
            // final LocalizationService localizationService,
            final Long[] selectedAppIds,
            final Long[] selectedEventTypeIds,
            final List<String> dataFields,
            final List<DataFilter> dataFilters, final Instant eventsFrom, final Instant eventsTo,
            final Locale locale, final long offset, final long limit) {
        this.conn = Objects.requireNonNull(conn, "conn cannot be null");
        this.sqlTemplates = Objects.requireNonNull(sqlTemplates, "sqlTemplates cannot be null");
        // this.localization = Objects.requireNonNull(localizationService, "localizationService cannot be null");
        this.selectedAppIds = Arrays.asList(Objects.requireNonNull(selectedAppIds, "selectedAppIds cannot be null"));
        this.selectedEventTypeIds = selectedEventTypeIds == null ? null : Arrays.asList(selectedEventTypeIds);
        this.dataFilters = Optional.ofNullable(dataFilters);
        this.dataFields = Optional.ofNullable(dataFields);
        this.eventsFrom = eventsFrom;
        this.eventsTo = eventsTo;
        this.locale = locale;
        this.offset = offset;
        this.limit = limit;
    }

    private void addOrderBy() {
        query = query.orderBy(evtSubqueryAlias.saveTimestamp.desc(), evtSubqueryAlias.eventId.asc());
    }

    private void buildEventDataSubquery() {
        QEventData evtData = new QEventData("evtData");
        SQLSubQuery subQuery = new SQLSubQuery().from(evtData);
        subQuery = subQuery.where(buildEventDataSubqueryPredicate());
        query = query.leftJoin(subQuery.list(
                evtData.eventId,
                // localization.getLocalizedValue(evtData.eventDataName, locale),
                evtData.eventDataName,
                evtData.eventDataType,
                evtData.numberValue,
                evtData.stringValue,
                evtData.textValue,
                evtData.binaryValue,
                evtData.timestampValue), evtDataSubqueryAlias = new QEventData("datas"))
                .on(evtSubqueryAlias.eventId.eq(evtDataSubqueryAlias.eventId));
        // query = query.leftJoin(evtDataSubqueryAlias = evtData)
        // .on(evtSubqueryAlias.eventId.eq(evtDataSubqueryAlias.eventId));
    }

    private BooleanExpression buildEventDataSubqueryPredicate() {
        QEventData evtData = new QEventData("evtData");
        BooleanExpression fieldPredicate = dataFields
                .map((fields) -> evtData.eventDataName.in(fields))
                .orElseGet(() -> Expressions.predicate(Ops.EQ, Expressions.constant(1), Expressions.constant(1)));
        return dataFilters.orElseGet(Collections::emptyList)
                .stream()
                .map(this::buildPredicateForFilter)
                .reduce(fieldPredicate, (pred1, pred2) -> pred1.and(pred2));
    }

    private BooleanExpression buildEventSubqueryPredicate() {
        QEvent evt = new QEvent("evt");
        QEventType evtType = new QEventType("evtType");
        BooleanExpression rval = evtType.applicationId.in(selectedAppIds);
        if (selectedEventTypeIds != null) {
            rval = rval.and(evt.eventTypeId.in(selectedEventTypeIds));
        }
        if (eventsFrom != null && eventsTo != null) {
            rval = rval.and(evt.saveTimestamp.between(instantToTimestamp(eventsFrom), instantToTimestamp(eventsTo)));
        } else if (eventsFrom != null) {
            rval = rval.and(evt.saveTimestamp.gt(instantToTimestamp(eventsFrom)));
        } else if (eventsTo != null) {
            rval = rval.and(evt.saveTimestamp.lt(instantToTimestamp(eventsTo)));
        }
        return rval;
    }

    private void buildFromClause() {
        QEvent evt = new QEvent("evt");
        QEventType evtType = new QEventType("evtType");
        SQLSubQuery subQuery = new SQLSubQuery().from(evt)
                .leftJoin(evtType).on(evt.eventTypeId.eq(evtType.eventTypeId))
                .where(buildEventSubqueryPredicate())
                .offset(offset)
                .limit(limit);
        query = query.from(subQuery.list(evt.eventId, evt.saveTimestamp, evt.eventTypeId),
                evtSubqueryAlias = new QEvent("events"));
        // query = query.from(evtSubqueryAlias = evt);
    }

    private BooleanExpression buildPredicateForFilter(final DataFilter dataFilter) {
        QEventData evtData = new QEventData("evtData");
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
            value = instantToTimestamp(operands.getTimestampValue());
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
        joinAppAndEventType();
        buildEventDataSubquery();
        addOrderBy();
    }

    private void joinAppAndEventType() {
        query = query.leftJoin(evtType).on(evtSubqueryAlias.eventTypeId.eq(evtType.eventTypeId));
        query = query.leftJoin(app).on(evtType.applicationId.eq(app.applicationId));
    }

    public List<EventUi> loadEvents() {
        buildQuery();
        List<Tuple> result = query.list(app.applicationName,
                evtType.name,
                evtSubqueryAlias.eventId,
                evtSubqueryAlias.saveTimestamp,
                evtDataSubqueryAlias.eventDataName,
                evtDataSubqueryAlias.eventDataType,
                evtDataSubqueryAlias.numberValue,
                evtDataSubqueryAlias.stringValue,
                evtDataSubqueryAlias.textValue,
                evtDataSubqueryAlias.timestampValue,
                evtDataSubqueryAlias.binaryValue);
        return new MultipleEventQueryResultMapper(result, evtSubqueryAlias, evtDataSubqueryAlias).mapToEvents();
    }

}
