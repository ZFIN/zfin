package org.zfin.datatransfer.flankingsequence;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;
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
import java.util.ArrayList;
import java.util.List;

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

    }

    private void insertFlankSeq(Feature ftr, String seq1, String seq2, int offset) {
        VariantSequence vrSeq = new VariantSequence();
        if (featureRepository.getFeatureVariant(ftr) != null) {
            vrSeq = featureRepository.getFeatureVariant(ftr);
        }
        vrSeq.setVseqDataZDB(ftr.getZdbID());
        vrSeq.setVfsLeftEnd(seq1);
        vrSeq.setVfsRightEnd(seq2);
        vrSeq.setVfsOffsetStart(offset);
        vrSeq.setVfsOffsetStop(offset);
        vrSeq.setVfsFlankType("genomic");
        vrSeq.setVfsFlankOrigin("directly sequenced");
        vrSeq.setVfsType("Genomic");
        if (ftr.getType() == FeatureTypeEnum.DELETION) {
            vrSeq.setVfsTargetSequence(seq1 + "[" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + "-" + "]" + seq2);
            vrSeq.setVfsVariation(ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + "-");
        }
        if (ftr.getType() == FeatureTypeEnum.INDEL) {
            if (ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef().length() != 0) {
                vrSeq.setVfsTargetSequence(seq1 + "[" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar() + "]" + seq2);
                vrSeq.setVfsVariation(ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar());
            }
        }
        if (ftr.getType() == FeatureTypeEnum.INSERTION) {
            vrSeq.setVfsTargetSequence(seq1 + "[" + "-" + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar() + "]" + seq2);
            vrSeq.setVfsVariation("-" + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar());
        }
        if (ftr.getType() == FeatureTypeEnum.POINT_MUTATION) {
            vrSeq.setVfsTargetSequence(seq1 + "[" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar() + "]" + seq2);
            vrSeq.setVfsVariation(ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar());
        }
        if (ftr.getType() == FeatureTypeEnum.MNV) {
            vrSeq.setVfsTargetSequence(seq1 + "[" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar() + "]" + seq2);
            vrSeq.setVfsVariation(ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar());
        }
        HibernateUtil.currentSession().save(vrSeq);
        if (featureRepository.getFeatureVariant(ftr) == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setPublication(pubRepo.getPublication("ZDB-PUB-191030-9"));
            pa.setDataZdbID(vrSeq.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            currentSession().save(pa);
        }
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
