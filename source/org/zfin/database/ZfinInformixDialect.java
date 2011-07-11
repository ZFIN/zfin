package org.zfin.database;

import org.hibernate.MappingException;
import org.hibernate.dialect.InformixDialect;

import java.sql.Types;

/**
 * Customized Informix Dialect for Hibernate.
 *
 * See case 6562.  Overriding calls to systables where tabid=1 in favor of single to avoid potential
 * locking issues.
 */
public class ZfinInformixDialect extends InformixDialect {

    public ZfinInformixDialect() {
        registerColumnType(Types.BOOLEAN, "boolean");
        registerHibernateType(Types.BOOLEAN, "boolean");

        // may be necessary in cases where we have a char(2), as it maps that to char and
        // truncates a valid 2-letter string
        registerColumnType(Types.CHAR, "string");
        registerHibernateType(Types.CHAR, "string");

        registerColumnType(Types.LONGVARCHAR, "string");
        registerHibernateType(Types.LONGVARCHAR, "string");

    }

    @Override
    public String getIdentitySelectString(String table, String column, int type) throws MappingException {
//  return type==Types.BIGINT ?
//             "select dbinfo('serial8') from systables where tabid=1" :
//             "select dbinfo('sqlca.sqlerrd1') from systables where tabid=1";
        return type==Types.BIGINT ?
                "select dbinfo('serial8') from single " :
                "select dbinfo('sqlca.sqlerrd1') from single ";
    }

    @Override
    public String getSequenceNextValString(String sequenceName) {
//         return "select " + getSelectSequenceNextValString( sequenceName )   +
//                 " from systables where tabid=1";
        return "select " + getSelectSequenceNextValString( sequenceName )   +
                " from single ";
    }

    @Override
    public String getCurrentTimestampSelectString() {
//         return "select distinct current timestamp from informix.systables";
        return "select distinct current timestamp from single";
    }
}
