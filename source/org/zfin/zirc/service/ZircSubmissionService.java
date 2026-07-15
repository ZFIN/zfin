package org.zfin.zirc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.zirc.api.ZircAssayFormSchema;
import org.zfin.zirc.api.ZircFormSchema;
import org.zfin.zirc.api.ZircGeneFormSchema;
import org.zfin.zirc.api.ZircLesionFormSchema;
import org.zfin.zirc.api.ZircLinkedFeatureFormSchema;
import org.zfin.zirc.api.ZircMutationFormSchema;
import org.zfin.zirc.api.ZircPhenotypeFormSchema;
import org.zfin.zirc.dto.FieldUpdate;
import org.zfin.zirc.entity.AuditEntry;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.GenotypingAssayFile;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;
import org.zfin.zirc.entity.LinkedFeature;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.entity.Phenotype;
import org.zfin.zirc.repository.ZircSubmissionRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@Log4j2
public class ZircSubmissionService {

    private static final ObjectMapper AUDIT_MAPPER = new ObjectMapper();

    @Autowired
    private ZircSubmissionRepository repository;

    public List<LineSubmission> getActiveLineSubmissions() {
        return repository.getLineSubmissions().stream()
                .filter(submission -> submission.getDeletedAt() == null)
                .filter(submission -> !Boolean.FALSE.equals(submission.getIsDraft()) || submission.getSubmittedAt() == null)
                .toList();
    }

    public List<LineSubmission> getClosedLineSubmissions() {
        return Collections.emptyList();
    }

    public LineSubmission getRequiredLineSubmission(String zdbID) {
        LineSubmission submission = repository.getLineSubmission(zdbID);
        if (submission == null || submission.getDeletedAt() != null) {
            throw new ZircEntityNotFoundException("Line submission " + zdbID + " not found");
        }
        return submission;
    }

    public Mutation getRequiredMutation(String submissionId, Long mutationId) {
        Mutation mutation = repository.getMutation(mutationId);
        if (mutation == null || mutation.getLineSubmission() == null ||
                !submissionId.equals(mutation.getLineSubmission().getZdbID())) {
            throw new ZircEntityNotFoundException("Mutation " + mutationId + " not found on line submission " + submissionId);
        }
        return mutation;
    }

    public LineSubmission createDraftForCurrentUser() {
        HibernateUtil.createTransaction();
        LineSubmission submission = new LineSubmission();
        repository.save(submission);
        addCurrentUserAsSubmitter(submission);
        HibernateUtil.flushAndCommitCurrentSession();
        return submission;
    }

    /**
     * Apply a single field change against the form schema. The path is checked
     * against {@link ZircFormSchema#FIELDS}; unknown paths raise
     * {@link IllegalArgumentException} (mapped to 400 by the advice). The same
     * descriptor's read is used to capture the pre-update value for the audit
     * log so old/new round-trip through Jackson consistently.
     */
    public LineSubmission updateField(String zdbID, FieldUpdate update) {
        LineSubmission submission = getRequiredLineSubmission(zdbID);

        ZircFormSchema.FieldDescriptor descriptor = ZircFormSchema.FIELDS.get(update.path());
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown form field path: " + update.path());
        }

        JsonNode oldValue = descriptor.read().apply(submission);
        HibernateUtil.createTransaction();
        descriptor.write().accept(submission, update.value());
        writeAudit("submission", zdbID, "update", update.path(), oldValue, update.value());
        HibernateUtil.flushAndCommitCurrentSession();

