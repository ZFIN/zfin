package org.zfin.datatransfer.ncbi.port;

import jakarta.persistence.Query;
import org.hibernate.Session;

import java.util.List;

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

    public static Integer countData(Session session, String sql) {
        String countSql = "select count(*) as num from (" + sql + ") as subquery";
        Query query = session.createNativeQuery(countSql);
        List results = query.getResultList();
        Long count = (Long)results.get(0);
        return count.intValue();
    }
}
