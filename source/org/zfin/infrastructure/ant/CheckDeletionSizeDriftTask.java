package org.zfin.infrastructure.ant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.zfin.feature.FeatureDeletionSizeRow;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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

    private static final Set<FeatureTypeEnum> SIZED_TYPES =
            EnumSet.of(FeatureTypeEnum.DELETION, FeatureTypeEnum.INDEL, FeatureTypeEnum.MNV);

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
            List<FeatureDeletionSizeRow> rows = RepositoryFactory.getSequenceRepository()
                    .getDeletionSizeDriftCandidates(SIZED_TYPES);

            for (FeatureDeletionSizeRow row : rows) {
                examined++;
                Integer storedSize = row.storedSize();
                Integer startLoc = row.startLocation();
                Integer endLoc = row.endLocation();

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
                    List<String> outRow = new ArrayList<>(9);
                    outRow.add(row.featureZdbId());
                    outRow.add(row.featureAbbrev() == null ? "" : row.featureAbbrev());
                    outRow.add(row.featureType() == null ? "" : row.featureType().name());
                    outRow.add(row.assemblyName() == null ? "" : row.assemblyName());
                    outRow.add(row.chromosome() == null ? "" : row.chromosome());
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