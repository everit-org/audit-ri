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
 * QApplication is a Querydsl query type for QApplication
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QApplication extends com.querydsl.sql.RelationalPathBase<QApplication> {

    private static final long serialVersionUID = 2096124509;

    public static final QApplication application = new QApplication("audit_application");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QApplication> auditApplicationPk = createPrimaryKey(applicationId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QEventType> _eventTypeApplicationIdFk = createInvForeignKey(applicationId, "application_id");

    }

    public final NumberPath<Long> applicationId = createNumber("applicationId", Long.class);

    public final StringPath applicationName = createString("applicationName");

    public final NumberPath<Long> resourceId = createNumber("resourceId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QApplication(String variable) {
        super(QApplication.class, forVariable(variable), "org.everit.audit.ri", "audit_application");
        addMetadata();
    }

    public QApplication(String variable, String schema, String table) {
        super(QApplication.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QApplication(Path<? extends QApplication> path) {
        super(path.getType(), path.getMetadata(), "org.everit.audit.ri", "audit_application");
        addMetadata();
    }

    public QApplication(PathMetadata metadata) {
        super(QApplication.class, metadata, "org.everit.audit.ri", "audit_application");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(applicationId, ColumnMetadata.named("application_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(applicationName, ColumnMetadata.named("application_name").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(resourceId, ColumnMetadata.named("resource_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

