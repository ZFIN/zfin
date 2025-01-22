package org.zfin.thisse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class ThisseLegacyImportAnalysisTask extends AbstractScriptWrapper {

    private static final String CSV_FILE = "thisse_legacy_import_analysis.csv";

    public static void main(String[] args) throws IOException {
        ThisseLegacyImportAnalysisTask task = new ThisseLegacyImportAnalysisTask();
        task.runTask();
        System.exit(0);
    }

    public void runTask() throws IOException {
        initAll();
        Transaction transaction = HibernateUtil.createTransaction();
        createTemporaryReportTable();
        setReportMatchesForGeneByZdbID();
        setReportMatchesForGeneByReplacedZdbID();
        markRowsWithZdbIDWithoutMatch();
        setReportMatchesForCloneByZdbIDAndCloneName();
        setReportMatchesForCloneByZdbIDAndAlias();
        setReportMatchesForCloneByCloneName();
        setReportMatchesForCloneByCloneAlias();
        setReportMatchesForGeneByName();
        setReportMatchesForGeneByAlias();
        setReportMatchesForGeneBySequence();
        setReportMatchesForCloneBySequence();
        setGeneMismatchFlag();
        exportReport();
        transaction.commit();
    }

    private void createTemporaryReportTable() {
        HibernateUtil.currentSession().createNativeQuery("DROP TABLE IF EXISTS thisse.temp_thisse_report").executeUpdate();
        String sql = """
                    SELECT
                        '' AS zclone_zdb_id,
                        '' AS zclone_name,
                        '' AS zclone_match_method,
                        '' AS zgene_zdb_id,
                        '' AS zgene_name,
                        '' AS zgene_match_method,
                        '' AS zflags,
                        th.* INTO thisse.temp_thisse_report
                    FROM
                        thisse.thisse_plates_march17 th
                    """;
        HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        String cleanSql = "update thisse.temp_thisse_report set gene_code = trim('\t ' from gene_code)";
        HibernateUtil.currentSession().createNativeQuery(cleanSql).executeUpdate();

    }

    private void setReportMatchesForGeneByZdbID() {
        String sql = """
            WITH subquery AS (
                SELECT
                    tpm.count,
                    tpm.zfin_page,
                    m.mrkr_abbrev
                FROM
                    thisse.temp_thisse_report tpm
                    LEFT JOIN marker m ON tpm.zfin_page = m.mrkr_zdb_id
                WHERE
                    zfin_page <> ''
                    AND mrkr_zdb_id IS NOT NULL)
            UPDATE
                thisse.temp_thisse_report
            SET
                zgene_zdb_id = subquery.zfin_page,
                zgene_name = subquery.mrkr_abbrev,
                zgene_match_method = 'ZFIN_ID'
            FROM
                subquery
            WHERE
                thisse.temp_thisse_report.count = subquery.count
            """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched Gene on ZFIN ID: " + numberOfUpdates);
    }

    private void setReportMatchesForGeneByReplacedZdbID() {
        String sql = """
            WITH subquery AS (
                SELECT
                    tpm.count,
                    rd.zrepld_new_zdb_id,
                    m.mrkr_abbrev
                FROM
                    thisse.temp_thisse_report tpm
                    LEFT JOIN zdb_replaced_data rd ON tpm.zfin_page = rd.zrepld_old_zdb_id
                    LEFT JOIN marker m ON zrepld_new_zdb_id = m.mrkr_zdb_id
                WHERE
                    rd.zrepld_new_zdb_id IS NOT NULL
                    AND tpm.zgene_zdb_id = '')
            UPDATE
                thisse.temp_thisse_report
            SET
                zgene_zdb_id = subquery.zrepld_new_zdb_id,
                zgene_name = subquery.mrkr_abbrev,
                zgene_match_method = 'ZFIN_REPLACED_ID'
            FROM
                subquery
            WHERE
                thisse.temp_thisse_report.count = subquery.count
            """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched Gene on Merged ZFIN ID: " + numberOfUpdates);
    }

    private void markRowsWithZdbIDWithoutMatch() {
        String sql = """
                UPDATE
                    thisse.temp_thisse_report
                SET
                    zflags = trim(zflags || ' GENE_ZDB_NOT_FOUND')
                WHERE
                    zfin_page <> ''
                    AND zgene_zdb_id = ''
                """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number of rows in Thisse data with ZFIN ID that has no match: " + numberOfUpdates);
    }

    private void setReportMatchesForCloneByZdbIDAndCloneName() {
        String sql = """
            WITH subquery AS (
                SELECT "count", clone_name, m.mrkr_zdb_id, m.mrkr_abbrev FROM thisse.temp_thisse_report th
                    LEFT JOIN marker_relationship mr ON th."zfin_page" = mr.mrel_mrkr_1_zdb_id
                    LEFT JOIN marker m ON mr.mrel_mrkr_2_zdb_id = m.mrkr_zdb_id
                    LEFT JOIN data_alias a ON m.mrkr_zdb_id = a.dalias_data_zdb_id
                    WHERE mrel_zdb_id IS NOT NULL
                    AND lower(mrkr_abbrev) = lower(th.clone_name)
            )
            UPDATE
                thisse.temp_thisse_report
            SET
                zclone_zdb_id = subquery.mrkr_zdb_id,
                zclone_name = subquery.mrkr_abbrev,
                zclone_match_method = 'ZFIN_GENE_ID_AND_CLONE_NAME'
            FROM
                subquery
            WHERE
                thisse.temp_thisse_report.count = subquery.count
            """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched on feature/marker relationship using GENE ID and Clone Name: " + numberOfUpdates);
    }

    private void setReportMatchesForCloneByZdbIDAndAlias() {
        String sql = """
            WITH subquery AS (
                SELECT "count", clone_name, m.mrkr_zdb_id, m.mrkr_abbrev FROM thisse.temp_thisse_report th
                    LEFT JOIN marker_relationship mr ON th."zfin_page" = mr.mrel_mrkr_1_zdb_id
                    LEFT JOIN marker m ON mr.mrel_mrkr_2_zdb_id = m.mrkr_zdb_id
                    LEFT JOIN data_alias a ON m.mrkr_zdb_id = a.dalias_data_zdb_id
                    WHERE mrel_zdb_id IS NOT NULL
                    AND lower(dalias_alias) = lower(replace(th.clone_name, '_', ''))
                    AND th.zclone_zdb_id = ''
            )
            UPDATE
                thisse.temp_thisse_report
            SET
                zclone_zdb_id = subquery.mrkr_zdb_id,
                zclone_name = subquery.mrkr_abbrev,
                zclone_match_method = 'ZFIN_GENE_ID_AND_CLONE_ALIAS'
            FROM
                subquery
            WHERE
                thisse.temp_thisse_report.count = subquery.count
            """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched on feature/marker relationship using GENE ID and Clone Alias: " + numberOfUpdates);
    }

    private void setReportMatchesForCloneByCloneName() {
        String sql = """
                WITH subquery AS (
                    SELECT
                        tr.count,
                        zclone_zdb_id,
                        zclone_name,
                        clone_name,
                        m.mrkr_abbrev,
                        m.mrkr_zdb_id
                    FROM
                        thisse.temp_thisse_report tr
                        LEFT JOIN marker m ON lower(replace(tr.clone_name, '_', '')) = lower(m.mrkr_abbrev)
                    WHERE
                        zclone_zdb_id = ''
                        AND mrkr_zdb_id IS NOT NULL)
                UPDATE
                    thisse.temp_thisse_report
                SET
                    zclone_zdb_id = subquery.mrkr_zdb_id,
                    zclone_name = subquery.mrkr_abbrev,
                    zclone_match_method = 'CLONE_NAME'
                FROM
                    subquery
                WHERE
                    thisse.temp_thisse_report.count = subquery.count
                """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched on Clone Name: " + numberOfUpdates);
    }

    private void setReportMatchesForCloneByCloneAlias() {
        String sql = """
            WITH subquery AS (
                SELECT
                    tr.count,
                    clone_name,
                    da.dalias_data_zdb_id,
                    m.mrkr_zdb_id,
                    m.mrkr_abbrev
                FROM
                    thisse.temp_thisse_report tr
                    LEFT JOIN data_alias da ON replace(dalias_alias_lower, '_', '') = replace(lower(clone_name), '_', '')
                    LEFT JOIN marker m ON dalias_data_zdb_id = m.mrkr_zdb_id
                WHERE
                    zclone_zdb_id = ''
                    AND dalias_zdb_id IS NOT NULL)
            UPDATE
                    thisse.temp_thisse_report
                SET
                    zclone_zdb_id = subquery.mrkr_zdb_id,
                    zclone_name = subquery.mrkr_abbrev,
                    zclone_match_method = 'CLONE_ALIAS'
                FROM
                    subquery
                WHERE
                    thisse.temp_thisse_report.count = subquery.count
            """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched on Clone Alias: " + numberOfUpdates);
    }

    private void setReportMatchesForGeneByName() {
        String sql = """
                WITH subquery AS (
                    SELECT
                        tr.count,
                        tr.gene_code,
                        m.mrkr_abbrev,
                        m.mrkr_zdb_id
                    FROM
                        thisse.temp_thisse_report tr
                        LEFT JOIN marker m ON m.mrkr_abbrev = tr.gene_code
                    WHERE
                        zgene_zdb_id = ''
                        AND gene_code <> ''
                        AND m.mrkr_zdb_id IS NOT NULL )
                UPDATE
                    thisse.temp_thisse_report
                SET
                    zgene_zdb_id = subquery.mrkr_zdb_id,
                    zgene_name = subquery.mrkr_abbrev,
                    zgene_match_method = 'GENE_NAME'
                FROM
                    subquery
                WHERE
                    thisse.temp_thisse_report.count = subquery.count
                """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched on Gene Name: " + numberOfUpdates);
    }

    private void setReportMatchesForGeneByAlias() {
        String sql = """
                WITH subquery AS (
                SELECT
                  tr.count,
                	da.dalias_data_zdb_id,
                	m.mrkr_abbrev,
                	m.mrkr_zdb_id
                FROM
                	thisse.temp_thisse_report tr
                	LEFT JOIN data_alias da ON da.dalias_alias_lower = LOWER ( tr.gene_code )
                	LEFT JOIN marker m on m.mrkr_zdb_id = dalias_data_zdb_id
                WHERE
                	zgene_name = ''
                	AND gene_code <> ''
                	AND da.dalias_zdb_id IS NOT NULL)
                UPDATE
                		thisse.temp_thisse_report
                SET
                		zgene_zdb_id = subquery.mrkr_zdb_id,
                		zgene_name = subquery.mrkr_abbrev,
                		zgene_match_method = 'GENE_ALIAS'
                FROM
                		subquery
                WHERE
                		thisse.temp_thisse_report.count = subquery.count
                """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched on Gene Alias: " + numberOfUpdates);
    }

    private void setReportMatchesForGeneBySequence() {
        String sql = """
                WITH subquery AS (
                    SELECT
                        tr.count,
                        m.mrkr_abbrev,
                        m.mrkr_zdb_id
                    FROM
                        thisse.temp_thisse_report tr
                        LEFT JOIN db_link d ON tr.sequence = d.dblink_acc_num
                        LEFT JOIN marker m ON d.dblink_linked_recid = m.mrkr_zdb_id
                        LEFT JOIN marker_type_group_member mtgm ON m.mrkr_type = mtgm.mtgrpmem_mrkr_type
                    WHERE
                        mtgrpmem_mrkr_type_group = 'GENE'
                        AND zgene_name = '')
                UPDATE
                		thisse.temp_thisse_report
                SET
                		zgene_zdb_id = subquery.mrkr_zdb_id,
                		zgene_name = subquery.mrkr_abbrev,
                		zgene_match_method = 'GENE_SEQUENCE'
                FROM
                		subquery
                WHERE
                		thisse.temp_thisse_report.count = subquery.count
                """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched on Gene by Sequence: " + numberOfUpdates);
    }

    private void setReportMatchesForCloneBySequence() {
        String sql = """
                WITH subquery AS (
                    SELECT
                        tr.count,
                        m.mrkr_abbrev,
                        m.mrkr_zdb_id
                    FROM
                        thisse.temp_thisse_report tr
                        LEFT JOIN db_link d ON tr.sequence = d.dblink_acc_num
                        LEFT JOIN marker m ON d.dblink_linked_recid = m.mrkr_zdb_id
                        LEFT JOIN marker_type_group_member mtgm ON m.mrkr_type = mtgm.mtgrpmem_mrkr_type
                    WHERE
                        mtgrpmem_mrkr_type_group = 'CLONEDOM'
                        AND zclone_name = '')
                UPDATE
                		thisse.temp_thisse_report
                SET
                		zclone_zdb_id = subquery.mrkr_zdb_id,
                		zclone_name = subquery.mrkr_abbrev,
                		zclone_match_method = 'CLONE_SEQUENCE'
                FROM
                		subquery
                WHERE
                		thisse.temp_thisse_report.count = subquery.count
                """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Matched on Clone by Sequence: " + numberOfUpdates);
    }

    private void setGeneMismatchFlag() {
        String sql = """
                WITH subquery AS (
                    SELECT
                        tr.count,
                        m.mrkr_abbrev,
                        m.mrkr_zdb_id
                    FROM
                        thisse.temp_thisse_report tr
                        LEFT JOIN marker_relationship mr ON tr.zclone_zdb_id = mr.mrel_mrkr_2_zdb_id
                        LEFT JOIN marker m ON mr.mrel_mrkr_1_zdb_id = m.mrkr_zdb_id
                    WHERE
                        m.mrkr_zdb_id <> tr.zgene_zdb_id)
                UPDATE
                		thisse.temp_thisse_report
                SET
                        zflags = trim(zflags || ' POSSIBLE_GENE_ZDB_MISMATCH:(' || zgene_name || '/' || zgene_zdb_id || ') vs (' || subquery.mrkr_abbrev || '/' || subquery.mrkr_zdb_id || ')')                       \s
                FROM
                		subquery
                WHERE
                		thisse.temp_thisse_report.count = subquery.count
                """;
        int numberOfUpdates = HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
        System.out.println("Number Flagged as Possible Gene Mismatch: " + numberOfUpdates);
    }

    private void exportReport() throws IOException {
        Path path = Paths.get( CSV_FILE );
        BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader(fieldNames()));

        List results = HibernateUtil.currentSession().createNativeQuery("select * from thisse.temp_thisse_report order by zgene_zdb_id desc, zclone_zdb_id desc").list();

        for(Object result : results) {
            writeCsvLine(result, csvPrinter);
        }

        csvPrinter.flush();
        csvPrinter.close();
        writer.close();

    }

    private void writeCsvLine(Object result, CSVPrinter csvPrinter) throws IOException {
        Object[] row = (Object[]) result;
        List<String> record = new ArrayList<>();
        for(int i = 0; i < row.length; i++) {
            record.add(row[i] == null ? "" : row[i].toString());
        }
        csvPrinter.printRecord(record);
    }

    private String[] fieldNames() {
        return new String[]{
                "zclone_zdb_id",
                "zclone_name",
                "zclone_match_method",
                "zgene_zdb_id",
                "zgene_name",
                "zgene_match_method",
                "zflags",
                "count",
                "clone_name",
                "original_name",
                "stars",
                "gene_name",
                "gene_code",
                "sequence",
                "size",
                "sending",
                "pattern",
                "plate1",
                "plate2",
                "plates",
                "marker",
                "zfin_name",
                "zfin_page",
                "morpholino",
                "top_blast",
                "test",
                "go_component",
                "go_function",
                "go_process",
                "plate1data",
                "plate2data",
                "plate1filename",
                "plate2filename"};
    }

}
