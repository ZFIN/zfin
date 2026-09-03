package org.zfin.zirc.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The four per-workflow attachment buckets on the assay form, and the file
 * extensions each one accepts (ZFIN-10413, ZFIN-10417).
 *
 * <p>All four buckets write to the same {@code attachments} array on
 * {@link org.zfin.zirc.entity.GenotypingAssay}; what distinguishes them is
 * the {@code assayType} that reveals them, the heading they carry, and the
 * file types they take. Keeping those three facts together here means the
 * uiSchema ({@link ZircAssayFormSchema#uiSchema()}, which publishes the
 * {@code accept} attribute and the helper text) and the upload endpoint
 * ({@link org.zfin.zirc.service.ZircSubmissionService#storeAttachment}, which
 * enforces them) cannot drift apart.
 *
 * <p>Validation is by <b>extension</b>, not Content-Type. Browsers report
 * {@code application/octet-stream} for the instrument formats
 * ({@code .abi}/{@code .ab1}, {@code .scf}), so a MIME allow-list would
 * reject exactly the chromatograms ZFIN-10417 asks us to accept.
 */
public enum ZircAttachmentKind {

    /**
     * Gel images for the four gel-scored workflows. PDF rides along because
     * the previous global Content-Type allow-list accepted it — dropping it
     * here would newly reject gel scans curators can upload today.
     */
    GEL_IMAGE("Annotated gel images",
            List.of("pcr_gel", "rflp", "dcaps", "sslp"),
            Ext.IMAGE),

    /**
     * Sanger trace files. {@code .ab1} is the extension Applied Biosystems
     * sequencers actually emit; ZFIN-10417 names {@code .abi}, so both are
     * accepted rather than rejecting the common real-world spelling.
     */
    CHROMATOGRAM("Chromatograms",
            List.of("pcr_sequencing"),
            List.of("abi", "ab1", "scf", "fa", "txt", "docx")),

    /** ASA / KASP result images — images by definition, so same set as gels. */
    RESULT_IMAGE("Annotated result images",
            List.of("asa", "kasp"),
            Ext.IMAGE),

    /**
     * HRMA melt curves. Deliberately unrestricted: melt-curve exports are
     * proprietary per instrument and nobody has enumerated the set, so an
     * allow-list here would block legitimate files. The size cap still
     * applies. Give this bucket an extension list once curators specify one.
     */
    MELT_CURVE("Annotated melt curve files",
            List.of("hrma"),
            List.of());

    /**
     * Extension sets shared by more than one bucket. These live in a nested
     * holder because an enum constructor cannot read a static field of its
     * own enum — the constants are initialized first, so a plain
     * {@code private static final} field here would not yet be assigned.
     */
    private static final class Ext {
        /** Both image buckets (gels and ASA/KASP results) take this set. */
        private static final List<String> IMAGE =
                List.of("tif", "tiff", "jpg", "jpeg", "png", "gif", "pdf");

        /**
         * Extension to the Content-Type we serve the file back as. Anything
         * absent here is served as {@code application/octet-stream} — see
         * {@link ZircAttachmentKind#contentTypeFor}.
         */
        private static final Map<String, String> CONTENT_TYPES = Map.ofEntries(
                Map.entry("tif",  "image/tiff"),
                Map.entry("tiff", "image/tiff"),
                Map.entry("jpg",  "image/jpeg"),
                Map.entry("jpeg", "image/jpeg"),
                Map.entry("png",  "image/png"),
                Map.entry("gif",  "image/gif"),
                Map.entry("pdf",  "application/pdf"),
                Map.entry("txt",  "text/plain"),
                // FASTA is plain text; no registered type worth preferring.
                Map.entry("fa",   "text/plain"),
                Map.entry("docx",
                        "application/vnd.openxmlformats-officedocument"
                                + ".wordprocessingml.document"));

        private Ext() {}
    }

    /** Fallback for extensions with no entry in the map above. */
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * The Content-Type to store and serve a file as, derived from its
     * extension rather than taken from the upload.
     *
     * <p>The client's declared Content-Type is not trusted: the attachment
     * download endpoint serves files back with an {@code inline}
     * Content-Disposition, so honouring an uploader's claim of
     * {@code text/html} on a file named {@code gel.png} would turn an
     * attachment into stored XSS. Deriving it from the extension we already
     * validated removes the uploader's say in it entirely.
     *
     * <p>Unknown extensions — including everything in the unrestricted
     * melt-curve bucket — get {@code application/octet-stream}, which browsers
     * download rather than render. The trace formats ({@code .abi},
     * {@code .ab1}, {@code .scf}) land here too, which is correct: they have
     * no renderable type and nothing should try to display them.
     */
    public static String contentTypeFor(String filename) {
        String ext = extensionOf(filename);
        if (ext == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        return Ext.CONTENT_TYPES.getOrDefault(ext, DEFAULT_CONTENT_TYPE);
    }

    private final String label;
    private final List<String> assayTypes;
    private final List<String> acceptedExtensions;

    ZircAttachmentKind(String label, List<String> assayTypes,
                       List<String> acceptedExtensions) {
        this.label = label;
        this.assayTypes = assayTypes;
        this.acceptedExtensions = acceptedExtensions;
    }

    /** Bucket heading, e.g. "Chromatograms". */
    public String getLabel() {
        return label;
    }

    /** The {@code assayType} tokens whose form reveals this bucket. */
    public List<String> getAssayTypes() {
        return assayTypes;
    }

    /**
     * Accepted extensions, lowercase and without the leading dot. Empty
     * means this bucket accepts any extension.
     */
    public List<String> getAcceptedExtensions() {
        return acceptedExtensions;
    }

    /** True when this bucket enforces no extension allow-list. */
    public boolean acceptsAnyExtension() {
        return acceptedExtensions.isEmpty();
    }

    /**
     * The bucket revealed for an {@code assayType}, or null when the type is
     * unrecognized or has no attachment bucket. A null means "no rule to
     * apply" — callers must not read it as "reject".
     */
    public static ZircAttachmentKind forAssayType(String assayType) {
        if (assayType == null) {
            return null;
        }
        String token = assayType.trim().toLowerCase(Locale.ROOT);
        for (ZircAttachmentKind kind : values()) {
            if (kind.assayTypes.contains(token)) {
                return kind;
            }
        }
        return null;
    }

    /**
     * Whether this bucket accepts the given filename. Unrestricted buckets
     * accept everything; a name with no extension is rejected by a
     * restricted bucket, since we cannot show it matches.
     */
    public boolean accepts(String filename) {
        if (acceptsAnyExtension()) {
            return true;
        }
        String ext = extensionOf(filename);
        return ext != null && acceptedExtensions.contains(ext);
    }

    /**
     * Lowercase extension without the dot, or null when the name has none.
     * Reads the last dot so {@code trace.raw.ab1} resolves to {@code ab1}.
     */
    public static String extensionOf(String filename) {
        if (filename == null) {
            return null;
        }
        String name = filename.trim();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return null;
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** Curator-facing list, e.g. ".abi, .ab1, .scf, .fa, .txt, .docx". */
    public String getAcceptedExtensionsDisplay() {
        return acceptedExtensions.stream()
                .map(e -> "." + e)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
