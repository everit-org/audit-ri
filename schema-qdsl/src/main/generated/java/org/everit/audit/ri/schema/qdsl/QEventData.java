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
 * QEventData is a Querydsl query type for QEventData
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QEventData extends com.querydsl.sql.RelationalPathBase<QEventData> {

    private static final long serialVersionUID = -755096143;

    public static final QEventData eventData = new QEventData("audit_event_data");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QEventData> auditEventDataPk = createPrimaryKey(eventDataId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QEvent> eventDataEventIdFk = createForeignKey(eventId, "event_id");

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
        super(QEventData.class, forVariable(variable), "org.everit.audit.ri", "audit_event_data");
        addMetadata();
    }

    public QEventData(String variable, String schema, String table) {
        super(QEventData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEventData(Path<? extends QEventData> path) {
        super(path.getType(), path.getMetadata(), "org.everit.audit.ri", "audit_event_data");
        addMetadata();
    }

    public QEventData(PathMetadata metadata) {
        super(QEventData.class, metadata, "org.everit.audit.ri", "audit_event_data");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(eventDataId, ColumnMetadata.named("event_data_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(eventDataName, ColumnMetadata.named("event_data_name").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(eventDataType, ColumnMetadata.named("event_data_type").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(eventId, ColumnMetadata.named("event_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(numberValue, ColumnMetadata.named("number_value").withIndex(6).ofType(Types.DOUBLE).withSize(17));
        addMetadata(stringValue, ColumnMetadata.named("string_value").withIndex(5).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(textValue, ColumnMetadata.named("text_value").withIndex(7).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(timestampValue, ColumnMetadata.named("timestamp_value").withIndex(8).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
    }

}

