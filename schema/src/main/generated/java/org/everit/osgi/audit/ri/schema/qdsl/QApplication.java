package org.everit.osgi.audit.ri.schema.qdsl;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QApplication is a Querydsl query type for QApplication
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QApplication extends com.mysema.query.sql.RelationalPathBase<QApplication> {

    private static final long serialVersionUID = 685378443;

    public static final QApplication auditApplication = new QApplication("AUDIT_APPLICATION");

    public final NumberPath<Long> applicationId = createNumber("applicationId", Long.class);

    public final StringPath applicationName = createString("applicationName");

    public final NumberPath<Long> resourceId = createNumber("resourceId", Long.class);

    public final com.mysema.query.sql.PrimaryKey<QApplication> auditApplicationPk = createPrimaryKey(applicationId);

    public final com.mysema.query.sql.ForeignKey<org.everit.osgi.resource.schema.qdsl.QResource> applicationResourceIdFk = createForeignKey(resourceId, "resource_id");

    public final com.mysema.query.sql.ForeignKey<QEventType> _eventTypeApplicationIdFk = createInvForeignKey(applicationId, "APPLICATION_ID");

    public QApplication(String variable) {
        super(QApplication.class, forVariable(variable), null, "AUDIT_APPLICATION");
        addMetadata();
    }

    public QApplication(String variable, String schema, String table) {
        super(QApplication.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QApplication(Path<? extends QApplication> path) {
        super(path.getType(), path.getMetadata(), null, "AUDIT_APPLICATION");
        addMetadata();
    }

    public QApplication(PathMetadata<?> metadata) {
        super(QApplication.class, metadata, null, "AUDIT_APPLICATION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(applicationId, ColumnMetadata.named("APPLICATION_ID").ofType(-5).withSize(19).notNull());
        addMetadata(applicationName, ColumnMetadata.named("APPLICATION_NAME").ofType(12).withSize(255).notNull());
        addMetadata(resourceId, ColumnMetadata.named("RESOURCE_ID").ofType(-5).withSize(19).notNull());
    }

}

