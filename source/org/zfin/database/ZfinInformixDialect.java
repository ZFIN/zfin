package org.zfin.database;

import org.hibernate.dialect.InformixDialect;

import java.sql.Types;

/**
 * Customized Informix Dialect for Hibernate.
 */
public class ZfinInformixDialect extends InformixDialect {

    public ZfinInformixDialect() {
        registerColumnType(Types.BOOLEAN, "boolean");
        registerHibernateType(Types.BOOLEAN, "boolean");
        registerColumnType(Types.LONGVARCHAR, "string");
        registerHibernateType(Types.LONGVARCHAR, "string");
    }

}
