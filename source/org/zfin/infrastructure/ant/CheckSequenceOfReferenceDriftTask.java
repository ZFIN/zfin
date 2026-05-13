package org.zfin.infrastructure.ant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.hibernate.query.Query;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.framework.HibernateUtil;
import org.zfin.mapping.FeatureLocation;
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

        HibernateUtil.currentSession().beginTransaction();
        List<List<String>> mismatches = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        int examined = 0;
        int skippedNoCoords = 0;
        int skippedFasta = 0;

        try {
            // Single HQL pass: every FGMD with a sequence of reference, joined to its
            // Feature. We pull FeatureLocation separately via fl-on-feature criteria,
            // since FeatureLocation isn't mapped as an association on Feature.
            String hql = "from FeatureGenomicMutationDetail fgmd " +
                    "where fgmd.fgmdSeqRef is not null " +
                    "order by fgmd.feature.zdbID";
            Query<FeatureGenomicMutationDetail> query = HibernateUtil.currentSession()
                    .createQuery(hql, FeatureGenomicMutationDetail.class);

            GenomicLocationService glService = new GenomicLocationService();

            for (FeatureGenomicMutationDetail fgmd : query.list()) {
                examined++;
                Feature feature = fgmd.getFeature();
                FeatureLocation fl = featureLocationFor(feature);

                if (fl == null || fl.getStartLocation() == null || fl.getEndLocation() == null
                        || fl.getAssembly() == null || fl.getChromosome() == null) {
                    skippedNoCoords++;
                    continue;
                }

                AssemblyEnum assembly = assemblyFor(fl.getAssembly());
                if (assembly == null) {
                    skippedFasta++;
                    continue;
                }

                String expected;
                try {
                    expected = new String(glService.getReferenceSequence(
                            assembly,
                            fl.getChromosome(),
                            fl.getStartLocation(),
                            fl.getEndLocation()).getBases()).toUpperCase();
                } catch (RuntimeException e) {
                    // FASTA file missing for this assembly, chromosome not in the
                    // index, or coordinates out of range — log + skip; not a
                    // sequence-of-reference drift per se.
                    skippedFasta++;
                    LOG.info("Skipping " + feature.getZdbID()
                            + " (" + fl.getAssembly() + " " + fl.getChromosome() + ":"
                            + fl.getStartLocation() + "-" + fl.getEndLocation()
                            + "): " + e.getMessage());
                    continue;
                }

                String stored = fgmd.getFgmdSeqRef().toUpperCase();
                if (!expected.equals(stored)) {
                    List<String> row = new ArrayList<>(9);
                    row.add(feature.getZdbID());
                    row.add(feature.getAbbreviation() == null ? "" : feature.getAbbreviation());
                    row.add(feature.getType() == null ? "" : feature.getType().name());
                    row.add(fl.getAssembly());
                    row.add(fl.getChromosome());
                    row.add(String.valueOf(fl.getStartLocation()));
                    row.add(String.valueOf(fl.getEndLocation()));
                    row.add(stored);
                    row.add(expected);
                    mismatches.add(row);
                }
            }

            HibernateUtil.currentSession().getTransaction().commit();
        } catch (Exception e) {
            LOG.error("Drift check failed", e);
            HibernateUtil.rollbackTransaction();
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

    private FeatureLocation featureLocationFor(Feature feature) {
        // Avoid pulling the whole repository; one targeted HQL.
        List<FeatureLocation> matches = HibernateUtil.currentSession()
                .createQuery("from FeatureLocation where feature.zdbID = :id", FeatureLocation.class)
                .setParameter("id", feature.getZdbID())
                .list();
        return matches.isEmpty() ? null : matches.get(0);
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
