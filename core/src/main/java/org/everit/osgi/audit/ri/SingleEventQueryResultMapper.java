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

import static org.everit.osgi.audit.ri.AuditComponent.timestampToInstant;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.everit.osgi.audit.dto.EventUi;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;

import com.mysema.query.Tuple;

public class SingleEventQueryResultMapper {

    private final QApplication app;

    private final QEventType evtType;

    private final QEvent evt;

    private final QEventData evtDataAlias;

    private final List<Tuple> result;

    public SingleEventQueryResultMapper(final List<Tuple> result,
            final QEventData evtDataAlias,
            final QEvent evt,
            final QEventType evtType,
            final QApplication app) {
        this.result = Objects.requireNonNull(result, "result cannot be null");
        this.evtDataAlias = Objects.requireNonNull(evtDataAlias, "evtDataAlias cannot be null");
        this.evt = Objects.requireNonNull(evt, "evt cannot be null");
        this.evtType = Objects.requireNonNull(evtType, "evtType cannot be null");
        this.app = Objects.requireNonNull(app, "app cannot be null");
    }

    public EventUi mapToEvent() {
        Iterator<Tuple> resultIt = result.iterator();
        if (!resultIt.hasNext()) {
            return null;
        }
        Tuple firstRow = resultIt.next();
        EventUi.Builder builder = new EventUi.Builder()
                .eventId(firstRow.get(evt.eventId))
                .typeName(firstRow.get(evtType.name))
                .appName(firstRow.get(app.applicationName))
                .saveTimestamp(timestampToInstant(firstRow.get(evt.saveTimestamp)));
        if (firstRow.get(evtDataAlias.eventDataType) == null) { // no event data belongs to the event
            return builder.build();
        }
        EventDataRowMapper rowDataMapper = new EventDataRowMapper(evtDataAlias);
        rowDataMapper.addEventDataForRow(builder, firstRow);
        while (resultIt.hasNext()) {
            Tuple row = resultIt.next();
            rowDataMapper.addEventDataForRow(builder, row);
        }
        return builder.build();
    }

}
