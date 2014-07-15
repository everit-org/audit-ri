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
import java.util.List;
import java.util.Objects;

import org.everit.osgi.audit.dto.EventUi;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.types.expr.BooleanExpression;

public class SingleEventLoader {

    private final QApplication app = new QApplication("app");

    private final QEventType evtType = new QEventType("evtType");

    private final QEvent evt = new QEvent("evt");

    private QEventData evtData = new QEventData("evtData");

    private final Connection conn;

    private final SQLTemplates sqlTemplates;

    private SQLQuery query;

    public SingleEventLoader(final Connection conn, final SQLTemplates sqlTemplates) {
        this.conn = Objects.requireNonNull(conn, "conn cannot be null");
        this.sqlTemplates = Objects.requireNonNull(sqlTemplates, "sqlTemplates cannot be null");
    }

    private QEventData addFilteredEventDataSubquery(final BooleanExpression eventDataPred) {
        QEventData evtDataAlias = new QEventData("datas");
        query.leftJoin(new SQLSubQuery()
        .from(evtData)
        .where(eventDataPred)
        .list(evtData.eventId,
                evtData.eventDataName,
                evtData.eventDataType,
                evtData.numberValue,
                evtData.stringValue,
                evtData.textValue,
                evtData.timestampValue,
                evtData.binaryValue), evtDataAlias).on(evt.eventId.eq(evtDataAlias.eventId));
        return evtDataAlias;
    }

    private void constructBaseQuery() {
        query = new SQLQuery(conn, sqlTemplates)
        .from(evt)
        .join(evtType).on(evt.eventTypeId.eq(evtType.eventTypeId))
        .join(app).on(app.applicationId.eq(evtType.applicationId));
    }

    private void constructWhereCondition(final BooleanExpression additionalPred,
            final long eventId) {
        BooleanExpression eventIdPred = evt.eventId.eq(eventId);
        if (additionalPred == null) {
            query.where(eventIdPred);
        } else {
            query.where(additionalPred.and(eventIdPred));
        }
    }

    private void dumpSQL() {
        System.err.println(
                query.getSQL(app.applicationName,
                        evtType.name,
                        evt.eventId,
                        evt.saveTimestamp,
                        evtData.eventDataName,
                        evtData.eventDataType,
                        evtData.numberValue,
                        evtData.stringValue,
                        evtData.textValue,
                        evtData.timestampValue,
                        evtData.binaryValue).getSQL());
    }

    private void joinEventData(final BooleanExpression eventDataPred) {
        if (eventDataPred == null) {
            query.leftJoin(evtData).on(evt.eventId.eq(evtData.eventId));
        } else {
            evtData = addFilteredEventDataSubquery(eventDataPred);
        }
    }

    public EventUi loadEvent(final long eventId) {
        return new SingleEventQueryResultMapper(singleEventQuery(eventId, null, null), evtData, evt, evtType, app)
                .mapToEvent();
    }

    public EventUi loadEvent(final long eventId, final BooleanExpression eventDataPred) {
        return new SingleEventQueryResultMapper(singleEventQuery(eventId, null, eventDataPred),
                evtData, evt, evtType, app).mapToEvent();
    }

    private List<Tuple> singleEventQuery(final long eventId, final BooleanExpression additionalPred,
            final BooleanExpression eventDataPred) {
        constructBaseQuery();
        joinEventData(eventDataPred);
        constructWhereCondition(additionalPred, eventId);
        // dumpSQL();
        return query.list(app.applicationName,
                evtType.name,
                evt.eventId,
                evt.saveTimestamp,
                evtData.eventDataName,
                evtData.eventDataType,
                evtData.numberValue,
                evtData.stringValue,
                evtData.textValue,
                evtData.timestampValue,
                evtData.binaryValue);
    }

}
