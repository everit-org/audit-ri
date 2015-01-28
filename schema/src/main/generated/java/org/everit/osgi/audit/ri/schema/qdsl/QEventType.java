/**
 * This file is part of org.everit.osgi.audit.ri.schema.
 *
 * org.everit.osgi.audit.ri.schema is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * org.everit.osgi.audit.ri.schema is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with org.everit.osgi.audit.ri.schema.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.audit.ri.schema.qdsl;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QEventType is a Querydsl query type for QEventType
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEventType extends com.mysema.query.sql.RelationalPathBase<QEventType> {

    private static final long serialVersionUID = 1724379631;

    public static final QEventType eventType = new QEventType("audit_event_type");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QEventType> auditEventTypePk = createPrimaryKey(eventTypeId);

    }

    public class ForeignKeys {

        public final com.mysema.query.sql.ForeignKey<QApplication> eventTypeApplicationIdFk = createForeignKey(applicationId, "application_id");

        public final com.mysema.query.sql.ForeignKey<org.everit.osgi.resource.ri.schema.qdsl.QResource> eventTypeResourceIdFk = createForeignKey(resourceId, "resource_id");

        public final com.mysema.query.sql.ForeignKey<QEvent> _eventEventTypeIdFk = createInvForeignKey(eventTypeId, "event_type_id");

    }

    public final NumberPath<Long> applicationId = createNumber("applicationId", Long.class);

    public final NumberPath<Long> eventTypeId = createNumber("eventTypeId", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> resourceId = createNumber("resourceId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QEventType(String variable) {
        super(QEventType.class, forVariable(variable), "org.everit.osgi.audit.ri", "audit_event_type");
        addMetadata();
    }

    public QEventType(String variable, String schema, String table) {
        super(QEventType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEventType(Path<? extends QEventType> path) {
        super(path.getType(), path.getMetadata(), "org.everit.osgi.audit.ri", "audit_event_type");
        addMetadata();
    }

    public QEventType(PathMetadata<?> metadata) {
        super(QEventType.class, metadata, "org.everit.osgi.audit.ri", "audit_event_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(applicationId, ColumnMetadata.named("application_id").ofType(-5).withSize(19).notNull());
        addMetadata(eventTypeId, ColumnMetadata.named("event_type_id").ofType(-5).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").ofType(12).withSize(255).notNull());
        addMetadata(resourceId, ColumnMetadata.named("resource_id").ofType(-5).withSize(19).notNull());
    }

}

