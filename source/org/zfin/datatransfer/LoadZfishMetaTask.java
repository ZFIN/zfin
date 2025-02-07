package org.zfin.datatransfer;

import jakarta.persistence.Tuple;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.IOException;
import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;

public class LoadZfishMetaTask extends AbstractScriptWrapper {

    public static void main(String[] args) throws IOException {
        LoadZfishMetaTask task = new LoadZfishMetaTask();
        task.runTask();
        System.exit(0);
    }

    public void runTask() throws IOException {
        initAll();

        String loadZfishSQL = """
        SELECT
            dalias_data_zdb_id AS dblink_linked_recid,
            dalias_alias AS dblink_acc_num,
            NULL AS dblink_info,
            get_id_and_insert_active_data('DBLINK') AS dblink_zdb_id,
            dalias_alias AS dblink_acc_num_display,
            NULL AS dblink_length,
            'ZDB-FDBCONT-120213-1' as dblink_fdbcont_zdb_id
        FROM
            data_alias
        WHERE
            dalias_alias ~ '^GBT\\d\\d\\d\\d$'
            AND dalias_alias NOT IN (SELECT dblink_acc_num FROM db_link WHERE dblink_acc_num LIKE 'GBT%')
        """;
        HibernateUtil.createTransaction();
        List<Tuple> newRows = currentSession().createNativeQuery(loadZfishSQL, Tuple.class).list();

        System.out.println("New GBT links to load: " + newRows.size());
        if (!newRows.isEmpty()) {
            //header
            System.out.println("dblink_linked_recid,dblink_acc_num,dblink_info,dblink_zdb_id,dblink_acc_num_display,dblink_length,dblink_fdbcont_zdb_id");
        }
        for(Tuple row : newRows) {
            System.out.println(row.get(0) + "," + row.get(1) + "," + row.get(2) + "," + row.get(3) + "," + row.get(4) + "," + row.get(5) + "," + row.get(6));
        }

        String insertZfishMetaSQL = "INSERT INTO db_link " + loadZfishSQL;
        currentSession().createNativeQuery(insertZfishMetaSQL).executeUpdate();

        HibernateUtil.flushAndCommitCurrentSession();
    }

}
