package org.zfin.mapping;

import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.commons.lang.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.HibernateFeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.sequence.gff.AssemblyEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.gwt.root.dto.FeatureTypeEnum.*;
import static org.zfin.util.ZfinCollectionUtils.isIn;


public class GenomicLocationService {

	public static final String FASTA_URL_BASE_DIR = "/opt/zfin/gff3/";
	public static final String FASTA_GENOMIC_Z11_URL = "Danio_rerio.fa";
	public static final String FASTA_GENOMIC_Z12_FILE = "GCF_049306965.1_GRCz12tu_genomic.fna";

	/** Nucleotides of flanking sequence read either side of a feature. */
	public static final int FLANKING_OFFSET = 500;

	/** The feature types that get a computed flanking sequence at all. */
	public static final Set<FeatureTypeEnum> FLANKING_SEQUENCE_TYPES =
		Collections.unmodifiableSet(EnumSet.of(INDEL, DELETION, INSERTION, MNV, POINT_MUTATION));

	/** Attributed to every flanking-sequence row this code computes. */
	public static final String COMPUTED_FLANK_SEQ_PUB = "ZDB-PUB-191030-9";

	/**
	 * "Sanger Institute Zebrafish Mutation Project mutant data submission" -- flanking
	 * sequence rows attributed to this came from the submitter, not from any assembly FASTA
	 * (they also carry a 50bp offset this code never produces). Recomputing them would
	 * overwrite submitted data, so batch recalculation skips them regardless of scope flags.
	 */
	public static final String SUBMITTED_FLANK_SEQ_PUB = "ZDB-PUB-130425-4";

	private FeatureRepository featureRepository = new HibernateFeatureRepository();
	private PublicationRepository pubRepo = new HibernatePublicationRepository();
	private final Map<AssemblyEnum, IndexedFastaSequenceFile> fastaFilesByAssembly = new HashMap<>();

	public GenomicLocationService() {
	}

	private static String getFastaPath(AssemblyEnum assembly) {
		String pathname = null;
		switch (assembly) {
			case GRCZ12TU -> pathname = FASTA_URL_BASE_DIR + FASTA_GENOMIC_Z12_FILE;
			case GRCZ11 -> pathname = FASTA_URL_BASE_DIR + FASTA_GENOMIC_Z11_URL;
		}
		return pathname;
	}

