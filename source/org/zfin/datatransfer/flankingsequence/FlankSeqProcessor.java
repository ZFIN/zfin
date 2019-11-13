package org.zfin.datatransfer.flankingsequence;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.HibernateFeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.mapping.FeatureLocation;
import org.zfin.mapping.VariantSequence;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;


/**
 *
 */
public class FlankSeqProcessor {

    private static final String MISSING_VARSEQS_FILE = "indel-missing-varseqs.txt";
    private FeatureRepository featureRepository = new HibernateFeatureRepository();
    private PublicationRepository pubRepo = new HibernatePublicationRepository();
    private Logger logger = LogManager.getLogger(FlankSeqProcessor.class);
    private List<String> messages = new ArrayList<>();
    private List<List<String>> updated = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private static Map<String, List<List<String>>> dataMap = new HashMap<>(20);

    public FlankSeqProcessor() {

    }


    public void updateFlankingSequences() {
        try {

            File fasta = new File("/research/zprodmore/gff3/Danio_rerio.fa");
            IndexedFastaSequenceFile ref = new IndexedFastaSequenceFile(fasta);
            int locStart = 0;
            int locEnd = 0;
            int offset = 500;
            String seq1 = "";
            String seq2 = "";
            // new Faidx(fasta);
            HibernateUtil.createTransaction();


            try {

                for (Feature feature : featureRepository.getFeaturesWithGenomicMutDets()) {



                    FeatureLocation ftrLoc = featureRepository.getAllFeatureLocationsOnGRCz11(feature);
                    if (ftrLoc != null
                            && ftrLoc.getSfclStart() != null && ftrLoc.getSfclStart().toString() != ""
                            && ftrLoc.getSfclEnd() != null && ftrLoc.getSfclEnd().toString() != ""
                            && ftrLoc.getSfclAssembly() != null

                            ) {
                        String ftrChrom = ftrLoc.getSfclChromosome();
                        locStart = ftrLoc.getSfclStart();
                        locEnd = ftrLoc.getSfclEnd();
                        if (feature.getType() == FeatureTypeEnum.DELETION) {
                            seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart - 1).getBases());
                            seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd + 1, locEnd + offset).getBases());
                        }

                        if (feature.getType() == FeatureTypeEnum.INSERTION) {
                            seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart).getBases());
                            seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd, locEnd + offset).getBases());
                        }
                        if (feature.getType() == FeatureTypeEnum.POINT_MUTATION) {
                            seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart).getBases());
                            seq2 = new String(ref.getSubsequenceAt(ftrChrom, locStart, locStart + offset).getBases());
                        }
                        if (feature.getType() == FeatureTypeEnum.INDEL) {

                            if (feature.getFeatureGenomicMutationDetail().getFgmdSeqRef().length() != 0) {
                                    /*String ftrChrom = ftrLoc.getSfclChromosome();
                                    FeatureGenomicMutationDetail fgmd = featureRepository.getFeatureGenomicDetail(feature);
                                    if (fgmd != null) {
                                        String seqRef = new String(ref.getSubsequenceAt(ftrChrom, locStart, locEnd).getBases());
                                        System.out.println(fgmd.getFeature().getZdbID() + ',' + seqRef + '\n');*/
                                      /*    fgmd.setFgmdSeqRef(seqRef);
                                          HibernateUtil.currentSession().save(fgmd);*/


                                seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart - 1).getBases());
                                seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd + 1, locEnd + offset).getBases());
                            }


                        }
                        if (feature.getType() == FeatureTypeEnum.MNV) {
                            seq1 = new String(ref.getSubsequenceAt(ftrChrom, locStart - offset, locStart - 1).getBases());
                            seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd + 1, locEnd + offset).getBases());
                        }
                        if (featureRepository.getFeatureVariant(feature) == null) {
                            insertFlankSeq(feature, seq1, seq2, offset);
                        } else {
                            updateFlankSeq(feature, seq1, seq2, offset);
                        }
                    }
                }


                HibernateUtil.flushAndCommitCurrentSession();
            } catch (NullPointerException e) {
                System.err.println("Cannot fetch sequence " +
                        " for fasta file " + fasta);
                e.printStackTrace();
            }

            HibernateUtil.currentSession().flush();

            HibernateUtil.closeSession();
        } catch (Exception e) {
            logger.error(e);
            errors.add(ExceptionUtils.getFullStackTrace(e));
        }
    }


    private void insertFlankSeq(Feature ftr, String seq1, String seq2, int offset) {
        VariantSequence vrSeq = new VariantSequence();
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
        PublicationAttribution pa = new PublicationAttribution();
        pa.setPublication(pubRepo.getPublication("ZDB-PUB-191030-9"));
        pa.setDataZdbID(vrSeq.getZdbID());
        pa.setSourceType(RecordAttribution.SourceType.STANDARD);
        currentSession().save(pa);


    }

    private void updateFlankSeq(Feature ftr, String seq1, String seq2, int offset) {
        VariantSequence vrSeq = featureRepository.getFeatureVariant(ftr);

        if (vrSeq != null) {
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
                vrSeq.setVfsTargetSequence(seq1 + "[" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar() + "]" + seq2);
                vrSeq.setVfsVariation(ftr.getFeatureGenomicMutationDetail().getFgmdSeqRef() + "/" + ftr.getFeatureGenomicMutationDetail().getFgmdSeqVar());
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
