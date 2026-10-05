package org.zfin.datatransfer.flankingsequence;

import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.HibernateFeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.mapping.FeatureLocation;
import org.zfin.mapping.GenomicLocationService;
import org.zfin.mapping.VariantSequence;
import org.zfin.sequence.gff.AssemblyEnum;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.LocalDate.now;
import static org.zfin.gwt.root.dto.FeatureTypeEnum.*;
import static org.zfin.util.ZfinCollectionUtils.isIn;
import static org.zfin.util.ZfinSystemUtils.env;
import static org.zfin.util.ZfinSystemUtils.envTrue;


/**
 * Catches up variant_flanking_sequence for feature/location/mutation-detail data written
 * outside the interactive curation save (e.g. bulk loaders, liftover migrations) --
 * FeatureRPCServiceImpl's on-save recalculation never sees those changes. Delegates the
 * actual computation to GenomicLocationService.upsertFlankingSequence(), the same method
 * the interactive save uses, resolved per-feature against whichever assembly currently
 * ranks highest (GenomicLocationService.supportedAssemblyFor(), backed by the assembly
 * table's a_order) instead of the GRCz11-only logic this class used to duplicate inline.
 * See ZFIN-10486.
 */
public class FlankSeqProcessor {


    //Add option to check for inconsistencies between .fa file and sequence of reference
    //Can set environment variable CHECK_FOR_INCONSISTENCIES to true to enable
    private static boolean CHECK_FOR_INCONSISTENCIES = false;

    /** Features processed between Hibernate session flush/clear cycles (see the main loop). */
    private static final int SESSION_CLEAR_INTERVAL = 500;

    private FeatureRepository featureRepository = new HibernateFeatureRepository();
    private GenomicLocationService glService = new GenomicLocationService();
    private Logger logger = LogManager.getLogger(FlankSeqProcessor.class);
    private List<String> messages = new ArrayList<>();
    private List<List<String>> updated = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public FlankSeqProcessor() {

    }