	/**
	 * The assembly (by name, e.g. from FeatureLocation.getAssembly()) this app can
	 * currently compute sequence against, or null if it's a read-only legacy assembly
	 * with no FASTA on disk (today: anything but GRCz12tu/GRCz11). Shared by the
	 * interactive curation save, the flanking-sequence batch job and the drift report so
	 * they all apply the same "can we actually compute this" rule. Static: it reads no
	 * instance state, and callers (e.g. FeatureRPCServiceImpl, several times per save)
	 * shouldn't have to build a service plus its repositories for a string lookup.
	 */
	public static AssemblyEnum supportedAssemblyFor(String assemblyName) {
		if (assemblyName == null) {
			return null;
		}
		for (AssemblyEnum candidate : AssemblyEnum.values()) {
			if (candidate.getName().equals(assemblyName) && getFastaPath(candidate) != null) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Reused across calls on this instance -- opening an IndexedFastaSequenceFile re-reads
	 * the .fai index from disk, which is wasteful when a caller (e.g. the flanking-sequence
	 * batch job) processes many features against the same one or two assemblies.
	 */
	private IndexedFastaSequenceFile getIndexedFastaSequenceFile(AssemblyEnum assembly) {
		return fastaFilesByAssembly.computeIfAbsent(assembly, a -> getIndexedFastaSequenceFile(getFastaPath(a)));
	}

	/**
	 * Length of a chromosome in the given assembly, or null when the assembly has no such
	 * chromosome. Reads the .fai index only, so the sequence itself is never loaded -- and
	 * goes through the cached IndexedFastaSequenceFile rather than re-parsing the .fai per
	 * call, which matters because locationWithinChromosome() calls this once per feature.
	 */
	public Long getChromosomeLength(AssemblyEnum assembly, String chromosome) {
		if (getFastaPath(assembly) == null || chromosome == null) {
			return null;
		}
		FastaSequenceIndex index = getIndexedFastaSequenceFile(assembly).getIndex();
		return index.hasIndexEntry(chromosome) ? index.getIndexEntry(chromosome).getSize() : null;
	}

	/**
	 * The part of [from, to] that exists on the chromosome, or "" if none of it does. The flanking
	 * windows below run `offset` bases either side of a feature, so one near a contig boundary used
	 * to make htsjdk throw "Query asks for data past end of contig" as a bare 500.
	 */
	private String subsequence(IndexedFastaSequenceFile ref, String chromosome, int from, int to) {
		FastaSequenceIndex index = ref.getIndex();
		if (!index.hasIndexEntry(chromosome)) {
			// Callers reach this only for a location locationWithinChromosome() has already accepted,
			// so the contig is known to be present; returning "" keeps a broken invariant from
			// becoming an unhandled SAMException out of getSubsequenceAt().
			return "";
		}
		int start = Math.max(from, 1);
		int end = (int) Math.min(to, index.getIndexEntry(chromosome).getSize());
		if (start > end) {
			return "";
		}
		return new String(ref.getSubsequenceAt(chromosome, start, end).getBases());
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

	public record FlankingSequencePair(String fivePrime, String threePrime) {
	}

	/**
	 * The five-prime/three-prime flanking sequence for a feature type at a given location,
	 * read live from the assembly FASTA. Shared by upsertFlankingSequence()'s on-save
	 * recalculation and by CheckFlankingSequenceDriftTask's read-only comparison, so both
	 * always agree on what "correct" means.
	 */
	public FlankingSequencePair computeFlankingSequences(FeatureTypeEnum type, AssemblyEnum assembly, String chromosome, int locStart, int locEnd, int offset) {
		IndexedFastaSequenceFile ref = getIndexedFastaSequenceFile(assembly);
		int leftOffset = Math.max(locStart - offset, 1);
		String seq1 = "";
		String seq2 = "";
		switch (type) {
			case POINT_MUTATION -> {
				seq1 = subsequence(ref, chromosome, leftOffset, locStart - 1);
				seq2 = subsequence(ref, chromosome, locStart + 1, locStart + offset);
			}
			case DELETION, MNV, INDEL -> {
				seq1 = subsequence(ref, chromosome, leftOffset, locStart - 1);
				seq2 = subsequence(ref, chromosome, locEnd + 1, locEnd + offset);
			}
			case INSERTION -> {
				seq1 = subsequence(ref, chromosome, leftOffset, locStart);
				seq2 = subsequence(ref, chromosome, locEnd, locEnd + offset);
			}
			default -> {
			}
		}
		return new FlankingSequencePair(seq1, seq2);
	}

	/**
	 * Recomputes and saves the feature's flanking sequence from the assembly FASTA.
	 * Returns an UpsertResult saying whether a row was written and whether it was new, so
	 * batch callers can report what actually changed without re-querying the row themselves.
	 */
	// create a new FeatureGenomicMutationDetail object if not exists
	// remove variant sequence if FeatureGenomicMutationDetail is null or empty (cleanup)
	public UpsertResult upsertFlankingSequence(Feature feature, AssemblyEnum assembly) {
		FeatureLocation ftrLoc = featureRepository.getAllFeatureLocationsForAssembly(assembly, feature);

		// A stored location running past the end of its chromosome (bad legacy data) cannot be read
		// from the FASTA at all. Leave whatever sequences exist alone rather than throwing out of
		// the middle of a save -- the caller reports the out-of-range location as a validation error.
		if (featureLocationIsNotEmpty(ftrLoc) && !locationWithinChromosome(assembly, ftrLoc)) {
			return UpsertResult.UNCHANGED;
		}

		// there is a sequence_feature_chromosome_location record / landmark
		// then
		if (FLANKING_SEQUENCE_TYPES.contains(feature.getType())) {
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
				int offset = FLANKING_OFFSET;
				if (isIn(feature.getType(), POINT_MUTATION, INDEL)
						&& StringUtils.isEmpty(feature.getFeatureGenomicMutationDetail().getFgmdSeqRef())) {
					updateFeatureGenomeRecord(feature.getFeatureGenomicMutationDetail(), refSeq);
				}
				FlankingSequencePair flankingSequencePair = computeFlankingSequences(feature.getType(), assembly, ftrChrom, locStart, locEnd, offset);
				return insertOrUpdateFlankSeq(feature, flankingSequencePair.fivePrime(), flankingSequencePair.threePrime(), offset);
			} else {
				// Location is empty/deleted - clean up the flanking-sequence row.
				// We deliberately do NOT touch feature_genomic_mutation_detail here:
				// the Feature.featureGenomicMutationDetailSet mapping has multiple
				// cascade flags (PERSIST + SAVE_UPDATE + orphanRemoval=true) and the
				// existing setFeatureGenomicMutationDetail() setter calls .clear() +
				// .add(null), which combination produced a double-DELETE at flush
				// (StaleStateException "actual row count: 0; expected: 1"). The fgmd
				// is cleared field-wise from editFeatureDTO when locationDeleted=true.
				VariantSequence vrSeq = featureRepository.getFeatureVariant(feature);
				if (vrSeq != null) {
					HibernateUtil.currentSession().delete(vrSeq);
					return UpsertResult.DELETED;
				}
			}
		}
		return UpsertResult.UNCHANGED;
	}

	/**
	 * The "ref/var" notation stored in vfseq_variation and shown in red on the feature page,
	 * or null when it would carry no sequence at all.
	 *
	 * The structural characters differ by type -- a deletion is "ACGT/-" (no variant sequence
	 * by definition), an insertion "-/ACGT" (no reference) -- so emptiness of one side is
	 * normal and only a notation with no bases in it at all is meaningless. That happens for a
	 * point mutation or MNV whose mutation detail has neither sequence: the result is a bare
	 * "/", which would render as a stray slash between the flanks. Returning null leaves
	 * whatever is stored alone, the same way this code has always handled an INDEL with no
	 * reference sequence. Features in that state are a separate data problem, already reported
	 * by Check-Feature-Mutation-Detail-Missing-Sequences_w.
	 */
	public static String variationNotation(FeatureTypeEnum type, String seqRef, String seqVar) {
		String ref = seqRef == null ? "" : seqRef;
		String var = seqVar == null ? "" : seqVar;
		String notation = switch (type) {
			case DELETION -> ref + "/-";
			case INSERTION -> "-/" + var;
			case INDEL, POINT_MUTATION, MNV -> ref + "/" + var;
			default -> null;
		};
		if (notation == null || notation.replace("/", "").replace("-", "").isEmpty()) {
			return null;
		}
		return notation;
	}

	/** What upsertFlankingSequence() did, so batch callers can report it without re-querying. */
	public enum UpsertResult {
		CREATED, UPDATED, UNCHANGED, DELETED;

		public boolean wroteAnything() {
			return this != UNCHANGED;
		}
	}

	private UpsertResult insertOrUpdateFlankSeq(Feature ftr, String seq1, String seq2, int offset) {
		VariantSequence vrSeq = featureRepository.getFeatureVariant(ftr);
		boolean newSequence = vrSeq == null;
		if (newSequence) {
			vrSeq = new VariantSequence();
		}

		// Null when the mutation detail carries no sequence at all; hasFlankSeqChanged() then
		// leaves both fields alone rather than storing a meaningless "[/]"/"/" (the INDEL
		// branch has always worked this way -- variationNotation() applies it to every type).
		FeatureGenomicMutationDetail detail = ftr.getFeatureGenomicMutationDetail();
		String vfsVariation = variationNotation(ftr.getType(), detail.getFgmdSeqRef(), detail.getFgmdSeqVar());
		String vfsTargetSequence = vfsVariation == null ? null : seq1 + "[" + vfsVariation + "]" + seq2;
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
			pa.setPublication(pubRepo.getPublication(COMPUTED_FLANK_SEQ_PUB));
			pa.setDataZdbID(vrSeq.getZdbID());
			pa.setSourceType(RecordAttribution.SourceType.STANDARD);
			currentSession().save(pa);
		}
		if (!changed) {
			return UpsertResult.UNCHANGED;
		}
		return newSequence ? UpsertResult.CREATED : UpsertResult.UPDATED;
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

	private boolean locationWithinChromosome(AssemblyEnum assembly, FeatureLocation ftrLoc) {
		Long chromosomeLength = getChromosomeLength(assembly, ftrLoc.getChromosome());
		return chromosomeLength != null
			&& ftrLoc.getStartLocation() != null && ftrLoc.getStartLocation() >= 1
			&& ftrLoc.getEndLocation() != null && ftrLoc.getEndLocation() <= chromosomeLength;
	}
}



