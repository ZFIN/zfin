package org.zfin.zirc.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.GenotypingAssayFile;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;
import org.zfin.zirc.entity.LinkedFeature;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.entity.Phenotype;
import org.zfin.zirc.presentation.GeneDTO;
import org.zfin.zirc.presentation.GenotypingAssayDTO;
import org.zfin.zirc.presentation.LesionDTO;
import org.zfin.zirc.presentation.LinkedFeatureDTO;
import org.zfin.zirc.presentation.PhenotypeDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Single entry point for mutating a line submission. Centralized so future
 * cross-cutting concerns (versioning via {@code Updates}, validation, audit
 * logging) have one place to hook in. Callers manage the transaction.
 */
@Service
@Log4j2
public class LineSubmissionService {

    /** Caps from the form spec. Surfaced to clients via /caps (see
     *  ZircDashboardController) so the React form can disable "+ Add"
     *  buttons before the user hits the server. */
    public static final int MAX_MUTATIONS_PER_SUBMISSION = 5;
    public static final int MAX_CHILD_ROWS_PER_MUTATION = 10;

    /**
     * Persist a single field change on a line submission. If {@code zdbID} is
     * null/blank, a new submission is created with {@code is_draft = true} (DB
     * default) and {@code currentUser} auto-linked as a "submitter" — matches
     * the previous {@code /new} behavior, just deferred until the curator
     * actually types something.
     *
     * @return the (possibly newly created) submission, with the field applied.
     */
    public LineSubmission saveField(String zdbID, String fieldName, String rawValue, Person currentUser) {
        String value = (rawValue != null && !rawValue.isBlank()) ? rawValue.trim() : null;
        LineSubmission submission = loadOrCreate(zdbID, currentUser);
        applyField(submission, fieldName, value);
        HibernateUtil.currentSession().merge(submission);
        return submission;
    }

    /**
     * Persist the acceptance-reasons section: an array of canonical snake_case
     * option values plus a single optional free-text "Other" entry. As with
     * {@link #saveField}, a new submission is created on the fly when
     * {@code zdbID} is null/blank.
     */
    public LineSubmission saveReasons(String zdbID, String[] reasons, String reasonsOther, Person currentUser) {
        LineSubmission submission = loadOrCreate(zdbID, currentUser);
        submission.setReasons(reasons != null ? reasons : new String[0]);
        String trimmed = (reasonsOther != null && !reasonsOther.isBlank()) ? reasonsOther.trim() : null;
        submission.setReasonsOther(trimmed);
        HibernateUtil.currentSession().merge(submission);
        return submission;
    }

