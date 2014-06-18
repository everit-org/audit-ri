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

    public static final QEventData auditEventData = new QEventData("AUDIT_EVENT_DATA");

    public final SimplePath<java.sql.Blob> binaryValue = createSimple("binaryValue", java.sql.Blob.class);

    public final NumberPath<Long> eventDataId = createNumber("eventDataId", Long.class);

    public final StringPath eventDataName = createString("eventDataName");

    public final StringPath eventDataType = createString("eventDataType");

    public final NumberPath<Long> eventId = createNumber("eventId", Long.class);

    public final NumberPath<Double> numberValue = createNumber("numberValue", Double.class);

    public final StringPath stringValue = createString("stringValue");

    public final StringPath textValue = createString("textValue");

    public final DateTimePath<java.sql.Timestamp> timestampValue = createDateTime("timestampValue", java.sql.Timestamp.class);

    public final com.mysema.query.sql.PrimaryKey<QEventData> auditEventDataPk = createPrimaryKey(eventDataId);

    public final com.mysema.query.sql.ForeignKey<QEvent> eventDataEventIdFk = createForeignKey(eventId, "EVENT_ID");

    public QEventData(String variable) {
        super(QEventData.class, forVariable(variable), null, "AUDIT_EVENT_DATA");
        addMetadata();
    }

    public QEventData(String variable, String schema, String table) {
        super(QEventData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEventData(Path<? extends QEventData> path) {
        super(path.getType(), path.getMetadata(), null, "AUDIT_EVENT_DATA");
        addMetadata();
    }

    public QEventData(PathMetadata<?> metadata) {
        super(QEventData.class, metadata, null, "AUDIT_EVENT_DATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(binaryValue, ColumnMetadata.named("BINARY_VALUE").ofType(2004).withSize(2147483647));
        addMetadata(eventDataId, ColumnMetadata.named("EVENT_DATA_ID").ofType(-5).withSize(19).notNull());
        addMetadata(eventDataName, ColumnMetadata.named("EVENT_DATA_NAME").ofType(12).withSize(255).notNull());
        addMetadata(eventDataType, ColumnMetadata.named("EVENT_DATA_TYPE").ofType(12).withSize(32).notNull());
        addMetadata(eventId, ColumnMetadata.named("EVENT_ID").ofType(-5).withSize(19).notNull());
        addMetadata(numberValue, ColumnMetadata.named("NUMBER_VALUE").ofType(8).withSize(17));
        addMetadata(stringValue, ColumnMetadata.named("STRING_VALUE").ofType(12).withSize(2000));
        addMetadata(textValue, ColumnMetadata.named("TEXT_VALUE").ofType(2005).withSize(2147483647));
        addMetadata(timestampValue, ColumnMetadata.named("TIMESTAMP_VALUE").ofType(93).withSize(23).withDigits(10));
    }

}

