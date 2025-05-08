package org.zfin.datatransfer.flankingsequence;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.HibernateFeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.FeatureLocation;
import org.zfin.mapping.VariantSequence;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.zfin.framework.HibernateUtil.currentSession;


/**
 *
 */
public class FlankSeqProcessor {

    public static final String FASTA_URL = "/research/zprodmore/gff3/Danio_rerio.fa";

    private FeatureRepository featureRepository = new HibernateFeatureRepository();
    private PublicationRepository pubRepo = new HibernatePublicationRepository();
    private Logger logger = LogManager.getLogger(FlankSeqProcessor.class);
    private List<String> messages = new ArrayList<>();
    private List<List<String>> updated = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public FlankSeqProcessor() {

    }


    public void updateFlankingSequences() {
        try {

            File fasta = new File(FASTA_URL);
            IndexedFastaSequenceFile ref = new IndexedFastaSequenceFile(fasta);
            int locStart = 0;
            int locEnd = 0;
            int offset = 500;
            String seq1 = "";
            String seq2 = "";


            try {
                HibernateUtil.createTransaction();

                //We are not loading flanking sequences for sa alleles. They have already been loaded via a one time SQL script.
                List<Feature> deletionFeatures = featureRepository.getDeletionFeatures();
                System.out.println("deletionFeatures.size() = " + deletionFeatures.size());

                for (Feature feature : deletionFeatures) {

                    //get Optional
                    FeatureLocation ftrLoc = featureRepository.getAllFeatureLocationsOnGRCz11(feature);

                    if (ftrLoc != null
                            && ftrLoc.getStartLocation() != null && ftrLoc.getStartLocation().toString() != ""
                            && ftrLoc.getEndLocation() != null && ftrLoc.getEndLocation().toString() != ""
                            && ftrLoc.getAssembly() != null

                    ) {
                        String ftrChrom = ftrLoc.getChromosome();
                        locStart = ftrLoc.getStartLocation();
                        locEnd = ftrLoc.getEndLocation();
                        if (feature.getFeatureGenomicMutationDetail() == null) {
                            String refSeq = new String(ref.getSubsequenceAt(ftrChrom, locStart, locEnd).getBases());
                            System.out.print(".");
                            InsertFeatureGenomeRecord(feature, refSeq);
                        }
                    }
                }
                System.out.println("");

                List<Feature> nonSaFeaturesWithGenomicMutDets = featureRepository.getNonSaFeaturesWithGenomicMutDets();
                System.out.println("nonSaFeaturesWithGenomicMutDets.size() = " + nonSaFeaturesWithGenomicMutDets.size());

                int i = 0;
                for (Feature feature : nonSaFeaturesWithGenomicMutDets) {
                    if (feature.getType() == FeatureTypeEnum.INDEL || feature.getType() == FeatureTypeEnum.DELETION || feature.getType() == FeatureTypeEnum.INSERTION || feature.getType() == FeatureTypeEnum.MNV || feature.getType() == FeatureTypeEnum.POINT_MUTATION) {
                        FeatureLocation ftrLoc = featureRepository.getAllFeatureLocationsOnGRCz11(feature);
                        if (ftrLoc != null
                                && ftrLoc.getStartLocation() != null && ftrLoc.getStartLocation().toString() != ""
                                && ftrLoc.getEndLocation() != null && ftrLoc.getEndLocation().toString() != ""
                                && ftrLoc.getAssembly() != null
                        ) {
                            i++;
                            String ftrChrom = ftrLoc.getChromosome();
                            locStart = ftrLoc.getStartLocation();
                            locEnd = ftrLoc.getEndLocation();
                            switch (feature.getType()) {
                                case POINT_MUTATION:
                                    if (StringUtils.isEmpty(feature.getFeatureGenomicMutationDetail().getFgmdSeqRef())) {
                                        String refSeq = new String(ref.getSubsequenceAt(ftrChrom, locStart, locEnd).getBases());
                                        UpdateFeatureGenomeRecord(feature.getFeatureGenomicMutationDetail(), refSeq);
                                        this.updated.add(List.of("Update FGMD:" + feature.getZdbID(), refSeq, "POINT_MUTATION"));
                                    }
                                    seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart - 1).getBases());
                                    seq2 = new String(ref.getSubsequenceAt(ftrChrom, locStart + 1, locStart + offset).getBases());

                                    break;
                                case DELETION:
                                    seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart - 1).getBases());
                                    seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd + 1, locEnd + offset).getBases());
                                    break;
                                case INSERTION:
                                    seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart).getBases());
                                    seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd, locEnd + offset).getBases());
                                    break;
                                case INDEL:
                                    if (StringUtils.isEmpty(feature.getFeatureGenomicMutationDetail().getFgmdSeqRef())) {
                                        String refSeq = new String(ref.getSubsequenceAt(ftrChrom, locStart, locEnd).getBases());
                                        UpdateFeatureGenomeRecord(feature.getFeatureGenomicMutationDetail(), refSeq);
                                        this.updated.add(List.of("Update FGMD:" + feature.getZdbID(), refSeq, "INDEL"));
                                    }

                                    seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart - 1).getBases());
                                    seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd + 1, locEnd + offset).getBases());

                                    break;
                                case MNV:
                                    seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart - 1).getBases());
                                    seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd + 1, locEnd + offset).getBases());
                                    break;
                            }

                            //progress output
                            System.out.print(".");
                            System.out.flush();
                            if (i % 100 == 0) {
                                System.out.println("\n " + i + " / " + nonSaFeaturesWithGenomicMutDets.size() + " features processed");
                            }

                            insertFlankSeq(feature, seq1, seq2, offset);
                        }

                    }
                }
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (NullPointerException e) {
                System.err.println("Cannot fetch sequence for fasta file " + fasta);
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


    private void UpdateFeatureGenomeRecord(FeatureGenomicMutationDetail fgmd, String seqRef) {
        fgmd.setFgmdSeqRef(seqRef);
        HibernateUtil.currentSession().update(fgmd);

    }

    private void InsertFeatureGenomeRecord(Feature ftr, String seqRef) {

        FeatureGenomicMutationDetail fgmd = new FeatureGenomicMutationDetail();
        fgmd.setFeature(ftr);
        fgmd.setFgmdSeqRef(seqRef);
        fgmd.setFeature(ftr);
        fgmd.setFgmdVarStrand("+");
        HibernateUtil.currentSession().save(fgmd);

        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().update(fgmd);
        HibernateUtil.currentSession().refresh(fgmd);
        HibernateUtil.currentSession().update(ftr);
        HibernateUtil.currentSession().refresh(ftr);
        this.updated.add(List.of("New FGMD: " +ftr.getZdbID(), seqRef, "+"));
    }

    private void insertFlankSeq(Feature ftr, String seq1, String seq2, int offset) {
        boolean newSequence = featureRepository.getFeatureVariant(ftr) == null;
        VariantSequence vrSeq;
        if (newSequence) {
            vrSeq = new VariantSequence();
        } else {
            vrSeq = featureRepository.getFeatureVariant(ftr);
        }
        String vfsTargetSequence = null;
        String vfsVariation = null;
        switch (ftr.getType()) {
            case DELETION -> {
                vfsTargetSequence = seq1 + "[" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + "-" + "]" + seq2;
                vfsVariation = ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + "-";
            }
            case INDEL -> {
                if (ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef().length() != 0) {
                    vfsTargetSequence = seq1 + "[" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar() + "]" + seq2;
                    vfsVariation = ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar();
                }
            }
            case INSERTION -> {
                vfsTargetSequence = seq1 + "[" + "-" + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar() + "]" + seq2;
                vfsVariation = "-" + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar();
            }
            case POINT_MUTATION, MNV -> {
                vfsTargetSequence = seq1 + "[" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar() + "]" + seq2;
                vfsVariation = ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar();
            }
        }
        boolean updateMade = this.setFlankSeqIfChanged(vrSeq,
                ftr.getZdbID(),
                seq1,
                seq2,
                offset,
                offset,
                "genomic",
                "directly sequenced",
                "Genomic",
                vfsTargetSequence,
                vfsVariation);
        boolean changed = newSequence || updateMade;

        try {
            if (changed) {
                HibernateUtil.currentSession().save(vrSeq);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Insertion failed at ...");
            System.exit(1);
        }
        if (newSequence) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setPublication(pubRepo.getPublication("ZDB-PUB-191030-9"));
            pa.setDataZdbID(vrSeq.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            currentSession().save(pa);
        }
        if (changed) {
            if (newSequence) {
                this.updated.add(List.of("NEW: " + ftr.getZdbID(), seq1, seq2));
            } else {
                this.updated.add(List.of("UPDATED: " + ftr.getZdbID(), seq1, seq2));
            }
        }
    }

    private boolean setFlankSeqIfChanged(VariantSequence vrSeq, String zdbID, String vfsLeftEnd, String vfsRightEnd, int vfsOffsetStart, int vfsOffsetStop, String vfsFlankType, String vfsFlankOrigin, String vfsType, String vfsTargetSequence, String vfsVariation) {
        boolean changed = false;
        if (zdbID == null) {
            System.out.println("zdbID is null");
        }
        if (vrSeq.getVseqDataZDB() == null) {
            System.out.println("vseqDataZDB is null");
        }
        if (!Objects.equals(vrSeq.getVseqDataZDB(),zdbID)) {
            vrSeq.setVseqDataZDB(zdbID);
            changed = true;
        }
        if (!Objects.equals(vrSeq.getVfsLeftEnd(), vfsLeftEnd)) {
            vrSeq.setVfsLeftEnd(vfsLeftEnd);
            changed = true;
        }
        if (!Objects.equals(vrSeq.getVfsRightEnd(), vfsRightEnd)) {
            vrSeq.setVfsRightEnd(vfsRightEnd);
            changed = true;
        }
        if (vrSeq.getVfsOffsetStart() != vfsOffsetStart) {
            vrSeq.setVfsOffsetStart(vfsOffsetStart);
            changed = true;
        }
        if (vrSeq.getVfsOffsetStop() != vfsOffsetStop) {
            vrSeq.setVfsOffsetStop(vfsOffsetStop);
            changed = true;
        }
        if (!Objects.equals(vrSeq.getVfsFlankType(), vfsFlankType)) {
            vrSeq.setVfsFlankType(vfsFlankType);
            changed = true;
        }
        if (!Objects.equals(vrSeq.getVfsFlankOrigin(), vfsFlankOrigin)) {
            vrSeq.setVfsFlankOrigin(vfsFlankOrigin);
            changed = true;
        }
        if (!Objects.equals(vrSeq.getVfsType(), vfsType)) {
            vrSeq.setVfsType(vfsType);
            changed = true;
        }
        if (vfsTargetSequence != null && !Objects.equals(vrSeq.getVfsTargetSequence(), vfsTargetSequence)) {
            vrSeq.setVfsTargetSequence(vfsTargetSequence);
            changed = true;
        }
        if (vfsVariation != null && !Objects.equals(vrSeq.getVfsVariation(), vfsVariation)) {
            vrSeq.setVfsVariation(vfsVariation);
            changed = true;
        }
        return changed;
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
        try {
            FlankSeqProcessor driver = new FlankSeqProcessor();
            driver.updateFlankingSequences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
