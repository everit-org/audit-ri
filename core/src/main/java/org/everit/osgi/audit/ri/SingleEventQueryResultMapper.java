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

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.everit.osgi.audit.api.dto.EventDataType;
import org.everit.osgi.audit.api.dto.EventUi;
import org.everit.osgi.audit.api.dto.EventUi.Builder;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;

import com.mysema.query.Tuple;

public class SingleEventQueryResultMapper {

    private static final QApplication app = QApplication.auditApplication;

    private static final QEventType evtType = QEventType.auditEventType;

    private static final QEvent evt = QEvent.auditEvent;

    private static final QEventData evtData = QEventData.auditEventData;

    private final List<Tuple> result;

    public SingleEventQueryResultMapper(final List<Tuple> result) {
        this.result = result;
    }

    private void addBlobData(final Builder builder, final String dataName, final Tuple row) {
        Blob blob = row.get(evtData.binaryValue);
        try {
            try {
                builder.binaryData(dataName, blob.getBytes(0, (int) blob.length()));
            } finally {
                blob.free();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addEventDataForRow(final Builder builder, final Tuple row) {
        Objects.requireNonNull(builder, "builder cannot be null");
        Objects.requireNonNull(row, "row cannot be null");
        String type = row.get(evtData.eventDataType);
        if (type == null) {
            throw new IllegalArgumentException("row has null value for eventData.eventDataType");
        }
        String dataName = row.get(evtData.eventDataName);
        if (type.equals(EventDataType.BINARY.toString())) {
            addBlobData(builder, dataName, row);
        } else if (type.equals(EventDataType.STRING.toString())) {
            builder.stringData(dataName, row.get(evtData.stringValue));
        } else if (type.equals(EventDataType.TEXT.toString())) {
            builder.textData(dataName, row.get(evtData.textValue));
        } else if (type.equals(EventDataType.NUMBER.toString())) {
            builder.numberData(dataName, row.get(evtData.numberValue));
        } else if (type.equals(EventDataType.TIMESTAMP)) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(row.get(evtData.timestampValue).getTime());
            builder.timestampData(dataName, calendar);
        } else {
            throw new IllegalStateException("unknown event data type: " + type);
        }
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
        .saveTimestamp(firstRow.get(evt.saveTimestamp));
        if (firstRow.get(evtData.eventDataType) == null) {
            return builder.build();
        }
        addEventDataForRow(builder, firstRow);
        while (resultIt.hasNext()) {
            Tuple row = resultIt.next();
            addEventDataForRow(builder, row);
        }
        return builder.build();
    }

}
