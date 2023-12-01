package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.persistence.BatchProcessor;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        BatchProcessor batchProcessor = new BatchProcessor(
                "db_link","dblink_zdb_id","DBLINK",
                subTypeActions.stream()
                        .map(action -> {
                            Map<String, Object> values = new HashMap<>();
                            values.put("dblink_linked_recid", action.getGeneZdbID());
                            values.put("dblink_acc_num", action.getAccession());
                            values.put("dblink_info", getDBLinkInfo());
                            values.put("dblink_acc_num_display", action.getAccession());
                            values.put("dblink_length", action.getLength());
                            values.put("dblink_fdbcont_zdb_id", getReferenceDatabaseIDForAction(action));
                            return values;
                        }).toList()
        );
        batchProcessor.execute();
    }


}
