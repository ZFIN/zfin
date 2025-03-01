package org.zfin.uniprot;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.zfin.framework.HibernateUtil;
import org.zfin.uniprot.dto.UniProtLoadSummaryItemDTO;
import org.zfin.uniprot.dto.UniProtLoadSummaryListDTO;

import java.math.BigInteger;
import java.util.List;

/**
 * Generate a table of statistics like the legacy load did.
 * This should be calculable based on the actions that were performed and the context.
 *
 * Example:
 * count of records associated with UniProt     	before load	after load 	percentage change
 * ---------------------------------------------	-----------	-----------	-----------------
 * db_link records                              	     194687	     194462	            -0.12
 * external_note with db_link                   	          0	      25525
 * genes with duplicated db_link notes          	          0	          0	   not calculated
 * ---------------------------------------------	-----------	-----------	-----------------
 * marker_go_term_evidence IEA records          	     112109	     112381	             0.24
 * marker_go_term_evidence records from SP      	      46510	      46861	             0.75
 * marker_go_term_evidence records from IP      	      61647	      61827	             0.29
 * marker_go_term_evidence records from EC      	       3952	       3693	            -6.55
 * ---------------------------------------------	-----------	-----------	-----------------
 * go terms with IEA annotation                 	       3701	       3684	            -0.46
 * component go terms with IEA                  	        432	        431	            -0.23
 * function go terms with IEA                   	       1958	       1943	            -0.77
 * process go terms with IEA                    	       1311	       1310	            -0.08
 * ---------------------------------------------	-----------	-----------	-----------------
 * markers with IEA annotation                  	      18033	      17993	            -0.22
 * markers with IEA annotation component        	        432	        431	            -0.23
 * markers with IEA annotation function         	      13536	      13557	             0.16
 * markers with IEA annotation process          	      10938	      10921	            -0.16
 *
 */
public class UniProtLoadSummaryService {

    /**
     * Returns the Summary list with descriptions and counts (for before)
     * @return
     */
    public static UniProtLoadSummaryListDTO getBeforeSummary() {
        Session session = HibernateUtil.currentSession();
        String sql = getSummarySql();
        NativeQuery query = session.createNativeQuery(sql);
        List results = query.list();
        UniProtLoadSummaryListDTO summaryList = new UniProtLoadSummaryListDTO();
        for (Object result : results) {
            Object[] row = (Object[]) result;
            String description = (String) row[3];
            Long count = (Long) row[2];
            UniProtLoadSummaryItemDTO summary = new UniProtLoadSummaryItemDTO(description, count, null);
            summaryList.putBeforeSummary(summary);
        }
        return summaryList;
    }

    /**
     * Given the results of the "before" summary, this grabs the new data from the DB
     * and fills in the "after" part of the summary
     * @param beforeSummary
     * @return
     */
    public static UniProtLoadSummaryListDTO getAfterSummary(UniProtLoadSummaryListDTO beforeSummary) {
        Session session = HibernateUtil.currentSession();
        String sql = getSummarySql();
        NativeQuery query = session.createNativeQuery(sql);
        List results = query.list();
        for (Object result : results) {
            Object[] row = (Object[]) result;
            String description = (String) row[3];
            Long count = (Long) row[2];
            UniProtLoadSummaryItemDTO summary = new UniProtLoadSummaryItemDTO(description, null, count);
            beforeSummary.putAfterSummary(summary);
        }
        return beforeSummary;
    }

