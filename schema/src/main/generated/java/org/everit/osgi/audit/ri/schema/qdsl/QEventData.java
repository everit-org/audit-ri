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
package org.everit.osgi.audit.ri.schema.qdsl;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QEventData is a Querydsl query type for QEventData
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEventData extends com.mysema.query.sql.RelationalPathBase<QEventData> {

    private static final long serialVersionUID = 1723880031;

    public static final QEventData eventData = new QEventData("audit_event_data");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QEventData> auditEventDataPk = createPrimaryKey(eventDataId);

    }

    public class ForeignKeys {

        public final com.mysema.query.sql.ForeignKey<QEvent> eventDataEventIdFk = createForeignKey(eventId, "event_id");

    }

    public final NumberPath<Long> eventDataId = createNumber("eventDataId", Long.class);

    public final StringPath eventDataName = createString("eventDataName");

    public final StringPath eventDataType = createString("eventDataType");

    public final NumberPath<Long> eventId = createNumber("eventId", Long.class);

    public final NumberPath<Double> numberValue = createNumber("numberValue", Double.class);

    public final StringPath stringValue = createString("stringValue");

    public final StringPath textValue = createString("textValue");

    public final DateTimePath<java.sql.Timestamp> timestampValue = createDateTime("timestampValue", java.sql.Timestamp.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QEventData(String variable) {
        super(QEventData.class, forVariable(variable), "org.everit.osgi.audit.ri", "audit_event_data");
        addMetadata();
    }

    public QEventData(String variable, String schema, String table) {
        super(QEventData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEventData(Path<? extends QEventData> path) {
        super(path.getType(), path.getMetadata(), "org.everit.osgi.audit.ri", "audit_event_data");
        addMetadata();
    }

    public QEventData(PathMetadata<?> metadata) {
        super(QEventData.class, metadata, "org.everit.osgi.audit.ri", "audit_event_data");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(eventDataId, ColumnMetadata.named("event_data_id").ofType(-5).withSize(19).notNull());
        addMetadata(eventDataName, ColumnMetadata.named("event_data_name").ofType(12).withSize(255).notNull());
        addMetadata(eventDataType, ColumnMetadata.named("event_data_type").ofType(12).withSize(32).notNull());
        addMetadata(eventId, ColumnMetadata.named("event_id").ofType(-5).withSize(19).notNull());
        addMetadata(numberValue, ColumnMetadata.named("number_value").ofType(8).withSize(17));
        addMetadata(stringValue, ColumnMetadata.named("string_value").ofType(12).withSize(2000));
        addMetadata(textValue, ColumnMetadata.named("text_value").ofType(2005).withSize(2147483647));
        addMetadata(timestampValue, ColumnMetadata.named("timestamp_value").ofType(93).withSize(23).withDigits(10));
    }

}