    public void updateFlankingSequences() {
        try {
            CHECK_FOR_INCONSISTENCIES = envTrue("CHECK_FOR_INCONSISTENCIES");
            String singleFeatureZdbID = env("FEATURE_ZDBID");
            Boolean processSingleFeature = StringUtils.isNotBlank(singleFeatureZdbID);
            if (processSingleFeature) {
                System.out.println("Processing single feature: " + singleFeatureZdbID);
            }

            try {
                HibernateUtil.createTransaction();

                //We are not loading flanking sequences for sa alleles. They have already been loaded via a one time SQL script.
                List<Feature> deletionFeatures = featureRepository.getDeletionFeatures(singleFeatureZdbID);
                System.out.println("deletionFeatures.size() = " + deletionFeatures.size());

                for (Feature feature : deletionFeatures) {

                    FeatureLocation ftrLoc = featureRepository.getLocationByFeature(feature);
                    AssemblyEnum assembly = featureLocationIsNotEmpty(ftrLoc)
                            ? GenomicLocationService.supportedAssemblyFor(ftrLoc.getAssembly()) : null;

                    if (assembly != null) {
                        // Only bootstrap brand-new DELETION features here (no FGMD yet) --
                        // one with an existing FGMD is left for the loop below, which only
                        // recomputes features actually modified recently (or on the Tuesday
                        // full sweep), same scope as before this method became assembly-aware.
                        if (feature.getFeatureGenomicMutationDetail() == null) {
                            System.out.print(".");
                            recomputeAndTrack(feature, assembly);
                        }
                        checkForInconsistentBetweenFgmdAndReferenceFA(feature, assembly, ftrLoc);
                    } else {
                        logger.debug("Feature " + feature.getZdbID() + " has no location on a supported assembly");
                        //This means that we may need to delete variant sequences for this feature if they exist.
                        VariantSequence vrSeq = featureRepository.getFeatureVariant(feature);
                        FeatureGenomicMutationDetail fgmd = feature.getFeatureGenomicMutationDetail();
                        if (fgmd == null && vrSeq != null) {
                            //We need to delete the variant sequence as there is no genomic mutation detail associated with this feature.
                            HibernateUtil.currentSession().delete(vrSeq);
                            System.out.println("Deleted variant sequence for feature " + feature.getZdbID());
                            this.updated.add(List.of("Deleted variant sequence for feature " + feature.getZdbID(), "", ""));
                        }
                    }
                }
                System.out.println("");

                //Once a week, get all non-sa features with genomic mutation details and update their flanking sequences.
                //Otherwise, just those that have a modified date in the last 48 hours (24 should be fine, but 48 for caution.
                boolean isItTuesday = (now().getDayOfWeek().getValue() == 2);
                boolean forceFullUpdate = envTrue("FORCE_FULL_UPDATE"); // set to true to force a full update of all non-sa features with genomic mutation details
                // "sa" (Sanger allele) features are ~36k of the ~42k rows, so they're excluded
                // by default purely to keep this job's routine runtime down (6,362 features per
                // full sweep instead of 41,914). Set INCLUDE_SANGER=true for a one-time backfill
                // that also reaches them (ZFIN-10486: pair with FORCE_FULL_UPDATE=true) -- that
                // combination covers exactly the same features as dropping the name exclusion
                // would, which is why the exclusion stays: this job is slated for retirement, so
                // it isn't worth permanently multiplying its nightly cost by ~6.6x.
                //
                // The flag is only about scope. Features whose flanking sequence was *submitted*
                // rather than computed are excluded by getNonSaFeaturesWithGenomicMutDets()
                // regardless, so no flag combination can overwrite submitter data.
                boolean includeSanger = envTrue("INCLUDE_SANGER");
                List<Feature> nonSaFeaturesWithGenomicMutDets;
                if (isItTuesday || forceFullUpdate || processSingleFeature) {
                    System.out.println("Updating all non-sa features with genomic mutation details" + (includeSanger ? " (including sa)" : ""));
                    nonSaFeaturesWithGenomicMutDets = featureRepository.getNonSaFeaturesWithGenomicMutDets(null, singleFeatureZdbID, includeSanger);
                } else {
                    System.out.println("Updating non-sa features with genomic mutation details modified in the last 48 hours" + (includeSanger ? " (including sa)" : ""));
                    nonSaFeaturesWithGenomicMutDets = featureRepository.getNonSaFeaturesWithGenomicMutDets(Date.valueOf(now().minusDays(2)), null, includeSanger);
                }
                System.out.println("nonSaFeaturesWithGenomicMutDets.size() = " + nonSaFeaturesWithGenomicMutDets.size());

                // Iterate by id and reload each feature, so the periodic session clear below
                // can't detach an entity we still hold a reference to (a cleared Feature would
                // throw LazyInitializationException on getFeatureGenomicMutationDetail()).
                List<String> candidateIds = nonSaFeaturesWithGenomicMutDets.stream()
                        .map(Feature::getZdbID).toList();
                nonSaFeaturesWithGenomicMutDets = null;
                HibernateUtil.currentSession().flush();
                HibernateUtil.currentSession().clear();

                int i = 0;
                for (String candidateId : candidateIds) {
                    Feature feature = featureRepository.getFeatureByID(candidateId);
                    if (feature != null && isIn(feature.getType(), INDEL, DELETION, INSERTION, MNV, POINT_MUTATION)) {
                        FeatureLocation ftrLoc = featureRepository.getLocationByFeature(feature);
                        AssemblyEnum assembly = featureLocationIsNotEmpty(ftrLoc)
                                ? GenomicLocationService.supportedAssemblyFor(ftrLoc.getAssembly()) : null;
                        if (assembly != null) {
                            i++;
                            checkForInconsistentBetweenFgmdAndReferenceFA(feature, assembly, ftrLoc);
                            recomputeAndTrack(feature, assembly);

                            //progress output
                            System.out.print(".");
                            System.out.flush();
                            if (i % 100 == 0) {
                                System.out.println("\n " + i + " / " + candidateIds.size() + " features processed");
                            }
                            // An INCLUDE_SANGER full sweep walks ~42k features in this one
                            // transaction; without periodically flushing and detaching them the
                            // persistence context grows all run and the flush at commit has to
                            // dirty-check every entity it ever loaded. (CheckSequenceOfReferenceDriftTask
                            // documents the same pitfall bringing the JVM to a halt.)
                            if (i % SESSION_CLEAR_INTERVAL == 0) {
                                HibernateUtil.currentSession().flush();
                                HibernateUtil.currentSession().clear();
                            }
                        }
                    }
                }
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (NullPointerException e) {
                System.err.println("Cannot fetch flanking sequence: " + e);
                e.printStackTrace();
                logger.error(e);
                errors.add(ExceptionUtils.getFullStackTrace(e));
            }
            HibernateUtil.closeSession();
        } catch (Exception e) {
            logger.error(e);
            errors.add(ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * Recomputes and saves the feature's flanking sequence via the same logic the interactive
     * curation save uses, recording what changed for the Jenkins report. upsertFlankingSequence()
     * reports that itself, so this doesn't re-read the row to diff it.
     */
    private void recomputeAndTrack(Feature feature, AssemblyEnum assembly) {
        GenomicLocationService.UpsertResult result = glService.upsertFlankingSequence(feature, assembly);
        if (!result.wroteAnything()) {
            return;
        }
        VariantSequence current = featureRepository.getFeatureVariant(feature);
        this.updated.add(List.of(result + ": " + feature.getZdbID(),
                current != null ? current.getVfsLeftEnd() : "",
                current != null ? current.getVfsRightEnd() : ""));
    }

    private void checkForInconsistentBetweenFgmdAndReferenceFA(Feature feature, AssemblyEnum assembly, FeatureLocation ftrLoc) {
        FeatureGenomicMutationDetail fgmd = feature.getFeatureGenomicMutationDetail();
        if (fgmd == null || StringUtils.isEmpty(fgmd.getFgmdSeqRef())) {
            return;
        }
        if (CHECK_FOR_INCONSISTENCIES) {
            String ftrChrom = ftrLoc.getChromosome();
            int locStart = ftrLoc.getStartLocation();
            int locEnd = ftrLoc.getEndLocation();
            ReferenceSequence referenceSequence;
            try {
                referenceSequence = glService.getReferenceSequence(assembly, ftrChrom, locStart, locEnd);
            } catch (RuntimeException e) {
                logger.warn("Could not read reference sequence for " + feature.getZdbID() + ": " + e.getMessage());
                return;
            }
            String refSeq = new String(referenceSequence.getBases());
            if (!refSeq.equals(fgmd.getFgmdSeqRef())) {
                boolean caseOnlyMismatch = refSeq.equalsIgnoreCase(fgmd.getFgmdSeqRef());
                String caseOnlyMessage = caseOnlyMismatch ? " (case-only mismatch)" : "";

                boolean separatorOnlyMismatch = false;
                if (fgmd.getFgmdSeqRef().replaceAll("\\s+", "").equals(refSeq.replaceAll("\\s+", ""))) {
                    separatorOnlyMismatch = true;
                }
                if (!separatorOnlyMismatch) {
                    String message = "\nInconsistency," + feature.getType() + "," + feature.getZdbID() +
                                     "," + fgmd.getFgmdSeqRef() + "," + refSeq + "," +
                                     ftrChrom + ":" + locStart + "-" + locEnd + "," + caseOnlyMessage;
                    System.out.println(message);
                    logger.warn(message);
                }
            }
        }
    }

    private static boolean featureLocationIsNotEmpty(FeatureLocation ftrLoc) {
        return ftrLoc != null && ftrLoc.containsLocationData();
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<List<String>> getUpdated() {
        return updated;
    }

    public static void main(String[] args) {
        CHECK_FOR_INCONSISTENCIES = envTrue("CHECK_FOR_INCONSISTENCIES");
        try {
            FlankSeqProcessor driver = new FlankSeqProcessor();
            driver.updateFlankingSequences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
