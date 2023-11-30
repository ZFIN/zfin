package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.Date;
import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.uniprot.secondary.SecondaryTermLoadService.getReferenceDatabaseIDForAction;

/**
 * Adds InterPro, PFAM, EC, PROSITE accessions to db_links table.
 * This is based on the entries that appear in the uniprot release file.
 * If the accession is already in the database, it is not added.
 */
@Log4j2
public class AddNewDBLinksFromUniProtsActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.DB_LINK;
    }

    private static String getDBLinkInfo() {
        //eg. 2023-08-27 Swiss-Prot
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return timestamp + " Swiss-Prot";
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> subTypeActions) {

        log.debug("creating bulk table for db_link");
        String sql = "create temp table bulk_db_link as select * from db_link where false";
        currentSession().createSQLQuery(sql).executeUpdate();

        //load the data into the bulk table
        log.debug("loading data into bulk table");
        for(SecondaryTermLoadAction action : subTypeActions) {
            loadSingleDBLinkToBulkTable(action);
        }

        //set the zdb_id field
        log.debug("generating ZDB IDs");
        sql = "update bulk_db_link set dblink_zdb_id = get_id('DBLINK')";
        currentSession().createSQLQuery(sql).executeUpdate();

        //insert the active data
        log.debug("inserting active data");
        sql = "insert into zdb_active_data select dblink_zdb_id from bulk_db_link";
        currentSession().createSQLQuery(sql).executeUpdate();

        //insert from the bulk table
        log.debug("inserting into db_link from bulk table");
        sql = "insert into db_link select * from bulk_db_link";
        currentSession().createSQLQuery(sql).executeUpdate();

        //drop the bulk table
        log.debug("dropping bulk table");
        sql = "drop table bulk_db_link";
        currentSession().createSQLQuery(sql).executeUpdate();

    }

    private void loadSingleDBLinkToBulkTable(SecondaryTermLoadAction action) {
        String sql = """
                    INSERT INTO bulk_db_link (dblink_linked_recid, dblink_acc_num, dblink_info, dblink_acc_num_display, dblink_length, dblink_fdbcont_zdb_id) 
                    VALUES (:linkedRecID, :accession, :linkInfo, :accession, :length, :fdbcontZdbID)                
                """;
        currentSession().createSQLQuery(sql)
                .setParameter("linkedRecID", action.getGeneZdbID())
                .setParameter("accession", action.getAccession())
                .setParameter("linkInfo", getDBLinkInfo())
                .setParameter("length", action.getLength())
                .setParameter("fdbcontZdbID", getReferenceDatabaseIDForAction(action))
                .executeUpdate();
    }

}
