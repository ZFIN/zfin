package org.zfin.datatransfer.ncbi.port;

import jakarta.persistence.Query;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class PortSqlHelper {
    public static String getSqlForGeneAndRnagDbLinksFromFdbContId(String fdbContId) {
        String sql = """
        select distinct dblink_acc_num
        from db_link
        where dblink_fdbcont_zdb_id =
        """ +
         "'" + fdbContId + "'" +
        " and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')";
        return sql;
    }

    public static String getSqlForGeneAndRnagDbLinksSupportedByLoadPubsFromFdbContId(
            String fdbContId, String pubMappedbasedOnRNA, String pubMappedbasedOnVega, String pubMappedbasedOnNCBISupplement) {
        String sql = getSqlForGeneAndRnagDbLinksFromFdbContId(fdbContId) +
                     " and exists (select 1 from record_attribution " +
                     "       where recattrib_data_zdb_id = dblink_zdb_id " +
                     "         and recattrib_source_zdb_id in ('" + pubMappedbasedOnRNA + "','" + pubMappedbasedOnVega + "','" + pubMappedbasedOnNCBISupplement + "'))";
        return sql;
    }


    public static Integer countData(Session session, String sql) {
        String countSql = "select count(*) as num from (" + sql + ") as subquery";
        Query query = session.createNativeQuery(countSql);

        // Ensure the result is treated as a number.
        // Hibernate 6 might return BigInteger or Long depending on the DB.
        Object result = query.getSingleResult();
        if (result instanceof BigInteger) {
            return ((BigInteger) result).intValue();
        } else if (result instanceof Long) {
            return ((Long) result).intValue();
        } else if (result instanceof Integer) {
            return (Integer) result;
        }
        // Fallback or error if type is unexpected
        throw new IllegalStateException("Unexpected type for count query result: " + result.getClass().getName());
    }
}