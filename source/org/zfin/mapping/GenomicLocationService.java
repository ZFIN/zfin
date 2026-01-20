package org.zfin.mapping;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.commons.lang.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.HibernateFeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.sequence.gff.AssemblyEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.gwt.root.dto.FeatureTypeEnum.*;
import static org.zfin.util.ZfinCollectionUtils.isIn;


public class GenomicLocationService {

    public static final String FASTA_URL_BASE_DIR = "/opt/zfin/gff3/";
    public static final String FASTA_GENOMIC_Z11_URL = FASTA_URL_BASE_DIR + "Danio_rerio.fa";
    public static final String FASTA_GENOMIC_Z12_FILE = FASTA_URL_BASE_DIR + "GCF_049306965.1_GRCz12tu_genomic.fna";

    private FeatureRepository featureRepository = new HibernateFeatureRepository();
    private PublicationRepository pubRepo = new HibernatePublicationRepository();
    private String pathToBlast = FASTA_URL_BASE_DIR;

    public GenomicLocationService(String pathToBlast) {
        this.pathToBlast = pathToBlast;
    }

    public GenomicLocationService() {
    }

    private IndexedFastaSequenceFile getIndexedFastaSequenceFile(AssemblyEnum assembly) {
        String pathname = null;
        switch (assembly) {
            case GRCZ12TU -> pathname = pathToBlast + FASTA_GENOMIC_Z12_FILE;
            case GRCZ11 -> pathname = pathToBlast + FASTA_GENOMIC_Z11_URL;
        }
        return getIndexedFastaSequenceFile(pathname);
    }

    private IndexedFastaSequenceFile getIndexedFastaSequenceFile(String fullPath) {
        File fasta = new File(fullPath);
        try {
            return new IndexedFastaSequenceFile(fasta);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ReferenceSequence getReferenceSequence(AssemblyEnum assembly, String chromosome, int start, int end) {
        return getIndexedFastaSequenceFile(assembly).getSubsequenceAt(chromosome, start, end);
    }

    // create a new FeatureGenomicMutationDetail object if not exists
    // remove variant sequence if FeatureGenomicMutationDetail is null or empty (cleanup)
    public void upsertFlankingSequence(Feature feature, AssemblyEnum assembly) {
        FeatureLocation ftrLoc = featureRepository.getAllFeatureLocationsForAssembly(assembly, feature);

        // there is a sequence_feature_chromosome_location record / landmark
        // then
        if (isIn(feature.getType(), INDEL, DELETION, INSERTION, MNV, POINT_MUTATION)) {
            if (featureLocationIsNotEmpty(ftrLoc)) {
                String ftrChrom = ftrLoc.getChromosome();
                int locStart = ftrLoc.getStartLocation();
                int locEnd = ftrLoc.getEndLocation();
                String refSeq = new String(getReferenceSequence(assembly, ftrChrom, locStart, locEnd).getBases());
                // create a new record
                if (feature.getFeatureGenomicMutationDetail() == null) {
                    insertFeatureGenomeRecord(feature, refSeq);
                }
//                checkForInconsistentBetweenFgmdAndReferenceFA(feature, ref, ftrChrom, locStart, locEnd);
                String seq1 = "";
                String seq2 = "";
                // number of nucleotides upstream or downstream
                int offset = 500;
                IndexedFastaSequenceFile ref = getIndexedFastaSequenceFile(assembly);
                int leftOffset = locStart - offset;
                if (leftOffset < 1) {
                    leftOffset = 1;
                }
                switch (feature.getType()) {
                    case POINT_MUTATION -> {
                        if (StringUtils.isEmpty(feature.getFeatureGenomicMutationDetail().getFgmdSeqRef())) {
                            updateFeatureGenomeRecord(feature.getFeatureGenomicMutationDetail(), refSeq);
                        }
                        seq1 = new String(ref.getSubsequenceAt(ftrChrom, leftOffset, locStart - 1).getBases());
                        seq2 = new String(ref.getSubsequenceAt(ftrChrom, locStart + 1, locStart + offset).getBases());
                    }
                    case DELETION, MNV -> {
                        seq1 = new String(ref.getSubsequenceAt(ftrChrom, leftOffset, locStart - 1).getBases());
                        seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd + 1, locEnd + offset).getBases());
                    }
                    case INSERTION -> {
                        seq1 = new String(ref.getSubsequenceAt(ftrChrom, leftOffset, locStart).getBases());
                        seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd, locEnd + offset).getBases());
                    }
                    case INDEL -> {
                        if (StringUtils.isEmpty(feature.getFeatureGenomicMutationDetail().getFgmdSeqRef())) {
                            updateFeatureGenomeRecord(feature.getFeatureGenomicMutationDetail(), refSeq);
                        }
                        seq1 = new String(ref.getSubsequenceAt(ftrChrom, leftOffset, locStart - 1).getBases());
                        seq2 = new String(ref.getSubsequenceAt(ftrChrom, locEnd + 1, locEnd + offset).getBases());
                    }
                }
                insertOrUpdateFlankSeq(feature, seq1, seq2, offset);
            } else {
                // Location is empty/deleted - clean up related records
                // Delete variant_flanking_sequence if it exists
                VariantSequence vrSeq = featureRepository.getFeatureVariant(feature);
                if (vrSeq != null) {
                    HibernateUtil.currentSession().delete(vrSeq);
                }
                // Delete feature_genomic_mutation_detail if it exists
                FeatureGenomicMutationDetail fgmd = feature.getFeatureGenomicMutationDetail();
                if (fgmd != null) {
                    feature.setFeatureGenomicMutationDetail(null);
                    HibernateUtil.currentSession().delete(fgmd);
                }
            }
        }
    }

    private void insertOrUpdateFlankSeq(Feature ftr, String seq1, String seq2, int offset) {
        VariantSequence vrSeq = featureRepository.getFeatureVariant(ftr);
        boolean newSequence = vrSeq == null;
        if (newSequence) {
            vrSeq = new VariantSequence();
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
        boolean updateMade = hasFlankSeqChanged(vrSeq,
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
        }
        if (newSequence) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setPublication(pubRepo.getPublication("ZDB-PUB-191030-9"));
            pa.setDataZdbID(vrSeq.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            currentSession().save(pa);
        }
/*
        if (changed) {
            if (newSequence) {
                this.updated.add(List.of("NEW: " + ftr.getZdbID(), seq1, seq2));
            } else {
                this.updated.add(List.of("UPDATED: " + ftr.getZdbID(), seq1, seq2));
            }
        }
*/
    }

    private void updateFeatureGenomeRecord(FeatureGenomicMutationDetail fgmd, String seqRef) {
        fgmd.setFgmdSeqRef(seqRef);
        HibernateUtil.currentSession().update(fgmd);

    }

    private boolean hasFlankSeqChanged(VariantSequence vrSeq, String zdbID, String vfsLeftEnd, String vfsRightEnd, int vfsOffsetStart, int vfsOffsetStop, String vfsFlankType, String vfsFlankOrigin, String vfsType, String vfsTargetSequence, String vfsVariation) {
        boolean changed = false;
        if (zdbID == null) {
            System.out.println("zdbID is null");
        }
        if (vrSeq.getVseqDataZDB() == null) {
            System.out.println("vseqDataZDB is null");
        }
        if (!Objects.equals(vrSeq.getVseqDataZDB(), zdbID)) {
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

    private void insertFeatureGenomeRecord(Feature ftr, String seqRef) {

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

    private boolean featureLocationIsNotEmpty(FeatureLocation ftrLoc) {
        return ftrLoc != null && ftrLoc.containsLocationData();
    }
}



