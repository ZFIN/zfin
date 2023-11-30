package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.hibernate.query.NativeQuery;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.ArrayList;
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
        insertDBLinksToBulkTableInBatches(subTypeActions);

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

    private void insertDBLinksToBulkTableInBatches(List<SecondaryTermLoadAction> subTypeActions) {
        List<List<SecondaryTermLoadAction>> batchedActions = ListUtils.partition(subTypeActions, 100);
        for(List<SecondaryTermLoadAction> action : batchedActions) {
            loadSingleBatchOfDBLinksToBulkTable(action);
        }
    }

    /**
     * Builds up a single batch insert query like so:
     *        insert into bulk_marker_go_term_evidence
     *         (
     *         dblink_linked_recid, dblink_acc_num, dblink_info, dblink_acc_num_display, dblink_length, dblink_fdbcont_zdb_id
     *         ) VALUES
     *         (?, ?, ?, ?, ?, ?),
     *         ...
     *         (?, ?, ?, ?, ?, ?)
     *         ...
     * @param actions
     */
    private void loadSingleBatchOfDBLinksToBulkTable(List<SecondaryTermLoadAction> actions) {
        String sqlOuterTemplate = """
                insert into bulk_db_link
                  (
                  dblink_linked_recid, dblink_acc_num, dblink_info, dblink_acc_num_display, dblink_length, dblink_fdbcont_zdb_id
                  ) VALUES
                """;
        List<String> sqlInnerTemplates = new ArrayList<>();
        actions.forEach(a -> sqlInnerTemplates.add("(?, ?, ?, ?, ?, ?)"));

        String sql = sqlOuterTemplate + String.join(", ", sqlInnerTemplates);
        NativeQuery query = currentSession().createSQLQuery(sql);

        int i = 1;
        for(SecondaryTermLoadAction action : actions) {
            query.setParameter(i++, action.getGeneZdbID());
            query.setParameter(i++, action.getAccession());
            query.setParameter(i++, getDBLinkInfo());
            query.setParameter(i++, action.getAccession());
            query.setParameter(i++, action.getLength());
            query.setParameter(i++, getReferenceDatabaseIDForAction(action));
        }
        query.executeUpdate();
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