    /**
     * Replace-all save for the linked-features section. Each row pairs two
     * mutations on the same submission with optional distance metadata.
     * Pairs are normalized so that {@code mutationA.id < mutationB.id}
     * before lookup / insert — the DB CHECK constraint enforces the same
     * ordering, so the on-disk identity of {@code (X, Y)} and {@code (Y, X)}
     * is the same row regardless of the order the user picked them.
     *
     * <p>Rows that omit either mutation, reference a mutation not on this
     * submission, or self-link ({@code A == B}) are silently dropped.
     * Duplicate pairs in the incoming list are deduped with last-write-
     * wins semantics — the later row's distance / additional info
     * overwrites the earlier row's. This matches the curator-intuitive
     * "I edited that row a second time, the second edit should stick"
     * outcome when the UI ends up with two rows sharing a (mutationA,
     * mutationB) pair.
     */
    public LineSubmission saveLinkedFeatures(String zdbID, List<LinkedFeatureDTO> incoming, Person currentUser) {
        LineSubmission submission = loadOrCreate(zdbID, currentUser);

        // Index this submission's mutations by id for the FK lookup.
        Map<Long, Mutation> mutationsById = new HashMap<>();
        for (Mutation m : submission.getMutations()) {
            mutationsById.put(m.getId(), m);
        }

        // Existing rows keyed by their normalized (a, b) tuple. Same map
        // is reused to look up the LinkedFeature created on a duplicate's
        // first occurrence so the second occurrence can overwrite it
        // rather than fight on a freshly-created sibling row (which the
        // DB CHECK would reject anyway).
        Map<List<Long>, LinkedFeature> byKey = new HashMap<>();
        for (LinkedFeature lf : submission.getLinkedFeatures()) {
            byKey.put(List.of(lf.getMutationA().getId(), lf.getMutationB().getId()), lf);
        }
        Set<List<Long>> incomingKeys = new HashSet<>();

        if (incoming != null) {
            for (LinkedFeatureDTO dto : incoming) {
                if (dto.getMutationAId() == null || dto.getMutationBId() == null
                        || dto.getMutationAId().equals(dto.getMutationBId())) {
                    continue;
                }
                Mutation mA = mutationsById.get(dto.getMutationAId());
                Mutation mB = mutationsById.get(dto.getMutationBId());
                if (mA == null || mB == null) {
                    continue; // mutation isn't on this submission — drop
                }
                // Normalize so id(A) < id(B). The DB CHECK enforces the same.
                if (mA.getId() > mB.getId()) {
                    Mutation tmp = mA; mA = mB; mB = tmp;
                }
                List<Long> key = List.of(mA.getId(), mB.getId());
                incomingKeys.add(key);
                LinkedFeature lf = byKey.get(key);
                if (lf == null) {
                    lf = new LinkedFeature();
                    lf.setLineSubmission(submission);
                    lf.setMutationA(mA);
                    lf.setMutationB(mB);
                    submission.getLinkedFeatures().add(lf);
                    byKey.put(key, lf);
                }
                // Last-write-wins on duplicates: each occurrence of the
                // same key re-applies its values to the same lf, so the
                // final iteration's distance / additional info is what
                // sticks.
                lf.setDistanceKnown(dto.getDistanceKnown());
                lf.setDistanceCentimorgans(dto.getDistanceCentimorgans());
                lf.setDistanceMegabases(dto.getDistanceMegabases());
                String info = dto.getAdditionalInfo();
                lf.setAdditionalInfo(info != null && !info.isBlank() ? info.trim() : null);
            }
        }

        submission.getLinkedFeatures().removeIf(lf -> !incomingKeys.contains(
                List.of(lf.getMutationA().getId(), lf.getMutationB().getId())));
        return submission;
    }

