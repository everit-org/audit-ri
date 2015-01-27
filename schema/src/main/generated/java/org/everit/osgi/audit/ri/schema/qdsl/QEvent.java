package org.everit.osgi.audit.ri.schema.qdsl;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QEvent is a Querydsl query type for QEvent
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QEvent extends com.mysema.query.sql.RelationalPathBase<QEvent> {

    private static final long serialVersionUID = 463503637;

    public static final QEvent event = new QEvent("audit_event");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QEvent> auditEventPk = createPrimaryKey(eventId);

    }

    public class ForeignKeys {

        public final com.mysema.query.sql.ForeignKey<QEventType> eventEventTypeIdFk = createForeignKey(eventTypeId, "event_type_id");

        public final com.mysema.query.sql.ForeignKey<QEventData> _eventDataEventIdFk = createInvForeignKey(eventId, "event_id");

    }

    public final NumberPath<Long> eventId = createNumber("eventId", Long.class);

    public final NumberPath<Long> eventTypeId = createNumber("eventTypeId", Long.class);

    public final DateTimePath<java.sql.Timestamp> occuredAt = createDateTime("occuredAt", java.sql.Timestamp.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QEvent(String variable) {
        super(QEvent.class, forVariable(variable), "org.everit.osgi.audit.ri", "audit_event");
        addMetadata();
    }

    public QEvent(String variable, String schema, String table) {
        super(QEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEvent(Path<? extends QEvent> path) {
        super(path.getType(), path.getMetadata(), "org.everit.osgi.audit.ri", "audit_event");
        addMetadata();
    }

    public QEvent(PathMetadata<?> metadata) {
        super(QEvent.class, metadata, "org.everit.osgi.audit.ri", "audit_event");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(eventId, ColumnMetadata.named("event_id").ofType(-5).withSize(19).notNull());
        addMetadata(eventTypeId, ColumnMetadata.named("event_type_id").ofType(-5).withSize(19).notNull());
        addMetadata(occuredAt, ColumnMetadata.named("occured_at").ofType(93).withSize(23).withDigits(10).notNull());
    }

}

