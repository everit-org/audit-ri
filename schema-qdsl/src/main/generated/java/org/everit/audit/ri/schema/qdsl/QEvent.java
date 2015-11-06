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
package org.everit.audit.ri.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QEvent is a Querydsl query type for QEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QEvent extends com.querydsl.sql.RelationalPathBase<QEvent> {

    private static final long serialVersionUID = -1268724377;

    public static final QEvent event = new QEvent("audit_event");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QEvent> auditEventPk = createPrimaryKey(eventId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QEventType> eventEventTypeIdFk = createForeignKey(eventTypeId, "event_type_id");

        public final com.querydsl.sql.ForeignKey<QEventData> _eventDataEventIdFk = createInvForeignKey(eventId, "event_id");

    }

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> eventId = createNumber("eventId", Long.class);

    public final NumberPath<Long> eventTypeId = createNumber("eventTypeId", Long.class);

    public final DateTimePath<java.sql.Timestamp> occuredAt = createDateTime("occuredAt", java.sql.Timestamp.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QEvent(String variable) {
        super(QEvent.class, forVariable(variable), "org.everit.audit.ri", "audit_event");
        addMetadata();
    }

    public QEvent(String variable, String schema, String table) {
        super(QEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEvent(Path<? extends QEvent> path) {
        super(path.getType(), path.getMetadata(), "org.everit.audit.ri", "audit_event");
        addMetadata();
    }

    public QEvent(PathMetadata metadata) {
        super(QEvent.class, metadata, "org.everit.audit.ri", "audit_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(2).ofType(Types.TIMESTAMP).withSize(23).withDigits(10).notNull());
        addMetadata(eventId, ColumnMetadata.named("event_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(eventTypeId, ColumnMetadata.named("event_type_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(occuredAt, ColumnMetadata.named("occured_at").withIndex(3).ofType(Types.TIMESTAMP).withSize(23).withDigits(10).notNull());
    }

}