    /**
     * Append a new mutation to the given submission. Creates the parent
     * submission too if {@code lsId} is null/blank — same flow as
     * {@link #saveField}, just initiated from the Mutations section's "Add"
     * button before the user has typed any scalar fields.
     *
     * @return the newly persisted mutation (with its IDENTITY-assigned id).
     */
    public Mutation addMutation(String lsId, Person currentUser) {
        LineSubmission submission = loadOrCreate(lsId, currentUser);
        if (submission.getMutations().size() >= MAX_MUTATIONS_PER_SUBMISSION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Submission already has the maximum of "
                            + MAX_MUTATIONS_PER_SUBMISSION + " mutations.");
        }
        Mutation m = new Mutation();
        m.setLineSubmission(submission);
        m.setSortOrder(nextMutationSortOrder(submission));
        HibernateUtil.currentSession().persist(m);
        submission.getMutations().add(m);
        return m;
    }

    private static void requireWithinChildCap(List<?> incoming, String childKind) {
        if (incoming != null && incoming.size() > MAX_CHILD_ROWS_PER_MUTATION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum " + MAX_CHILD_ROWS_PER_MUTATION + " "
                            + childKind + " rows per mutation; got " + incoming.size() + ".");
        }
    }

    private int nextMutationSortOrder(LineSubmission submission) {
        return submission.getMutations().stream()
                .map(Mutation::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    public void removeMutation(Long mutationId) {
        Mutation m = HibernateUtil.currentSession().get(Mutation.class, mutationId);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
        }
        // Detach from parent collection so orphanRemoval fires.
        m.getLineSubmission().getMutations().remove(m);
        HibernateUtil.currentSession().remove(m);
    }

    /**
     * Replace-all save for a mutation's per-row gene list. Diff is keyed on
     * the persistent gene id so existing rows can be updated in place,
     * tolerate row reorder, and we don't churn FKs unnecessarily. Rows
     * without an id are inserted; rows whose id is no longer in the
     * incoming list are removed (orphan removal).
     *
     * <p>{@code mutatedGeneZdbId} is resolved against
     * {@link org.zfin.marker.Marker}. A blank value clears the FK; a
     * non-blank value that doesn't resolve to an existing marker raises
     * a 400 (saving null silently would lose the user's intent).
     */
    public Mutation saveGenes(Long mutationId, List<GeneDTO> incoming, Person currentUser) {
        Mutation mutation = HibernateUtil.currentSession().get(Mutation.class, mutationId);
        if (mutation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
        }
        requireWithinChildCap(incoming, "gene");
        Map<Long, Gene> existing = new HashMap<>();
        for (Gene g : mutation.getGenes()) {
            existing.put(g.getId(), g);
        }
        // Track entity references rather than ids. Auto-flushes triggered
        // inside the loop (e.g. resolveMarker runs a HQL query that
        // forces a flush) populate newly-created rows' IDENTITY ids, so
        // an id-based "keep" check would mis-classify them as "existing
        // but not in incoming" and drop them on the removeIf below.
        Set<Gene> keep = new HashSet<>();
        int order = 0;

        if (incoming != null) {
            for (GeneDTO dto : incoming) {
                order += 1;
                Gene g = (dto.getId() != null) ? existing.get(dto.getId()) : null;
                if (g == null) {
                    g = new Gene();
                    g.setMutation(mutation);
                    mutation.getGenes().add(g);
                }
                keep.add(g);
                g.setSortOrder(order);
                g.setMutatedGene(resolveMarker(dto.getMutatedGeneZdbId()));
                g.setLinkageGroup(blankToNull(dto.getLinkageGroup()));
                g.setGenbankGenomicDna(blankToNull(dto.getGenbankGenomicDna()));
                g.setGenbankCdna(blankToNull(dto.getGenbankCdna()));
                g.setSectionComplete(dto.getSectionComplete());
            }
        }

        // Drop rows that didn't appear in the incoming list.
        mutation.getGenes().removeIf(g -> !keep.contains(g));
        return mutation;
    }

    /**
     * Replace-all save for a mutation's per-row lesion list. Same diff
     * shape as {@link #saveGenes} (insert by null-id, update existing in
     * place, remove rows whose id no longer appears).
     */
    public Mutation saveLesions(Long mutationId, List<LesionDTO> incoming, Person currentUser) {
        Mutation mutation = requireMutation(mutationId);
        requireWithinChildCap(incoming, "lesion");
        Map<Long, Lesion> existing = new HashMap<>();
        for (Lesion l : mutation.getLesions()) {
            existing.put(l.getId(), l);
        }
        // Reference-keyed so freshly-inserted rows survive auto-flushes
        // that mid-loop give them their IDENTITY id (cf. saveGenes).
        Set<Lesion> keep = new HashSet<>();
        int order = 0;
        if (incoming != null) {
            for (LesionDTO dto : incoming) {
                order += 1;
                Lesion l = (dto.getId() != null) ? existing.get(dto.getId()) : null;
                if (l == null) {
                    l = new Lesion();
                    l.setMutation(mutation);
                    mutation.getLesions().add(l);
                }
                keep.add(l);
                l.setSortOrder(order);
                l.setLesionType(blankToNull(dto.getLesionType()));
                l.setLesionSizeBp(dto.getLesionSizeBp());
                l.setNucleotideChange(blankToNull(dto.getNucleotideChange()));
                l.setDeletedSequence(blankToNull(dto.getDeletedSequence()));
                l.setInsertedSequence(blankToNull(dto.getInsertedSequence()));
                l.setTransgeneSequence(blankToNull(dto.getTransgeneSequence()));
                l.setLocationInline(blankToNull(dto.getLocationInline()));
                l.setFivePrimeFlank(blankToNull(dto.getFivePrimeFlank()));
                l.setThreePrimeFlank(blankToNull(dto.getThreePrimeFlank()));
                l.setHasLargeVariant(dto.getHasLargeVariant());
                l.setMutatedAminoAcids(blankToNull(dto.getMutatedAminoAcids()));
                l.setMutatedAminoAcidsHgvs(blankToNull(dto.getMutatedAminoAcidsHgvs()));
                l.setAdditionalInfo(blankToNull(dto.getAdditionalInfo()));
            }
        }
        mutation.getLesions().removeIf(l -> !keep.contains(l));
        return mutation;
    }

    public Mutation saveGenotypingAssays(Long mutationId, List<GenotypingAssayDTO> incoming, Person currentUser) {
        Mutation mutation = requireMutation(mutationId);
        requireWithinChildCap(incoming, "genotyping assay");
        Map<Long, GenotypingAssay> existing = new HashMap<>();
        for (GenotypingAssay g : mutation.getGenotypingAssays()) {
            existing.put(g.getId(), g);
        }
        // Reference-keyed; see saveGenes for rationale.
        Set<GenotypingAssay> keep = new HashSet<>();
        int order = 0;
        if (incoming != null) {
            for (GenotypingAssayDTO dto : incoming) {
                order += 1;
                GenotypingAssay g = (dto.getId() != null) ? existing.get(dto.getId()) : null;
                if (g == null) {
                    g = new GenotypingAssay();
                    g.setMutation(mutation);
                    mutation.getGenotypingAssays().add(g);
                }
                keep.add(g);
                g.setSortOrder(order);
                g.setAssayType(blankToNull(dto.getAssayType()));
                g.setForwardPrimer(validatedPrimer(dto.getForwardPrimer(), "forwardPrimer"));
                g.setReversePrimer(validatedPrimer(dto.getReversePrimer(), "reversePrimer"));
                g.setExpectedWtPcr(blankToNull(dto.getExpectedWtPcr()));
                g.setExpectedMutPcr(blankToNull(dto.getExpectedMutPcr()));
                g.setRestrictionEnzymeName(blankToNull(dto.getRestrictionEnzymeName()));
                g.setRestrictionEnzymeCatalog(blankToNull(dto.getRestrictionEnzymeCatalog()));
                g.setEnzymeCleaves(dto.getEnzymeCleaves() != null ? dto.getEnzymeCleaves() : new String[0]);
                g.setExpectedWtDigest(blankToNull(dto.getExpectedWtDigest()));
                g.setExpectedMutDigest(blankToNull(dto.getExpectedMutDigest()));
                g.setAdditionalInfo(blankToNull(dto.getAdditionalInfo()));
                g.setSequencingPrimer(validatedPrimer(dto.getSequencingPrimer(), "sequencingPrimer"));
                g.setDcapsMismatchPrimer(validatedPrimer(dto.getDcapsMismatchPrimer(), "dcapsMismatchPrimer"));
                g.setWtSpecificPrimer(validatedPrimer(dto.getWtSpecificPrimer(), "wtSpecificPrimer"));
                g.setMutSpecificPrimer(validatedPrimer(dto.getMutSpecificPrimer(), "mutSpecificPrimer"));
                g.setCommonPrimer(validatedPrimer(dto.getCommonPrimer(), "commonPrimer"));
                g.setKaspGenomicSequence(blankToNull(dto.getKaspGenomicSequence()));
                g.setSslpMarkerName(blankToNull(dto.getSslpMarkerName()));
                g.setSslpDistance(blankToNull(dto.getSslpDistance()));
                g.setSslpGenomicLocation(blankToNull(dto.getSslpGenomicLocation()));
                g.setSslpInducedBackground(blankToNull(dto.getSslpInducedBackground()));
                g.setSslpOutcrossedBackground(blankToNull(dto.getSslpOutcrossedBackground()));
                g.setSslpInducedPcr(blankToNull(dto.getSslpInducedPcr()));
                g.setSslpOutcrossedPcr(blankToNull(dto.getSslpOutcrossedPcr()));
                // Files aren't roundtripped here; uploads/deletes go
                // through addAssayFile / removeAssayFile via dedicated
                // multipart endpoints.
            }
        }
        mutation.getGenotypingAssays().removeIf(g -> !keep.contains(g));
        return mutation;
    }

    public Mutation savePhenotypes(Long mutationId, List<PhenotypeDTO> incoming, Person currentUser) {
        Mutation mutation = requireMutation(mutationId);
        requireWithinChildCap(incoming, "phenotype");
        Map<Long, Phenotype> existing = new HashMap<>();
        for (Phenotype p : mutation.getPhenotypes()) {
            existing.put(p.getId(), p);
        }
        // Reference-keyed; see saveGenes for rationale.
        Set<Phenotype> keep = new HashSet<>();
        int order = 0;
        if (incoming != null) {
            for (PhenotypeDTO dto : incoming) {
                order += 1;
                Phenotype p = (dto.getId() != null) ? existing.get(dto.getId()) : null;
                if (p == null) {
                    p = new Phenotype();
                    p.setMutation(mutation);
                    mutation.getPhenotypes().add(p);
                }
                keep.add(p);
                p.setSortOrder(order);
                p.setDescription(blankToNull(dto.getDescription()));
                p.setHpfStart(dto.getHpfStart());
                p.setHpfEnd(dto.getHpfEnd());
                // Stage is a server-managed cache derived from hpfStart;
                // dto.getStage() is ignored on the inbound side.
                p.setStage(lookupStageName(dto.getHpfStart()));
                p.setZfinImagePermission(dto.getZfinImagePermission());
                p.setZircImagePermission(dto.getZircImagePermission());
                p.setNonMendelianPercentage(dto.getNonMendelianPercentage());
                p.setNonMendelianComment(blankToNull(dto.getNonMendelianComment()));
                p.setSegregation(dto.getSegregation() != null ? dto.getSegregation() : new String[0]);
                p.setType(dto.getType() != null ? dto.getType() : new String[0]);
            }
        }
        mutation.getPhenotypes().removeIf(p -> !keep.contains(p));
        return mutation;
    }

    /**
     * Resolve an hpf value to a stage name via the {@code STAGE} table.
     * Picks the row whose {@code hoursStart <= h < hoursEnd}, excluding
     * the {@code Unknown} catch-all. Returns null when the input is null
     * or no row matches (e.g. a value past the table's coverage).
     */
    private static String lookupStageName(Integer hpf) {
        if (hpf == null) {
            return null;
        }
        DevelopmentStage stage = (DevelopmentStage) HibernateUtil.currentSession()
                .createQuery("FROM DevelopmentStage "
                        + "WHERE hoursStart <= :h AND hoursEnd > :h AND name <> :unknown "
                        + "ORDER BY hoursStart DESC")
                .setParameter("h", hpf.floatValue())
                .setParameter("unknown", DevelopmentStage.UNKNOWN)
                .setMaxResults(1)
                .uniqueResult();
        return stage != null ? stage.getName() : null;
    }

    /**
     * Replace-all save for a mutation's publication references. Each row
     * is just a free-text citation (PMID / DOI / "Smith et al. 2024").
     * Empty / blank entries are dropped; duplicates are deduped (first
     * occurrence wins). Backed by the {@code mutation_publication}
     * @ElementCollection on {@link Mutation}.
     */
    public Mutation savePublications(Long mutationId, List<String> incoming, Person currentUser) {
        Mutation mutation = requireMutation(mutationId);
        requireWithinChildCap(incoming, "publication");
        List<String> normalized = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        if (incoming != null) {
            for (String pub : incoming) {
                if (pub == null || pub.isBlank()) {
                    continue;
                }
                String trimmed = pub.trim();
                if (seen.add(trimmed)) {
                    normalized.add(trimmed);
                }
            }
        }
        // Replace contents of the @ElementCollection. clear() + addAll()
        // is the canonical way; Hibernate handles the diff.
        mutation.getPublications().clear();
        mutation.getPublications().addAll(normalized);
        return mutation;
    }

    // ─── Genotyping-assay file uploads ────────────────────────────────────

    private static final Set<String> VALID_ASSAY_FILE_KINDS =
            Set.of("chromatogram", "gel_image", "result_image", "melt_curve");

    /**
     * Persist an uploaded file alongside its {@link GenotypingAssay}. The
     * file lands on disk at
     * {@code $TARGETROOT/server_apps/data_transfer/ZIRC/<submission>/
     * assay-<assayId>-<random suffix>-<sanitized original name>}; the
     * DB row carries the relative path so the bytes can be served
     * back later.
     *
     * <p>Order is: write the file first, then create the DB row.
     * Filename uniqueness comes from a random suffix rather than
     * af_id, so storedPath can be set before persist (af_stored_path
     * is NOT NULL). If the DB insert fails we leak a file on disk,
     * which is preferable to a half-saved row with a null path.
     */
    public GenotypingAssayFile addAssayFile(Long assayId, String kind, MultipartFile file, Person currentUser) throws IOException {
        if (kind == null || !VALID_ASSAY_FILE_KINDS.contains(kind)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unknown assay file kind: " + kind);
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Uploaded file is empty.");
        }
        GenotypingAssay assay = HibernateUtil.currentSession().get(GenotypingAssay.class, assayId);
        if (assay == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Genotyping assay " + assayId + " not found");
        }

        String submissionId = assay.getMutation().getLineSubmission().getZdbID();
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        String sanitized = original.replaceAll("[^A-Za-z0-9._-]", "_");

        // Write the bytes first, then create the DB row. Filename uniqueness
        // comes from a short random suffix (not the af_id, which we don't
        // have yet — and would force a 2-phase insert / update fighting
        // the NOT NULL on af_stored_path). If the DB insert fails, we leak
        // a file on disk; preferable to a half-saved DB row.
        String randomSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        String storedFilename = "assay-" + assayId + "-" + randomSuffix + "-" + sanitized;
        Path dir = zircFileDirFor(submissionId);
        Files.createDirectories(dir);
        Path destination = dir.resolve(storedFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        GenotypingAssayFile entity = new GenotypingAssayFile();
        entity.setAssay(assay);
        entity.setKind(kind);
        entity.setOriginalFilename(original);
        entity.setContentType(file.getContentType());
        entity.setFileSize(file.getSize());
        // Store path relative to TARGETROOT so the row stays valid if
        // TARGETROOT is reconfigured later.
        entity.setStoredPath(zircFileRelativePath(submissionId, storedFilename));
        if (currentUser != null && currentUser.getZdbID() != null) {
            entity.setUploadedBy(HibernateUtil.currentSession()
                    .getReference(Person.class, currentUser.getZdbID()));
        }
        HibernateUtil.currentSession().persist(entity);
        return entity;
    }

    /** Resolve a file row to its on-disk location, or 404 if missing. */
    public GenotypingAssayFile requireAssayFile(Long fileId) {
        GenotypingAssayFile f = HibernateUtil.currentSession().get(GenotypingAssayFile.class, fileId);
        if (f == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Assay file " + fileId + " not found");
        }
        return f;
    }

    public void removeAssayFile(Long fileId) {
        GenotypingAssayFile f = requireAssayFile(fileId);
        Path onDisk = Path.of(ZfinPropertiesEnum.TARGETROOT.value(), f.getStoredPath());
        try {
            Files.deleteIfExists(onDisk);
        } catch (IOException e) {
            // Disk delete failed but we still want to remove the DB row.
            // The orphan file would be cosmetic; manual cleanup is fine.
            log.warn("Failed to delete assay file on disk: {}", onDisk, e);
        }
        f.getAssay().getFiles().remove(f);
        HibernateUtil.currentSession().remove(f);
    }

    private static Path zircFileDirFor(String submissionZdbID) {
        return Path.of(ZfinPropertiesEnum.TARGETROOT.value(),
                "server_apps", "data_transfer", "ZIRC", submissionZdbID);
    }

    private static String zircFileRelativePath(String submissionZdbID, String storedFilename) {
        return "server_apps/data_transfer/ZIRC/" + submissionZdbID + "/" + storedFilename;
    }

    /** Absolute on-disk path for serving the file back. */
    public File absoluteFilePath(GenotypingAssayFile f) {
        return new File(ZfinPropertiesEnum.TARGETROOT.value(), f.getStoredPath());
    }

    private Mutation requireMutation(Long mutationId) {
        Mutation m = HibernateUtil.currentSession().get(Mutation.class, mutationId);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
        }
        return m;
    }

    /**
     * Resolve a marker reference from a curator-supplied string. Accepts
     * either a ZDB ID (e.g. {@code ZDB-GENE-980526-388}) or a gene
     * abbreviation (e.g. {@code fgf8a}) — the marker autocomplete sends
     * the abbreviation back, so the server can't insist on the formal
     * ID. Abbreviation lookups are case-insensitive; the first match
     * wins. Returns null for blank inputs; 400 if neither lookup
     * resolves.
     */
    private Marker resolveMarker(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("ZDB-")) {
            Marker byPk = HibernateUtil.currentSession().get(Marker.class, trimmed);
            if (byPk != null) {
                return byPk;
            }
        } else {
            // ORDER BY zdbID makes the result deterministic when two
            // markers share an abbreviation (rare but possible — the
            // column has no UNIQUE constraint). Without it, setMaxResults
            // can return either match arbitrarily and the persisted FK
            // becomes inconsistent across saves.
            Marker byAbbrev = HibernateUtil.currentSession()
                    .createQuery("from Marker where lower(abbreviation) = :a order by zdbID",
                            Marker.class)
                    .setParameter("a", trimmed.toLowerCase())
                    .setMaxResults(1)
                    .uniqueResult();
            if (byAbbrev != null) {
                return byAbbrev;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Marker not found: " + trimmed);
    }

    private static String blankToNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }

    private static final java.util.regex.Pattern PRIMER_PATTERN =
            java.util.regex.Pattern.compile("^[ACTGNactgn]+$");

    /**
     * Trim + null-blank a primer field, additionally rejecting any
     * sequence that isn't pure ACTGN (case-insensitive). The form has a
     * matching client-side pattern, but the server is authoritative —
     * curators can still hit this path via raw HTTP, paste a string with
     * a stray space, etc. Returns a 400 with a curator-friendly message
     * naming the offending field.
     */
    private static String validatedPrimer(String raw, String fieldName) {
        String trimmed = blankToNull(raw);
        if (trimmed != null && !PRIMER_PATTERN.matcher(trimmed).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Primer field '" + fieldName + "' must contain only ACTGN nucleotides.");
        }
        return trimmed;
    }

    /**
     * Per-field save for a mutation. Mirrors {@link #saveField} but on a
     * mutation by primary-key id (no create-on-first-save here — mutations
     * are created via {@link #addMutation}).
     */
    public Mutation saveMutationField(Long mutationId, String fieldName, String rawValue, Person currentUser) {
        Mutation m = HibernateUtil.currentSession().get(Mutation.class, mutationId);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
        }
        String value = (rawValue != null && !rawValue.isBlank()) ? rawValue.trim() : null;
        applyMutationField(m, fieldName, value);
        HibernateUtil.currentSession().merge(m);
        return m;
    }

    /** Package-private for unit testing. */
    static void applyMutationField(Mutation m, String fieldName, String value) {
        switch (fieldName) {
            case "alleleDesignation":          m.setAlleleDesignation(value); break;
            case "alleleInZfin":               m.setAlleleInZfin(Boolean.TRUE.equals(parseTriBool(value))); break;
            case "mutagenesisStage":           m.setMutagenesisStage(value); break;
            case "mutagenesisProtocol":        m.setMutagenesisProtocol(value); break;
            case "mutagenesisProtocolOther":   m.setMutagenesisProtocolOther(value); break;
            case "molecularlyCharacterized":   m.setMolecularlyCharacterized(parseTriBool(value)); break;
            case "mutationType":               m.setMutationType(value); break;
            case "homozygousLethal":           m.setHomozygousLethal(parseTriBool(value)); break;
            case "lethalityStageTypical":      m.setLethalityStageTypical(value); break;
            case "lethalitySpecificTimepoint": m.setLethalitySpecificTimepoint(value); break;
            case "lethalityWindowStart":       m.setLethalityWindowStart(value); break;
            case "lethalityWindowEnd":         m.setLethalityWindowEnd(value); break;
            case "lethalityAdditionalInfo":    m.setLethalityAdditionalInfo(value); break;
            case "zfinRecordEstablished":      m.setZfinRecordEstablished(parseTriBool(value)); break;
            case "cellGenomicFeature":         m.setCellGenomicFeature(value); break;
            case "mutationDiscoverer":         m.setMutationDiscoverer(value); break;
            case "mutationInstitution":        m.setMutationInstitution(value); break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown mutation field: " + fieldName);
        }
    }

    private LineSubmission loadOrCreate(String zdbID, Person currentUser) {
        if (zdbID == null || zdbID.isBlank()) {
            LineSubmission submission = new LineSubmission();
            HibernateUtil.currentSession().persist(submission);
            linkSubmitter(submission, currentUser);
            return submission;
        }
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        return submission;
    }

    private void linkSubmitter(LineSubmission submission, Person currentUser) {
        if (currentUser == null || currentUser.getZdbID() == null) {
            return;
        }
        // Re-attach: getCurrentSecurityUser() returns a Person from the security context
        // that's detached from this session, which would trip the @Id @ManyToOne cascade.
        Person attached = HibernateUtil.currentSession().getReference(Person.class, currentUser.getZdbID());
        LineSubmissionPerson lsp = new LineSubmissionPerson();
        lsp.setLineSubmission(submission);
        lsp.setPerson(attached);
        lsp.setRole("submitter");
        lsp.setSortOrder(1);
        HibernateUtil.currentSession().persist(lsp);
    }

    /** Package-private for unit testing. */
    static void applyField(LineSubmission s, String fieldName, String value) {
        switch (fieldName) {
            case "name":                       s.setName(value); break;
            case "abbreviation":               s.setAbbreviation(value); break;
            case "previousNames":              s.setPreviousNames(value); break;
            case "featuresLinked":             s.setFeaturesLinked(parseTriBool(value)); break;
            case "maternalBackground":         s.setMaternalBackground(value); break;
            case "paternalBackground":         s.setPaternalBackground(value); break;
            case "backgroundChangeable":       s.setBackgroundChangeable(parseTriBool(value)); break;
            case "backgroundChangeConcerns":   s.setBackgroundChangeConcerns(value); break;
            case "unreportedFeaturesDetails":  s.setUnreportedFeaturesDetails(value); break;
            case "additionalInfo":             s.setAdditionalInfo(value); break;
            case "singleAllelic":              s.setSingleAllelic(parseTriBool(value)); break;
            case "husbandryInfo":              s.setHusbandryInfo(value); break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown field: " + fieldName);
        }
    }

    /** Package-private for unit testing. */
    static Boolean parseTriBool(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
