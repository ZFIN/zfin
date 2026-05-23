package org.zfin.infrastructure.ant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.hibernate.query.NativeQuery;
import org.zfin.framework.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Reports features whose stored deletion size (fdmd_number_removed_dna_base_pairs)
 * disagrees with the size implied by the feature's stored chromosome coordinates
 * (sfcl_end_position - sfcl_start_position + 1). Read-only — no DB updates.
 *
 * The deletion size has recently been changed to an auto-calculated value
 * from start/end on edit, so new rows are always consistent. This report
 * finds older rows that were saved before the auto-calculation landed.
 *
 * Scope: feature types where a deletion size makes sense — DELETION, INDEL,
 * MNV. Features with a null number_removed_dna_base_pairs, no SFCL row, or
 * missing start/end are skipped (logged in counters, not the report).
 *
 * Sibling of {@link CheckSequenceOfReferenceDriftTask}; tracks ZFIN-10280.
 */
public class CheckDeletionSizeDriftTask extends AbstractValidateDataReportTask {

    private static final Logger LOG = LogManager.getLogger(CheckDeletionSizeDriftTask.class);

    public CheckDeletionSizeDriftTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    @Override
    public int execute() {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
        LOG.info("Job Name: " + jobName);

        clearReportDirectory();
        setLoggerFile();

        List<List<String>> mismatches = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        int examined = 0;
        int skippedNoCoords = 0;
        int skippedNoSize = 0;

        try {
            // Native projection — keep entities out of the persistence context
            // for the full dataset (cf. CheckSequenceOfReferenceDriftTask).
            // Restrict to feature types where deletion size is meaningful.
            String sql = "select fdmd.fdmd_feature_zdb_id, " +
                    "       f.feature_abbrev, " +
                    "       f.feature_type, " +
                    "       sfcl.sfcl_assembly, " +
                    "       sfcl.sfcl_chromosome, " +
                    "       sfcl.sfcl_start_position, " +
                    "       sfcl.sfcl_end_position, " +
                    "       fdmd.fdmd_number_removed_dna_base_pairs " +
                    "  from feature_dna_mutation_detail fdmd " +
                    "  join feature f on f.feature_zdb_id = fdmd.fdmd_feature_zdb_id " +
                    "  left join sequence_feature_chromosome_location sfcl " +
                    "         on sfcl.sfcl_feature_zdb_id = fdmd.fdmd_feature_zdb_id " +
                    " where f.feature_type in ('DELETION', 'INDEL', 'MNV') " +
                    "   and fdmd.fdmd_number_removed_dna_base_pairs is not null " +
                    " order by fdmd.fdmd_feature_zdb_id";

            NativeQuery<Object[]> query = HibernateUtil.currentSession().createNativeQuery(sql, Object[].class);

            for (Object[] row : query.list()) {
                examined++;
                String featureZdbId = (String) row[0];
                String featureAbbrev = (String) row[1];
                String featureType = (String) row[2];
                String assemblyName = (String) row[3];
                String chromosome = (String) row[4];
                Integer startLoc = row[5] != null ? ((Number) row[5]).intValue() : null;
                Integer endLoc = row[6] != null ? ((Number) row[6]).intValue() : null;
                Integer storedSize = row[7] != null ? ((Number) row[7]).intValue() : null;

                if (storedSize == null) {
                    skippedNoSize++;
                    continue;
                }
                if (startLoc == null || endLoc == null) {
                    skippedNoCoords++;
                    continue;
                }

                int expectedSize = endLoc - startLoc + 1;
                if (storedSize != expectedSize) {
                    List<String> outRow = new ArrayList<>(8);
                    outRow.add(featureZdbId);
                    outRow.add(featureAbbrev == null ? "" : featureAbbrev);
                    outRow.add(featureType == null ? "" : featureType);
                    outRow.add(assemblyName == null ? "" : assemblyName);
                    outRow.add(chromosome == null ? "" : chromosome);
                    outRow.add(String.valueOf(startLoc));
                    outRow.add(String.valueOf(endLoc));
                    outRow.add(String.valueOf(storedSize));
                    outRow.add(String.valueOf(expectedSize));
                    mismatches.add(outRow);
                }
            }
        } catch (Exception e) {
            LOG.error("Deletion-size drift check failed", e);
            errorMessages.add(e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            HibernateUtil.closeSession();
        }

        LOG.info("Examined " + examined + " FDMD rows; "
                + mismatches.size() + " mismatches; "
                + skippedNoCoords + " skipped (no usable coords); "
                + skippedNoSize + " skipped (no stored size)");

        createErrorReport(errorMessages, mismatches);
        return 0;
    }

    public static void main(String[] args) {
        String instance = args[0];
        String jobName = args[1];
        String directory = args[2];
        String propertyFilePath = args[3];
        CheckDeletionSizeDriftTask task = new CheckDeletionSizeDriftTask(
                jobName, propertyFilePath, directory);
        task.setInstance(instance);
        task.initDatabase();
        System.exit(task.execute());
    }
}
