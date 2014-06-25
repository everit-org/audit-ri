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
import java.util.Objects;

import org.everit.osgi.audit.api.dto.EventDataType;
import org.everit.osgi.audit.api.dto.EventUi.Builder;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;

import com.mysema.query.Tuple;

public class EventDataRowMapper {

    private final QEventData evtDataAlias;

    public EventDataRowMapper(final QEventData evtDataAlias) {
        this.evtDataAlias = evtDataAlias;
    }

    private void addBlobData(final Builder builder, final String dataName, final Tuple row) {
        Blob blob = row.get(evtDataAlias.binaryValue);
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

    void addEventDataForRow(final Builder builder, final Tuple row) {
        Objects.requireNonNull(builder, "builder cannot be null");
        Objects.requireNonNull(row, "row cannot be null");
        String type = row.get(evtDataAlias.eventDataType);
        if (type == null) {
            throw new IllegalArgumentException("row has null value for eventData.eventDataType");
        }
        String dataName = row.get(evtDataAlias.eventDataName);
        if (type.equals(EventDataType.BINARY.toString())) {
            addBlobData(builder, dataName, row);
        } else if (type.equals(EventDataType.STRING.toString())) {
            builder.stringData(dataName, row.get(evtDataAlias.stringValue));
        } else if (type.equals(EventDataType.TEXT.toString())) {
            builder.textData(dataName, row.get(evtDataAlias.textValue));
        } else if (type.equals(EventDataType.NUMBER.toString())) {
            builder.numberData(dataName, row.get(evtDataAlias.numberValue));
        } else if (type.equals(EventDataType.TIMESTAMP)) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(row.get(evtDataAlias.timestampValue).getTime());
            builder.timestampData(dataName, calendar);
        } else {
            throw new IllegalStateException("unknown event data type: " + type);
        }
    }

}
