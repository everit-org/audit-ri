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
package org.everit.osgi.audit.ri.internal;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.function.Supplier;

import javax.sql.rowset.serial.SerialBlob;

import org.everit.osgi.audit.dto.AuditEvent;
import org.everit.osgi.audit.dto.EventData;
import org.everit.osgi.audit.ri.schema.qdsl.QEvent;
import org.everit.osgi.audit.ri.schema.qdsl.QEventData;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.transaction.helper.api.TransactionHelper;

import com.mysema.query.sql.dml.SQLInsertClause;

public class AuditEventPersister implements Supplier<Void> {

    private final QuerydslSupport querydslSupport;

    private final AuditEvent auditEvent;

    private final long eventTypeId;

    private final TransactionHelper transactionHelper;

    public AuditEventPersister(final TransactionHelper transactionHelper, final QuerydslSupport querydslSupport,
            final long eventTypeId, final AuditEvent auditEvent) {
        this.transactionHelper = transactionHelper;
        this.querydslSupport = querydslSupport;
        this.eventTypeId = eventTypeId;
        this.auditEvent = auditEvent;
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
        case BINARY:
            try {
                insert.set(qEventData.binaryValue, new SerialBlob(eventData.binaryValue));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            break;
        case TIMESTAMP:
            insert.set(qEventData.timestampValue, Timestamp.from(eventData.timestampValue));
            break;
        }
    }

    @Override
    public Void get() {
        return transactionHelper.required(() -> {
            return querydslSupport.execute((connection, configuration) -> {

                QEvent qEvent = QEvent.event;

                long eventId = new SQLInsertClause(connection, configuration, qEvent)
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
        });
    }

}