        return submission;
    }

    /**
     * Apply a single field change to a Mutation against
     * {@link ZircMutationFormSchema#FIELDS}. Mirrors {@link #updateField} but
     * for the per-mutation aggregate; audit log keys by mutation id.
     */
    public Mutation updateMutationField(Long mutationId, FieldUpdate update) {
        Mutation mutation = repository.getMutation(mutationId);
        if (mutation == null) {
            throw new ZircEntityNotFoundException("Mutation " + mutationId + " not found");
        }

        ZircMutationFormSchema.FieldDescriptor descriptor =
                ZircMutationFormSchema.FIELDS.get(update.path());
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown mutation field path: " + update.path());
        }

        JsonNode oldValue = descriptor.read().apply(mutation);
        HibernateUtil.createTransaction();
        descriptor.write().accept(mutation, update.value());
        writeAudit("mutation", String.valueOf(mutationId), "update",
                update.path(), oldValue, update.value());
        HibernateUtil.flushAndCommitCurrentSession();

        return mutation;
    }

    /**
     * Look up a mutation by database id alone (no parent-submission check).
     * Used by the mutation edit page where the URL only carries the mutation
     * id; ownership/visibility checks come from the parent submission via
     * {@link Mutation#getLineSubmission()} when needed.
     */
    public Mutation getRequiredMutationById(Long mutationId) {
        Mutation mutation = repository.getMutation(mutationId);
        if (mutation == null) {
            throw new ZircEntityNotFoundException("Mutation " + mutationId + " not found");
        }
        return mutation;
    }

    /**
     * Look up an assay by database id alone. Parent-mutation ownership
     * checks are deferred to the controller when needed.
     */
    public GenotypingAssay getRequiredAssayById(Long assayId) {
        GenotypingAssay assay = repository.getAssay(assayId);
        if (assay == null) {
            throw new ZircEntityNotFoundException("Assay " + assayId + " not found");
        }
        return assay;
    }

    /**
     * Apply a single field change to a GenotypingAssay against
     * {@link ZircAssayFormSchema#FIELDS}. Mirrors {@link #updateMutationField}
     * but for the per-assay aggregate; audit log keys by assay id.
     */
    public GenotypingAssay updateAssayField(Long assayId, FieldUpdate update) {
        GenotypingAssay assay = getRequiredAssayById(assayId);

        ZircAssayFormSchema.FieldDescriptor descriptor =
                ZircAssayFormSchema.FIELDS.get(update.path());
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown assay field path: " + update.path());
        }

        JsonNode oldValue = descriptor.read().apply(assay);
        HibernateUtil.createTransaction();
        descriptor.write().accept(assay, update.value());
        writeAudit("assay", String.valueOf(assayId), "update",
                update.path(), oldValue, update.value());
        HibernateUtil.flushAndCommitCurrentSession();

        return assay;
    }

    private static String safeJson(JsonNode node) {
        try {
            return node == null ? "null" : AUDIT_MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            return "?";
        }
    }

    /**
     * Persist a row in {@code zirc.audit} and emit the legacy {@code ZIRC_AUDIT}
     * log line so existing log-based pipelines keep working. The insert is
     * invoked inside whatever transaction the caller has already opened so
     * the audit reflects committed state precisely; if the main commit fails
     * the audit row rolls back with it.
     */
    private void writeAudit(
            String entityKind,
            String entityId,
            String action,
            String path,
            JsonNode oldValue,
            JsonNode newValue) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        String actor = currentUser == null ? "anonymous" : currentUser.getZdbID();

        AuditEntry entry = new AuditEntry();
        entry.setActor(actor);
        entry.setEntityKind(entityKind);
        entry.setEntityId(entityId);
        entry.setAction(action);
        entry.setPath(path);
        entry.setOldValue(safeJson(oldValue));
        entry.setNewValue(safeJson(newValue));
        repository.save(entry);

        log.info("ZIRC_AUDIT user={} {}={} action={} path={} old={} new={}",
                actor, entityKind, entityId, action,
                path == null ? "-" : path,
                safeJson(oldValue),
                safeJson(newValue));
    }

    public Mutation addMutation(String submissionId) {
        LineSubmission submission = getRequiredLineSubmission(submissionId);
        int existing = submission.getMutations() == null ? 0 : submission.getMutations().size();
        if (existing >= ZircFormSchema.MAX_MUTATIONS_PER_SUBMISSION) {
            throw new IllegalArgumentException(
                    "Maximum " + ZircFormSchema.MAX_MUTATIONS_PER_SUBMISSION
                            + " mutations per submission.");
        }
        HibernateUtil.createTransaction();
        Mutation mutation = new Mutation();
        mutation.setLineSubmission(submission);
        mutation.setSortOrder(nextMutationSortOrder(submission));
        repository.save(mutation);
        HibernateUtil.currentSession().flush();
        writeAudit("submission", submissionId, "create-mutation", null,
                null, AUDIT_MAPPER.valueToTree(
                        java.util.Map.of("mutationId", mutation.getId())));
        HibernateUtil.flushAndCommitCurrentSession();
        return mutation;
    }

    public void deleteMutation(String submissionId, Long mutationId) {
        Mutation mutation = getRequiredMutation(submissionId, mutationId);
        HibernateUtil.createTransaction();
        repository.delete(mutation);
        writeAudit("submission", submissionId, "delete-mutation", null,
                AUDIT_MAPPER.valueToTree(java.util.Map.of("mutationId", mutationId)),
                null);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    /**
     * Create a new {@link GenotypingAssay} under the given mutation. Mirrors
     * {@link #addMutation} — assigns the next sort order so cards stay
     * stably ordered. Returns the parent mutation so callers can refresh the
     * MutationDTO in one round trip.
     */
    public Mutation addAssay(Long mutationId) {
        Mutation mutation = getRequiredMutationById(mutationId);
        int existing = mutation.getGenotypingAssays() == null ? 0
                : mutation.getGenotypingAssays().size();
        if (existing >= ZircMutationFormSchema.MAX_ASSAYS_PER_MUTATION) {
            throw new IllegalArgumentException(
                    "Maximum " + ZircMutationFormSchema.MAX_ASSAYS_PER_MUTATION
                            + " genotyping assays per mutation.");
        }
        HibernateUtil.createTransaction();
        GenotypingAssay assay = new GenotypingAssay();
        assay.setMutation(mutation);
        assay.setSortOrder(nextAssaySortOrder(mutation));
        repository.save(assay);
        mutation.getGenotypingAssays().add(assay);
        HibernateUtil.currentSession().flush();
        writeAudit("mutation", String.valueOf(mutationId), "create-assay", null,
                null, AUDIT_MAPPER.valueToTree(
                        java.util.Map.of("assayId", assay.getId())));
        HibernateUtil.flushAndCommitCurrentSession();
        return mutation;
    }

    public void deleteAssay(Long assayId) {
        GenotypingAssay assay = getRequiredAssayById(assayId);
        Long mutationId = assay.getMutation() == null ? null : assay.getMutation().getId();
        HibernateUtil.createTransaction();
        repository.delete(assay);
        writeAudit("mutation", mutationId == null ? "?" : String.valueOf(mutationId),
                "delete-assay", null,
                AUDIT_MAPPER.valueToTree(java.util.Map.of("assayId", assayId)),
                null);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public void addSubmitter(String zdbID, String personZdbID) {
        LineSubmission submission = getRequiredLineSubmission(zdbID);
        Person person = repository.getPerson(personZdbID);
        if (person == null) {
            throw new ZircEntityNotFoundException("Person " + personZdbID + " not found");
        }
        boolean alreadyLinked = submission.getPersons().stream()
                .anyMatch(lsp -> personZdbID.equals(lsp.getPerson().getZdbID()) && "submitter".equals(lsp.getRole()));
        if (alreadyLinked) {
            return;
        }
        HibernateUtil.createTransaction();
        LineSubmissionPerson lsp = new LineSubmissionPerson();
        lsp.setLineSubmission(submission);
        lsp.setPerson(person);
        lsp.setRole("submitter");
        lsp.setSortOrder(submission.getPersons().size() + 1);
        repository.save(lsp);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    private void addCurrentUserAsSubmitter(LineSubmission submission) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        if (currentUser == null || currentUser.getZdbID() == null) {
            return;
        }
        Person attachedUser = repository.getPersonReference(currentUser.getZdbID());
        LineSubmissionPerson lsp = new LineSubmissionPerson();
        lsp.setLineSubmission(submission);
        lsp.setPerson(attachedUser);
        lsp.setRole("submitter");
        lsp.setSortOrder(1);
        repository.save(lsp);
    }

    private static int nextMutationSortOrder(LineSubmission submission) {
        return submission.getMutations().stream()
                .map(Mutation::getSortOrder)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private static int nextAssaySortOrder(Mutation mutation) {
        return mutation.getGenotypingAssays().stream()
                .map(GenotypingAssay::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    // ─── Attachments (M4.3) ─────────────────────────────────────────────────

    /** Hard upper bound on a single uploaded attachment. */
    public static final long MAX_ATTACHMENT_BYTES = 20L * 1024 * 1024;

    /**
     * Content-types we'll accept. Starter list — internal-curator workflow.
     * For richer formats (e.g. CSV chromatograms) extend this list.
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/svg+xml", "image/tiff",
            "application/pdf",
            "text/plain", "text/csv");

    public GenotypingAssayFile getRequiredAssayFile(Long fileId) {
        GenotypingAssayFile file = repository.getAssayFile(fileId);
        if (file == null) {
            throw new ZircEntityNotFoundException("Assay file " + fileId + " not found");
        }
        return file;
    }

    /**
     * Persist an uploaded {@link MultipartFile} as an attachment on the
     * given assay. Layout per the schema comment:
     * {@code $TARGETROOT/server_apps/data_transfer/ZIRC/<submission zdb id>/
     *  assay-<assay id>-<file id>-<sanitized filename>}.
     *
     * <p>Returns the parent assay so callers can return a refreshed
     * AssayDTO in one round trip.
     */
    public GenotypingAssay storeAttachment(Long assayId, MultipartFile upload) throws IOException {
        if (upload == null || upload.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }
        if (upload.getSize() > MAX_ATTACHMENT_BYTES) {
            throw new IllegalArgumentException(
                    "Attachment exceeds size limit (" + MAX_ATTACHMENT_BYTES + " bytes)");
        }
        String contentType = upload.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Content type not allowed: " + contentType);
        }

        GenotypingAssay assay = getRequiredAssayById(assayId);
        int existing = assay.getFiles() == null ? 0 : assay.getFiles().size();
        if (existing >= ZircAssayFormSchema.MAX_ATTACHMENTS_PER_ASSAY) {
            throw new IllegalArgumentException(
                    "Maximum " + ZircAssayFormSchema.MAX_ATTACHMENTS_PER_ASSAY
                            + " attachments per assay.");
        }
        String submissionId = assay.getMutation().getLineSubmission().getZdbID();
        String safeName = sanitizeFilename(upload.getOriginalFilename());

        HibernateUtil.createTransaction();

        // Insert first to obtain the generated file id, then we can build
        // a deterministic on-disk name. storedPath stays placeholder until
        // the file is actually written; we update it below.
        GenotypingAssayFile file = new GenotypingAssayFile();
        file.setAssay(assay);
        file.setOriginalFilename(upload.getOriginalFilename());
        file.setContentType(contentType);
        file.setFileSize(upload.getSize());
        file.setStoredPath("__pending__");
        repository.save(file);
        HibernateUtil.currentSession().flush();

        Path dir = Paths.get(
                ZfinPropertiesEnum.TARGETROOT.value(),
                "server_apps", "data_transfer", "ZIRC", submissionId);
        Files.createDirectories(dir);

        String storedName = "assay-" + assayId + "-" + file.getId() + "-" + safeName;
        Path stored = dir.resolve(storedName);
        upload.transferTo(stored.toFile());
        file.setStoredPath(stored.toString());

        // Audit the upload — old=null, new={file id + original name + size}
        JsonNode meta = AUDIT_MAPPER.valueToTree(java.util.Map.of(
                "fileId", file.getId(),
                "originalFilename", upload.getOriginalFilename(),
                "bytes", upload.getSize()));
        writeAudit("assay", String.valueOf(assayId), "upload", null, null, meta);

        HibernateUtil.flushAndCommitCurrentSession();

        return assay;
    }

    public void deleteAttachment(Long fileId) {
        GenotypingAssayFile file = getRequiredAssayFile(fileId);
        Long assayId = file.getAssay() == null ? null : file.getAssay().getId();
        String storedPath = file.getStoredPath();
        String originalFilename = file.getOriginalFilename();

        HibernateUtil.createTransaction();
        repository.delete(file);
        // Audit captures the about-to-be-removed file's metadata as "old".
        JsonNode meta = AUDIT_MAPPER.valueToTree(java.util.Map.of(
                "fileId", fileId,
                "originalFilename", originalFilename == null ? "" : originalFilename,
                "storedPath", storedPath == null ? "" : storedPath));
        writeAudit("assay", assayId == null ? "?" : String.valueOf(assayId),
                "delete-file", null, meta, null);
        HibernateUtil.flushAndCommitCurrentSession();

        // Best-effort unlink of the on-disk file. We've already committed
        // the DB delete, so a missing file is not an error condition.
        if (storedPath != null) {
            try {
                Files.deleteIfExists(Paths.get(storedPath));
            } catch (IOException e) {
                log.warn("ZIRC attachment delete: failed to remove file on disk: {}", storedPath, e);
            }
        }
    }

    /**
     * Strip path separators, control characters, and leading dots from a
     * caller-supplied filename so it's safe to use as the suffix of a
     * server-constructed path. Returns {@code "file"} as a fallback when
     * the result would otherwise be empty.
     */
    static String sanitizeFilename(String name) {
        if (name == null) {return "file";}
        String trimmed = name.replaceAll("[\\\\/\\u0000-\\u001F]", "_");
        // Collapse path traversal segments to underscores too.
        trimmed = trimmed.replace("..", "_");
        // Strip leading dots so we don't create hidden files.
        while (trimmed.startsWith(".")) {trimmed = trimmed.substring(1);}
        if (trimmed.isBlank()) {return "file";}
        // Cap length so very long names don't blow up file systems.
        if (trimmed.length() > 200) {trimmed = trimmed.substring(0, 200);}
        return trimmed;
    }

    public File resolveAttachmentPath(GenotypingAssayFile file) {
        return new File(file.getStoredPath());
    }

    // ─── Linked Features (M5.3) ─────────────────────────────────────────────

    /**
     * Look up one linkage row by its composite PK. The path-segments
     * (a,b) come from the URL in their as-submitted order; we normalize
     * to (min,max) here to match the storage invariant enforced by the
     * DB CHECK constraint.
     */
    public LinkedFeature getRequiredLinkedFeature(String submissionId, Long aId, Long bId) {
        long lo = Math.min(aId, bId);
        long hi = Math.max(aId, bId);
        LinkedFeature lf = repository.getLinkedFeature(submissionId, lo, hi);
        if (lf == null) {
            throw new ZircEntityNotFoundException(
                    "Linked feature (" + lo + ", " + hi + ") not found on submission " + submissionId);
        }
        return lf;
    }

    /**
     * Create a linkage row between two mutations on the same submission.
     * The DB CHECK constraint enforces {@code mutationA.id < mutationB.id};
     * we normalize the pair here so callers can pass either order.
     * Returns the parent submission so the response carries the refreshed
     * linkedFeatures list.
     */
    public LineSubmission addLinkedFeature(String submissionId, Long aId, Long bId) {
        if (aId == null || bId == null || aId.equals(bId)) {
            throw new IllegalArgumentException(
                    "A linkage requires two distinct mutations on the same submission.");
        }
        long lo = Math.min(aId, bId);
        long hi = Math.max(aId, bId);

        LineSubmission submission = getRequiredLineSubmission(submissionId);
        Mutation mutationA = ensureMutationOnSubmission(submission, lo);
        Mutation mutationB = ensureMutationOnSubmission(submission, hi);

        if (repository.getLinkedFeature(submissionId, lo, hi) != null) {
            throw new IllegalArgumentException(
                    "Mutations " + lo + " and " + hi + " are already linked on this submission.");
        }

        HibernateUtil.createTransaction();
        LinkedFeature lf = new LinkedFeature();
        lf.setLineSubmission(submission);
        lf.setMutationA(mutationA);
        lf.setMutationB(mutationB);
        repository.save(lf);
        submission.getLinkedFeatures().add(lf);
        writeAudit("submission", submissionId, "create-linked-feature", null,
                null, AUDIT_MAPPER.valueToTree(
                        java.util.Map.of("mutationAId", lo, "mutationBId", hi)));
        HibernateUtil.flushAndCommitCurrentSession();
        return submission;
    }

    public void deleteLinkedFeature(String submissionId, Long aId, Long bId) {
        LinkedFeature lf = getRequiredLinkedFeature(submissionId, aId, bId);
        long lo = Math.min(aId, bId);
        long hi = Math.max(aId, bId);
        HibernateUtil.createTransaction();
        repository.delete(lf);
        writeAudit("submission", submissionId, "delete-linked-feature", null,
                AUDIT_MAPPER.valueToTree(
                        java.util.Map.of("mutationAId", lo, "mutationBId", hi)),
                null);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    /**
     * Field-path PATCH on a linkage row. Path/value flow mirrors the
     * other three updateField methods; the composite-PK lookup happens
     * up front.
     */
    public LinkedFeature updateLinkedFeatureField(
            String submissionId, Long aId, Long bId, FieldUpdate update) {
        LinkedFeature lf = getRequiredLinkedFeature(submissionId, aId, bId);

        ZircLinkedFeatureFormSchema.FieldDescriptor descriptor =
                ZircLinkedFeatureFormSchema.FIELDS.get(update.path());
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown linked-feature field path: " + update.path());
        }

        JsonNode oldValue = descriptor.read().apply(lf);
        HibernateUtil.createTransaction();
        descriptor.write().accept(lf, update.value());
        // Audit entity-id is the (submission,a,b) tuple flattened — keeps
        // (entity_kind, entity_id, at) queries simple.
        long lo = Math.min(aId, bId);
        long hi = Math.max(aId, bId);
        writeAudit("linked-feature",
                submissionId + ":" + lo + ":" + hi,
                "update", update.path(), oldValue, update.value());
        HibernateUtil.flushAndCommitCurrentSession();
        return lf;
    }

    // ─── Genes (M6.1) ───────────────────────────────────────────────────────

    /** Hard cap to keep card list manageable; same as MAX_ASSAYS_PER_MUTATION. */
    public static final int MAX_GENES_PER_MUTATION = 10;

    public Gene getRequiredGeneById(Long geneId) {
        Gene gene = repository.getGene(geneId);
        if (gene == null) {
            throw new ZircEntityNotFoundException("Gene " + geneId + " not found");
        }
        return gene;
    }

    public Mutation addGene(Long mutationId) {
        Mutation mutation = getRequiredMutationById(mutationId);
        int existing = mutation.getGenes() == null ? 0 : mutation.getGenes().size();
        if (existing >= MAX_GENES_PER_MUTATION) {
            throw new IllegalArgumentException(
                    "Maximum " + MAX_GENES_PER_MUTATION + " genes per mutation.");
        }
        HibernateUtil.createTransaction();
        Gene gene = new Gene();
        gene.setMutation(mutation);
        gene.setSortOrder(nextGeneSortOrder(mutation));
        repository.save(gene);
        mutation.getGenes().add(gene);
        HibernateUtil.currentSession().flush();
        writeAudit("mutation", String.valueOf(mutationId), "create-gene", null,
                null, AUDIT_MAPPER.valueToTree(
                        java.util.Map.of("geneId", gene.getId())));
        HibernateUtil.flushAndCommitCurrentSession();
        return mutation;
    }

    public void deleteGene(Long geneId) {
        Gene gene = getRequiredGeneById(geneId);
        Long mutationId = gene.getMutation() == null ? null : gene.getMutation().getId();
        HibernateUtil.createTransaction();
        repository.delete(gene);
        writeAudit("mutation", mutationId == null ? "?" : String.valueOf(mutationId),
                "delete-gene", null,
                AUDIT_MAPPER.valueToTree(java.util.Map.of("geneId", geneId)),
                null);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public Gene updateGeneField(Long geneId, FieldUpdate update) {
        Gene gene = getRequiredGeneById(geneId);
        ZircGeneFormSchema.FieldDescriptor descriptor =
                ZircGeneFormSchema.FIELDS.get(update.path());
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown gene field path: " + update.path());
        }
        JsonNode oldValue = descriptor.read().apply(gene);
        HibernateUtil.createTransaction();
        descriptor.write().accept(gene, update.value());
        writeAudit("gene", String.valueOf(geneId), "update",
                update.path(), oldValue, update.value());
        HibernateUtil.flushAndCommitCurrentSession();
        return gene;
    }

    private static int nextGeneSortOrder(Mutation mutation) {
        return mutation.getGenes().stream()
                .map(Gene::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    // ─── Lesions (M7.1) ─────────────────────────────────────────────────────

    /** Hard cap on lesions per mutation; same shape as MAX_GENES_PER_MUTATION. */
    public static final int MAX_LESIONS_PER_MUTATION = 10;

    public Lesion getRequiredLesionById(Long lesionId) {
        Lesion lesion = repository.getLesion(lesionId);
        if (lesion == null) {
            throw new ZircEntityNotFoundException("Lesion " + lesionId + " not found");
        }
        return lesion;
    }

    public Mutation addLesion(Long mutationId) {
        Mutation mutation = getRequiredMutationById(mutationId);
        int existing = mutation.getLesions() == null ? 0 : mutation.getLesions().size();
        if (existing >= MAX_LESIONS_PER_MUTATION) {
            throw new IllegalArgumentException(
                    "Maximum " + MAX_LESIONS_PER_MUTATION + " lesions per mutation.");
        }
        HibernateUtil.createTransaction();
        Lesion lesion = new Lesion();
        lesion.setMutation(mutation);
        lesion.setSortOrder(nextLesionSortOrder(mutation));
        repository.save(lesion);
        mutation.getLesions().add(lesion);
        HibernateUtil.currentSession().flush();
        writeAudit("mutation", String.valueOf(mutationId), "create-lesion", null,
                null, AUDIT_MAPPER.valueToTree(
                        java.util.Map.of("lesionId", lesion.getId())));
        HibernateUtil.flushAndCommitCurrentSession();
        return mutation;
    }

    public void deleteLesion(Long lesionId) {
        Lesion lesion = getRequiredLesionById(lesionId);
        Long mutationId = lesion.getMutation() == null ? null : lesion.getMutation().getId();
        HibernateUtil.createTransaction();
        repository.delete(lesion);
        writeAudit("mutation", mutationId == null ? "?" : String.valueOf(mutationId),
                "delete-lesion", null,
                AUDIT_MAPPER.valueToTree(java.util.Map.of("lesionId", lesionId)),
                null);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public Lesion updateLesionField(Long lesionId, FieldUpdate update) {
        Lesion lesion = getRequiredLesionById(lesionId);
        ZircLesionFormSchema.FieldDescriptor descriptor =
                ZircLesionFormSchema.FIELDS.get(update.path());
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown lesion field path: " + update.path());
        }
        JsonNode oldValue = descriptor.read().apply(lesion);
        HibernateUtil.createTransaction();
        descriptor.write().accept(lesion, update.value());
        recalcLesionSizes(lesion);
        writeAudit("lesion", String.valueOf(lesionId), "update",
                update.path(), oldValue, update.value());
        HibernateUtil.flushAndCommitCurrentSession();
        return lesion;
    }

    /**
     * Lesion sizes are always derived, never curator-entered: a point mutation
     * is 1 bp, a deletion/indel's lesion size is its deleted-sequence length,
     * and an insertion/indel's insertion size is its inserted-sequence length.
     * Recomputed on every save so the stored value can't drift from the
     * (read-only) sequence fields. Types that don't use a given size get null.
     */
    private static void recalcLesionSizes(Lesion lesion) {
        String type = lesion.getLesionType();
        if ("point_mutation".equals(type)) {
            lesion.setLesionSizeBp(1);
        } else if ("deletion".equals(type) || "indel".equals(type)) {
            lesion.setLesionSizeBp(sequenceLength(lesion.getDeletedSequence()));
        } else {
            lesion.setLesionSizeBp(null);
        }
        if ("insertion".equals(type) || "indel".equals(type)) {
            lesion.setInsertionSizeBp(sequenceLength(lesion.getInsertedSequence()));
        } else {
            lesion.setInsertionSizeBp(null);
        }
    }

    /** Count of nucleotide letters in a sequence; null when empty/blank. */
    private static Integer sequenceLength(String sequence) {
        if (sequence == null) {
            return null;
        }
        int count = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (Character.isLetter(sequence.charAt(i))) {
                count++;
            }
        }
        return count == 0 ? null : count;
    }

    private static int nextLesionSortOrder(Mutation mutation) {
        return mutation.getLesions().stream()
                .map(Lesion::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    // ─── Phenotypes (M8.1) ──────────────────────────────────────────────────

    /** Hard cap on phenotypes per mutation; same shape as MAX_LESIONS_PER_MUTATION. */
    public static final int MAX_PHENOTYPES_PER_MUTATION = 10;

    public Phenotype getRequiredPhenotypeById(Long phenotypeId) {
        Phenotype phenotype = repository.getPhenotype(phenotypeId);
        if (phenotype == null) {
            throw new ZircEntityNotFoundException("Phenotype " + phenotypeId + " not found");
        }
        return phenotype;
    }

    public Mutation addPhenotype(Long mutationId) {
        Mutation mutation = getRequiredMutationById(mutationId);
        int existing = mutation.getPhenotypes() == null ? 0 : mutation.getPhenotypes().size();
        if (existing >= MAX_PHENOTYPES_PER_MUTATION) {
            throw new IllegalArgumentException(
                    "Maximum " + MAX_PHENOTYPES_PER_MUTATION + " phenotypes per mutation.");
        }
        HibernateUtil.createTransaction();
        Phenotype phenotype = new Phenotype();
        phenotype.setMutation(mutation);
        phenotype.setSortOrder(nextPhenotypeSortOrder(mutation));
        repository.save(phenotype);
        mutation.getPhenotypes().add(phenotype);
        HibernateUtil.currentSession().flush();
        writeAudit("mutation", String.valueOf(mutationId), "create-phenotype", null,
                null, AUDIT_MAPPER.valueToTree(
                        java.util.Map.of("phenotypeId", phenotype.getId())));
        HibernateUtil.flushAndCommitCurrentSession();
        return mutation;
    }

    public void deletePhenotype(Long phenotypeId) {
        Phenotype phenotype = getRequiredPhenotypeById(phenotypeId);
        Long mutationId = phenotype.getMutation() == null ? null : phenotype.getMutation().getId();
        HibernateUtil.createTransaction();
        repository.delete(phenotype);
        writeAudit("mutation", mutationId == null ? "?" : String.valueOf(mutationId),
                "delete-phenotype", null,
                AUDIT_MAPPER.valueToTree(java.util.Map.of("phenotypeId", phenotypeId)),
                null);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public Phenotype updatePhenotypeField(Long phenotypeId, FieldUpdate update) {
        Phenotype phenotype = getRequiredPhenotypeById(phenotypeId);
        ZircPhenotypeFormSchema.FieldDescriptor descriptor =
                ZircPhenotypeFormSchema.FIELDS.get(update.path());
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown phenotype field path: " + update.path());
        }
        JsonNode oldValue = descriptor.read().apply(phenotype);
        HibernateUtil.createTransaction();
        descriptor.write().accept(phenotype, update.value());
        writeAudit("phenotype", String.valueOf(phenotypeId), "update",
                update.path(), oldValue, update.value());
        HibernateUtil.flushAndCommitCurrentSession();
        return phenotype;
    }

    private static int nextPhenotypeSortOrder(Mutation mutation) {
        return mutation.getPhenotypes().stream()
                .map(Phenotype::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    /** Throws if {@code mutationId} isn't a mutation on this submission. */
    private static Mutation ensureMutationOnSubmission(LineSubmission s, Long mutationId) {
        return s.getMutations().stream()
                .filter(m -> mutationId.equals(m.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mutation " + mutationId + " is not on submission " + s.getZdbID()));
    }

}
