package org.zfin.infrastructure.ant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.hibernate.query.NativeQuery;
import org.zfin.framework.HibernateUtil;
import org.zfin.mapping.GenomicLocationService;
import org.zfin.sequence.gff.AssemblyEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Reports features whose stored Sequence of Reference (fgmd_sequence_of_reference)
 * disagrees with the sequence computed live from the genome assembly FASTA at
 * the feature's stored coordinates. Read-only — no DB updates.
 *
 * Wired via the validate-data-report-simple-java Ant target; designed to run as
 * a Jenkins job. Tracks ZFIN-10280.
 */
public class CheckSequenceOfReferenceDriftTask extends AbstractValidateDataReportTask {

    private static final Logger LOG = LogManager.getLogger(CheckSequenceOfReferenceDriftTask.class);

    public CheckSequenceOfReferenceDriftTask(String jobName, String propertyFilePath, String dataDirectoryString) {
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
        int skippedFasta = 0;

        try {
            // Native projection — no Hibernate entity loading. Loading entities
            // (FeatureGenomicMutationDetail with lazy associations) attached every
            // row to the persistence context, and the session-close cascade walk
            // ground the JVM to a halt over the full dataset.
            String sql = "select fgmd.fgmd_feature_zdb_id, " +
                    "       f.feature_abbrev, " +
                    "       f.feature_type, " +
                    "       sfcl.sfcl_assembly, " +
                    "       sfcl.sfcl_chromosome, " +
                    "       sfcl.sfcl_start_position, " +
                    "       sfcl.sfcl_end_position, " +
                    "       fgmd.fgmd_sequence_of_reference " +
                    "  from feature_genomic_mutation_detail fgmd " +
                    "  join feature f on f.feature_zdb_id = fgmd.fgmd_feature_zdb_id " +
                    "  left join sequence_feature_chromosome_location sfcl " +
                    "         on sfcl.sfcl_feature_zdb_id = fgmd.fgmd_feature_zdb_id " +
                    " where fgmd.fgmd_sequence_of_reference is not null " +
                    " order by fgmd.fgmd_feature_zdb_id";

            NativeQuery<Object[]> query = HibernateUtil.currentSession().createNativeQuery(sql, Object[].class);
            GenomicLocationService glService = new GenomicLocationService();

            for (Object[] row : query.list()) {
                examined++;
                String featureZdbId = (String) row[0];
                String featureAbbrev = (String) row[1];
                String featureType = (String) row[2];
                String assemblyName = (String) row[3];
                String chromosome = (String) row[4];
                Integer startLoc = row[5] != null ? ((Number) row[5]).intValue() : null;
                Integer endLoc = row[6] != null ? ((Number) row[6]).intValue() : null;
                String storedSeqRef = (String) row[7];

                if (assemblyName == null || chromosome == null || startLoc == null || endLoc == null) {
                    skippedNoCoords++;
                    continue;
                }

                AssemblyEnum assembly = assemblyFor(assemblyName);
                if (assembly == null) {
                    skippedFasta++;
                    continue;
                }

                String expected;
                try {
                    expected = new String(glService.getReferenceSequence(
                            assembly, chromosome, startLoc, endLoc).getBases()).toUpperCase();
                } catch (RuntimeException e) {
                    skippedFasta++;
                    LOG.info("Skipping " + featureZdbId
                            + " (" + assemblyName + " " + chromosome + ":" + startLoc + "-" + endLoc
                            + "): " + e.getMessage());
                    continue;
                }

                String stored = storedSeqRef.toUpperCase();
                if (!expected.equals(stored)) {
                    List<String> outRow = new ArrayList<>(9);
                    outRow.add(featureZdbId);
                    outRow.add(featureAbbrev == null ? "" : featureAbbrev);
                    outRow.add(featureType == null ? "" : featureType);
                    outRow.add(assemblyName);
                    outRow.add(chromosome);
                    outRow.add(String.valueOf(startLoc));
                    outRow.add(String.valueOf(endLoc));
                    outRow.add(stored);
                    outRow.add(expected);
                    mismatches.add(outRow);
                }
            }
        } catch (Exception e) {
            LOG.error("Drift check failed", e);
            errorMessages.add(e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            HibernateUtil.closeSession();
        }

        LOG.info("Examined " + examined + " FGMD rows; "
                + mismatches.size() + " mismatches; "
                + skippedNoCoords + " skipped (no usable coords); "
                + skippedFasta + " skipped (FASTA unavailable / out-of-range)");

        createErrorReport(errorMessages, mismatches);
        return 0;
    }

    private AssemblyEnum assemblyFor(String name) {
        for (AssemblyEnum ae : AssemblyEnum.values()) {
            if (ae.getName().equals(name)) {
                return ae;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String instance = args[0];
        String jobName = args[1];
        String directory = args[2];
        String propertyFilePath = args[3];
        CheckSequenceOfReferenceDriftTask task = new CheckSequenceOfReferenceDriftTask(
                jobName, propertyFilePath, directory);
        task.setInstance(instance);
        task.initDatabase();
        System.exit(task.execute());
    }
}