    /**
     * Get the SQL query for the summary report.
     * This is the same SQL that was used in the legacy perl scripts.
     * @return
     */
    private static String getSummarySql() {
        return """
            WITH external_note_data AS (
                SELECT
                    extnote_zdb_id,
                    dblink_zdb_id,
                    dblink_info
                FROM external_note
                         JOIN db_link ON extnote_data_zdb_id = dblink_zdb_id
                WHERE dblink_info LIKE '%Swiss-Prot%'
            ),
            marker_go_term_evidence_data AS (
                 SELECT
                     mrkrgoev_zdb_id,
                     mrkrgoev_evidence_code,
                     mrkrgoev_notes,
                     term_zdb_id,
                     term_ontology,
                     mrkr_zdb_id
                 FROM marker_go_term_evidence
                          JOIN term ON mrkrgoev_term_zdb_id = term_zdb_id
                          JOIN marker ON mrkr_zdb_id = mrkrgoev_mrkr_zdb_id
                 WHERE term_ont_id LIKE 'GO%'
                   AND mrkrgoev_evidence_code = 'IEA'
                   AND mrkrgoev_notes IN ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
            )
            
            SELECT 1 as row_number,
                   'numDblink' AS variable_name,
                   COUNT(*) AS count,
                   'db_link records' AS description
            FROM db_link
            WHERE dblink_info LIKE '%Swiss-Prot%'
            
            UNION ALL
            
            SELECT 2,
                   'numExternalNote',
                   COUNT(DISTINCT extnote_zdb_id),
                   'external_note with db_link'
            FROM external_note_data
            
            UNION ALL
            
            SELECT 3,
                   'numMarkersWithRedundantDblkNote',
                   COUNT(DISTINCT extnote_data_zdb_id),
                   'genes with duplicated db_link notes'
            FROM external_note note1
                     JOIN external_note_data ON note1.extnote_data_zdb_id = dblink_zdb_id
            WHERE EXISTS (
                          SELECT 1 FROM external_note note2
                          WHERE note2.extnote_data_zdb_id = note1.extnote_data_zdb_id
                            AND note2.extnote_zdb_id != note1.extnote_zdb_id
                      )
            
            UNION ALL
            
            SELECT 4,
                   'numIEA',
                   COUNT(DISTINCT mrkrgoev_zdb_id),
                   'marker_go_term_evidence IEA records'
            FROM marker_go_term_evidence_data
            
            UNION ALL
            
            SELECT 5,
                   'numIEASP2GO',
                   COUNT(DISTINCT mrkrgoev_zdb_id),
                   'marker_go_term_evidence records from SP'
            FROM marker_go_term_evidence_data
            WHERE mrkrgoev_notes = 'ZFIN SP keyword 2 GO'
            
            UNION ALL
            
            SELECT 6,
                   'numIEAInterPro2GO',
                   COUNT(DISTINCT mrkrgoev_zdb_id),
                   'marker_go_term_evidence records from IP'
            FROM marker_go_term_evidence_data
            WHERE mrkrgoev_notes = 'ZFIN InterPro 2 GO'
            
            UNION ALL
            
            SELECT 7,
                   'numIEAEC2GO',
                   COUNT(DISTINCT mrkrgoev_zdb_id),
                   'marker_go_term_evidence records from EC'
            FROM marker_go_term_evidence_data
            WHERE mrkrgoev_notes = 'ZFIN EC acc 2 GO'
            
            UNION ALL
            
            SELECT 8,
                   'numIEAterms',
                   COUNT(DISTINCT term_zdb_id),
                   'go terms with IEA annotation'
            FROM marker_go_term_evidence_data
            
            UNION ALL
            
            SELECT 9,
                   'numIEAtermComponent',
                   COUNT(DISTINCT term_zdb_id),
                   'component go terms with IEA'
            FROM marker_go_term_evidence_data
            WHERE term_ontology = 'cellular_component'
            
            UNION ALL
            
            SELECT 10,
                   'numIEAtermFunction',
                   COUNT(DISTINCT term_zdb_id),
                   'function go terms with IEA'
            FROM marker_go_term_evidence_data
            WHERE term_ontology = 'molecular_function'
            
            UNION ALL
            
            SELECT 11,
                   'numIEAtermProcess',
                   COUNT(DISTINCT term_zdb_id),
                   'process go terms with IEA'
            FROM marker_go_term_evidence_data
            WHERE term_ontology = 'biological_process'
            
            UNION ALL
            
            SELECT 12,
                   'numMrkr',
                   COUNT(DISTINCT mrkr_zdb_id),
                   'markers with IEA annotation'
            FROM marker_go_term_evidence_data
            
            UNION ALL
            
            SELECT 13,
                   'numMrkrComponent',
                   COUNT(DISTINCT mrkr_zdb_id),
                   'markers with IEA annotation component'
            FROM marker_go_term_evidence_data
            WHERE term_ontology = 'cellular_component'
            
            UNION ALL
            
            SELECT 14,
                   'numMrkrFunction',
                   COUNT(DISTINCT mrkr_zdb_id),
                   'markers with IEA annotation function'
            FROM marker_go_term_evidence_data
            WHERE term_ontology = 'molecular_function'
            
            UNION ALL
            
            SELECT 15,
                   'numMrkrProcess',
                   COUNT(DISTINCT mrkr_zdb_id),
                   'markers with IEA annotation process'
            FROM marker_go_term_evidence_data
            WHERE term_ontology = 'biological_process'
            
            ORDER BY row_number;
        """;
    }
}
