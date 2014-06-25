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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.everit.osgi.audit.api.dto.EventUi;
import org.everit.osgi.audit.ri.schema.qdsl.QApplication;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEventType;

import com.mysema.query.Tuple;

public class MultipleEventQueryResultMapper {

    private final List<Tuple> rawResult;

    private final QApplication app = QApplication.auditApplication;

    private final QEventType evtType = QEventType.auditEventType;

    private final QEvent evt;

    private final QEventData evtData;

    public MultipleEventQueryResultMapper(final List<Tuple> rawResult, final QEvent evt, final QEventData evtData) {
        this.rawResult = Objects.requireNonNull(rawResult, "rawResult cannot be null");
        this.evt = Objects.requireNonNull(evt, "evt cannot be null");
        this.evtData = Objects.requireNonNull(evtData, "evtData cannot be null");
    }

    List<EventUi> mapToEvents() {
        List<EventUi> rval = new ArrayList<EventUi>();
        Long prevEventId = null;
        EventUi.Builder underConstruction = null;
        EventDataRowMapper rowDataMapper = new EventDataRowMapper(evtData);
        for (Tuple row : rawResult) {
            Long eventId = row.get(evt.eventId);
            if (prevEventId == null || !eventId.equals(prevEventId)) {
                if (underConstruction != null) {
                    rval.add(underConstruction.build());
                }
                underConstruction = new EventUi.Builder()
                .eventId(row.get(evt.eventId))
                .typeName(row.get(evtType.name))
                .appName(row.get(app.applicationName))
                .saveTimestamp(row.get(evt.saveTimestamp));
                prevEventId = eventId;
            }
            rowDataMapper.addEventDataForRow(underConstruction, row);
        }
        return rval;
    }

}
