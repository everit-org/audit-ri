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

    public static final QApplication application = new QApplication("audit_application");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QApplication> auditApplicationPk = createPrimaryKey(applicationId);

    }

    public class ForeignKeys {

        public final com.mysema.query.sql.ForeignKey<org.everit.osgi.resource.ri.schema.qdsl.QResource> applicationResourceIdFk = createForeignKey(resourceId, "resource_id");

        public final com.mysema.query.sql.ForeignKey<QEventType> _eventTypeApplicationIdFk = createInvForeignKey(applicationId, "application_id");

    }

    public final NumberPath<Long> applicationId = createNumber("applicationId", Long.class);

    public final StringPath applicationName = createString("applicationName");

    public final NumberPath<Long> resourceId = createNumber("resourceId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QApplication(String variable) {
        super(QApplication.class, forVariable(variable), "org.everit.osgi.audit.ri", "audit_application");
        addMetadata();
    }

    public QApplication(String variable, String schema, String table) {
        super(QApplication.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QApplication(Path<? extends QApplication> path) {
        super(path.getType(), path.getMetadata(), "org.everit.osgi.audit.ri", "audit_application");
        addMetadata();
    }

    public QApplication(PathMetadata<?> metadata) {
        super(QApplication.class, metadata, "org.everit.osgi.audit.ri", "audit_application");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(applicationId, ColumnMetadata.named("application_id").ofType(-5).withSize(19).notNull());
        addMetadata(applicationName, ColumnMetadata.named("application_name").ofType(12).withSize(255).notNull());
        addMetadata(resourceId, ColumnMetadata.named("resource_id").ofType(-5).withSize(19).notNull());
    }

}

