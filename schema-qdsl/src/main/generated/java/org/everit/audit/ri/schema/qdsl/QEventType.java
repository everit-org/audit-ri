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
 * QEventType is a Querydsl query type for QEventType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QEventType extends com.querydsl.sql.RelationalPathBase<QEventType> {

    private static final long serialVersionUID = -754596543;

    public static final QEventType eventType = new QEventType("audit_event_type");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QEventType> auditEventTypePk = createPrimaryKey(eventTypeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QApplication> eventTypeApplicationIdFk = createForeignKey(applicationId, "application_id");

        public final com.querydsl.sql.ForeignKey<org.everit.resource.ri.schema.qdsl.QResource> eventTypeResourceIdFk = createForeignKey(resourceId, "resource_id");

        public final com.querydsl.sql.ForeignKey<QEvent> _eventEventTypeIdFk = createInvForeignKey(eventTypeId, "event_type_id");

    }

    public final NumberPath<Long> applicationId = createNumber("applicationId", Long.class);

    public final NumberPath<Long> eventTypeId = createNumber("eventTypeId", Long.class);

    public final StringPath eventTypeName = createString("eventTypeName");

    public final NumberPath<Long> resourceId = createNumber("resourceId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QEventType(String variable) {
        super(QEventType.class, forVariable(variable), "org.everit.audit.ri", "audit_event_type");
        addMetadata();
    }

    public QEventType(String variable, String schema, String table) {
        super(QEventType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEventType(Path<? extends QEventType> path) {
        super(path.getType(), path.getMetadata(), "org.everit.audit.ri", "audit_event_type");
        addMetadata();
    }

    public QEventType(PathMetadata metadata) {
        super(QEventType.class, metadata, "org.everit.audit.ri", "audit_event_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(applicationId, ColumnMetadata.named("application_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(eventTypeId, ColumnMetadata.named("event_type_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(eventTypeName, ColumnMetadata.named("event_type_name").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(resourceId, ColumnMetadata.named("resource_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

