package org.zfin.datatransfer.ensembl;

import jakarta.persistence.Tuple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.Session;
import org.zfin.datatransfer.persistence.Load;
import org.zfin.datatransfer.persistence.LoadFile;
import org.zfin.datatransfer.persistence.LoadFileLog;
import org.zfin.datatransfer.persistence.LoadFileRegistry;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.TranscriptType;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.load.EnsemblTranscriptFastaReadProcess;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads the Ensembl transcripts (ENSDARTs) of the current assemblies as ZDB-TSCRIPT markers, from the cDNA FASTA
 * Ensembl publishes for each assembly (see {@link EnsemblTranscriptFastaFile}).
 *
 * <p>For every transcript it makes sure there is
 * <ul>
 *   <li>a {@code marker} / {@code transcript} named by the lower-cased ENSDART (markers of this type must be lower
 *   case), with the type from the Ensembl biotype, the strain from the assembly (tu: Tuebingen, ab: AB) and the
 *   annotation method Ensembl,</li>
 *   <li>a "gene produces transcript" relationship from the gene that carries the ENSDARG for this assembly
 *   (the {@code Ensembl(<assembly>)} db_links of {@link LoadEnsemblMappingTask}),</li>
 *   <li>an {@code Ensembl_Trans} RNA {@code db_link} with the length, tied to the assembly in
 *   {@code db_link_assembly}, which is what the gene page's Current Ensembl Transcripts table reads, and</li>
 *   <li>a {@code record_attribution} of the transcript and the relationship to the publication given for the load.</li>
 * </ul>
 * Nothing is ever deleted or changed. A rerun adds nothing, and an existing transcript that lacks its assembly or
 * attribution gets just that. Left out, and listed with the reason in the report: an ENSDARG no gene carries (or
 * more than one does), a name already taken by another marker, and any biotype without a ZFIN transcript type.
 *
 * <p>Where the biotype types come from: {@link EnsemblTranscriptFastaReadProcess#BIOTYPE_TO_ZFIN_TYPE}, the map the
 * GRCz11 load uses. Biotypes it does not have (lncRNA, vault_RNA, ...) are skipped, and counted in the summary,
 * unless {@code BIOTYPE_OVERRIDES} supplies a type for them, so a curator's decision can be applied without a code
 * change.
 *
 * <p>Run from the {@code Load-Ensembl-Transcripts-z12ab-z12tu-z13ab} Jenkins job. Inputs, as environment variables:
 * <ul>
 *   <li>{@code TRANSCRIPT_FASTA_UPLOAD}: the Jenkins file parameter; the file is
 *   {@code $WORKSPACE/TRANSCRIPT_FASTA_UPLOAD}. Or {@code TRANSCRIPT_FASTA}: a path, for running by hand. Either one
 *   replaces the download, and the run is logged against the upload placeholder load_file. With neither, every
 *   http(s) load_file of the "Ensembl Transcripts" load is downloaded.</li>
 *   <li>{@code XREF_UPLOAD} / {@code XREF_FILE}: optional, the Ensembl {@code xref.tsv(.gz)} that goes with an uploaded
 *   FASTA. A downloaded FASTA's xref file is downloaded from the same directory. It is only used for the xref
 *   report, never to load anything.</li>
 *   <li>{@code ASSEMBLY}: optional; load only the file(s) for this assembly (the assembly a file is for is read
 *   from its headers).</li>
 *   <li>{@code ATTRIBUTION_PUB_ID}: required, the ZDB-PUB ID the new records are attributed to.</li>
 *   <li>{@code BIOTYPE_OVERRIDES}: optional, {@code biotype=ZFIN type} pairs, comma separated, e.g.
 *   {@code lncRNA=lincRNA,vault_RNA=ncRNA}.</li>
 *   <li>{@code DRY_RUN}: anything but {@code false} only reports what would be done (the default).</li>
 * </ul>
 * Writes {@code ensembl_transcript_load_report.html} (an HTML report, see
 * {@link LoadEnsemblTranscriptsReport}), the full per-transcript {@code ensembl_transcript_load_report.csv},
 * {@code ensembl_transcript_load_summary.txt} and, when an xref file is there, {@code ensembl_transcript_xref_report.csv}
 * to the working directory.
 *
 * <p>The xref report covers the transcripts left out as NO_GENE (no gene carries their ENSDARG): for each, the ZFIN
 * gene Ensembl's own xref names for that ENSDARG, and whether using it would work. Nothing in it is loaded.
 *
 * <p>The load never withdraws or deletes a transcript. A ZFIN transcript on an assembly whose ENSDART is no longer in
 * the file is listed as NOT_IN_FILE, for a curator to decide on.
 *
 * <p>By hand: {@code TRANSCRIPT_FASTA=/path/cdna.fa.bgz ATTRIBUTION_PUB_ID=ZDB-PUB-... DRY_RUN=false gradle loadEnsemblTranscripts}
 */
@Log4j2
public class LoadEnsemblTranscriptsTask extends AbstractScriptWrapper {

    public static final String LOAD_NAME = "Ensembl Transcripts";
    public static final String UPLOAD_PARAMETER = "TRANSCRIPT_FASTA_UPLOAD";
    public static final String REPORT_FILE = "ensembl_transcript_load_report.csv";
    public static final String SUMMARY_FILE = "ensembl_transcript_load_summary.txt";
    public static final String HTML_REPORT_FILE = "ensembl_transcript_load_report.html";
    public static final String XREF_REPORT_FILE = "ensembl_transcript_xref_report.csv";
    public static final String XREF_UPLOAD_PARAMETER = "XREF_UPLOAD";

    // The owner and annotation method the GRCz11 load (EnsemblTranscriptFastaReadProcess) uses.
    static final String OWNER_ZDB_ID = "ZDB-PERS-060413-1";
    static final String ANNOTATION_METHOD = "Ensembl";
    static final String TU_GENOTYPE = "ZDB-GENO-990623-3";
    static final String AB_GENOTYPE = "ZDB-GENO-960809-7";

    private static final Pattern PUB_ZDB_ID = Pattern.compile("ZDB-PUB-\\d{6}-\\d+");
    private static final Pattern RELEASE = Pattern.compile("/ensembl/(\\d{4}_\\d{2})/");

    enum Action {
        ADDED,
        ALREADY_LOADED,
        ASSEMBLY_OR_ATTRIBUTION_ADDED,
        NO_GENE,
        AMBIGUOUS_GENE,
        UNMAPPED_BIOTYPE,
        NAME_TAKEN,
        REJECTED,
        /** In ZFIN on this assembly, but not in the file any more. Reported only; not about a transcript of the file. */
        NOT_IN_FILE;

        boolean omitted() {
            return this != ADDED && this != ALREADY_LOADED && this != ASSEMBLY_OR_ATTRIBUTION_ADDED && this != NOT_IN_FILE;
        }
    }

    record ReportLine(Action action, String ensdart, String ensdarg, String gene, String assembly, String detail) {
    }

    /** A FASTA to load and where it came from. */
    record Input(String source, Path file, Path xref, boolean uploaded) {
    }

    /** What the xref file says about a transcript whose ENSDARG no gene carries; see {@link #xrefReport}. */
    enum XrefOutcome {
        /** Ensembl names one ZFIN gene, it exists, and has no other ENSDARG for this assembly. */
        WOULD_ADD,
        /** As WOULD_ADD, but the biotype has no ZFIN type, so the transcript would still be skipped. */
        WOULD_ADD_BIOTYPE_UNMAPPED,
        /** The gene already carries a different ENSDARG for this assembly. */
        GENE_HAS_OTHER_ENSDARG,
        GENE_NOT_IN_ZFIN,
        AMBIGUOUS_XREF,
        NO_XREF
    }

    record XrefLine(XrefOutcome outcome, String ensdart, String ensdarg, String biotype, String assembly, String gene, String symbol, String detail) {
    }

    private final boolean dryRun;
    private final String pubId;
    private final String assemblyFilter;
    private final Path uploadedFile;
    private final Path uploadedXref;
    private final Map<String, String> biotypeOverrides;
    private final Path outputDirectory;

    LoadEnsemblTranscriptsTask(Path uploadedFile, Path uploadedXref, String pubId, String assemblyFilter, Map<String, String> biotypeOverrides,
                               boolean dryRun, Path outputDirectory) {
        this.uploadedFile = uploadedFile;
        this.uploadedXref = uploadedXref;
        this.pubId = pubId;
        this.assemblyFilter = assemblyFilter;
        this.biotypeOverrides = biotypeOverrides;
        this.dryRun = dryRun;
        this.outputDirectory = outputDirectory;
    }

    public static void main(String[] args) throws IOException {
        String pubId = env("ATTRIBUTION_PUB_ID");
        if (!PUB_ZDB_ID.matcher(pubId).matches()) {
            fail("ATTRIBUTION_PUB_ID must be a publication ZDB ID like ZDB-PUB-240101-1, got '" + pubId + "'");
        }
        Map<String, String> overrides = parseOverrides(env("BIOTYPE_OVERRIDES"));
        boolean dryRun = !"false".equalsIgnoreCase(env("DRY_RUN"));

        LoadEnsemblTranscriptsTask task = new LoadEnsemblTranscriptsTask(
                uploadedFile(), uploadedXref(), pubId, env("ASSEMBLY"), overrides, dryRun, Paths.get("."));
        task.initAll();
        task.run();
        System.exit(0);
    }

    /** The uploaded or hand-supplied FASTA, or null to download the load_files. */
    private static Path uploadedFile() {
        String workspace = env("WORKSPACE");
        if (!workspace.isEmpty() && Files.isRegularFile(Paths.get(workspace, UPLOAD_PARAMETER))
                && Paths.get(workspace, UPLOAD_PARAMETER).toFile().length() > 0) {
            return Paths.get(workspace, UPLOAD_PARAMETER);
        }
        String path = env("TRANSCRIPT_FASTA");
        if (!path.isEmpty()) {
            if (!Files.isRegularFile(Paths.get(path))) {
                fail("TRANSCRIPT_FASTA is not a file: " + path);
            }
            return Paths.get(path);
        }
        return null;
    }

    /** The uploaded or hand-supplied xref file, or null. */
    private static Path uploadedXref() {
        String workspace = env("WORKSPACE");
        if (!workspace.isEmpty() && Files.isRegularFile(Paths.get(workspace, XREF_UPLOAD_PARAMETER))
                && Paths.get(workspace, XREF_UPLOAD_PARAMETER).toFile().length() > 0) {
            return Paths.get(workspace, XREF_UPLOAD_PARAMETER);
        }
        String path = env("XREF_FILE");
        if (!path.isEmpty()) {
            if (!Files.isRegularFile(Paths.get(path))) {
                fail("XREF_FILE is not a file: " + path);
            }
            return Paths.get(path);
        }
        return null;
    }

    /** {@code lncRNA=lincRNA,vault_RNA=ncRNA} -> biotype to ZFIN transcript type name. */
    static Map<String, String> parseOverrides(String text) {
        Map<String, String> overrides = new LinkedHashMap<>();
        for (String pair : text.split(",")) {
            if (pair.isBlank()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                fail("BIOTYPE_OVERRIDES entries must look like biotype=ZFIN type, got '" + pair + "'");
            }
            overrides.put(parts[0].trim(), parts[1].trim());
        }
        return overrides;
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value.trim();
    }

    private static void fail(String message) {
        System.err.println(message);
        System.exit(1);
    }

    void run() throws IOException {
        log.info("Ensembl transcript load: pub=" + pubId + " assembly=" + (assemblyFilter.isEmpty() ? "(all)" : assemblyFilter)
                + " overrides=" + biotypeOverrides + " dryRun=" + dryRun);
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        List<ReportLine> report = new ArrayList<>();
        StringBuilder fileSummaries = new StringBuilder();
        Map<String, Integer> unmappedBiotypes = new TreeMap<>();
        List<XrefLine> xrefReport = new ArrayList<>();
        try {
            requirePublication(session);
            Map<String, Long> typeIds = transcriptTypeIds(session);
            Map<String, Long> biotypeTypeIds = biotypeTypeIds(typeIds);
            for (Input input : inputs(session)) {
                EnsemblTranscriptFastaFile fasta = EnsemblTranscriptFastaFile.read(input.file());
                if (fasta.entries().isEmpty()) {
                    throw new IllegalStateException(input.file() + " has no usable transcripts");
                }
                String assembly = fasta.assembly();
                if (!assemblyFilter.isEmpty() && !assemblyFilter.equals(assembly)) {
                    fileSummaries.append("Skipped ").append(input.source()).append(": it is for ").append(assembly)
                            .append(", not ").append(assemblyFilter).append('\n');
                    continue;
                }
                fileSummaries.append(input.source()).append(": ").append(assembly).append(", ")
                        .append(fasta.entries().size()).append(" transcripts\n");
                for (EnsemblTranscriptFastaFile.Rejected r : fasta.rejected()) {
                    report.add(new ReportLine(Action.REJECTED, "", "", "", assembly, "line " + r.lineNumber() + ": " + r.reason()));
                }
                int before = report.size();
                EnsemblXrefFile xref = input.xref() == null ? null : EnsemblXrefFile.read(input.xref());
                if (xref == null) {
                    fileSummaries.append("  no xref file, so no xref report for ").append(assembly).append('\n');
                }
                session.doWork(connection -> load(connection, fasta, xref, assembly, biotypeTypeIds, report, xrefReport, unmappedBiotypes));
                fileSummaries.append("  ").append(report.size() - before).append(" report lines\n");
                recordRun(session, input, fasta);
            }
            if (dryRun) {
                session.getTransaction().rollback();
            } else {
                session.getTransaction().commit();
            }
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }
        writeReport(report);
        writeXrefReport(xrefReport);
        writeSummary(report, xrefReport, fileSummaries.toString(), unmappedBiotypes);
        new LoadEnsemblTranscriptsReport().write(outputDirectory.resolve(HTML_REPORT_FILE).toFile(), dryRun, pubId, biotypeOverrides,
                fileSummaries.toString(), report, xrefReport, unmappedBiotypes);
    }

    private void requirePublication(Session session) {
        Number found = (Number) session.createNativeQuery("select count(*) from publication where zdb_id = :pub")
                .setParameter("pub", pubId).getSingleResult();
        if (found.intValue() == 0) {
            throw new IllegalArgumentException("Publication " + pubId + " does not exist");
        }
    }

    private Map<String, Long> transcriptTypeIds(Session session) {
        Map<String, Long> ids = new HashMap<>();
        for (Tuple row : session.createNativeQuery("select tscriptt_type, tscriptt_pk_id from transcript_type", Tuple.class).list()) {
            ids.put(row.get(0, String.class), row.get(1, Number.class).longValue());
        }
        return ids;
    }

    /** Ensembl biotype -> transcript_type id: the GRCz11 map, then the overrides. */
    private Map<String, Long> biotypeTypeIds(Map<String, Long> typeIds) {
        Map<String, Long> result = new HashMap<>();
        EnsemblTranscriptFastaReadProcess.BIOTYPE_TO_ZFIN_TYPE.forEach((biotype, type) -> {
            Long id = typeIds.get(type.toString());
            if (id != null) {
                result.put(biotype, id);
            }
        });
        biotypeOverrides.forEach((biotype, typeName) -> {
            Long id = typeIds.get(typeName);
            if (id == null) {
                throw new IllegalArgumentException("BIOTYPE_OVERRIDES: no transcript type named '" + typeName
                        + "'. Types: " + new TreeSet<>(typeIds.keySet()));
            }
            result.put(biotype, id);
        });
        return result;
    }

    /** The files to load: the upload, or every http(s) load_file of the load, downloaded. */
    private List<Input> inputs(Session session) throws IOException {
        List<Input> inputs = new ArrayList<>();
        if (uploadedFile != null) {
            inputs.add(new Input(LoadFileRegistry.MANUAL_UPLOAD_SOURCE, uploadedFile, uploadedXref, true));
            return inputs;
        }
        List<String> sources = session.createNativeQuery("""
                select lf_source from load_file join load on lf_ld_id = ld_id
                 where ld_name = :load and lf_source like 'http%' order by lf_id
                """, String.class).setParameter("load", LOAD_NAME).list();
        if (sources.isEmpty()) {
            throw new IllegalStateException("The '" + LOAD_NAME + "' load has no http load_file to download. "
                    + "Upload a file instead, or add the URL to load_file.");
        }
        DownloadService downloads = new DownloadService();
        int n = 0;
        for (String source : sources) {
            File local = outputDirectory.resolve("ensembl_transcripts_" + (++n) + ".fa.bgz").toFile();
            log.info("Downloading " + source);
            downloads.downloadFileIfChanged(local, new URL(source));
            inputs.add(new Input(source, local.toPath(), downloadXref(downloads, source, n), false));
        }
        return inputs;
    }

    /** The xref.tsv.gz beside a downloaded FASTA, or null (with a note in the log) if it cannot be had. */
    private Path downloadXref(DownloadService downloads, String fastaSource, int n) {
        String xrefSource = fastaSource.substring(0, fastaSource.lastIndexOf('/') + 1) + "xref.tsv.gz";
        File local = outputDirectory.resolve("ensembl_transcripts_" + n + ".xref.tsv.gz").toFile();
        try {
            downloads.downloadFileIfChanged(local, new URL(xrefSource));
            return local.length() > 0 ? local.toPath() : null;
        } catch (RuntimeException | IOException e) {
            log.warn("No xref file at " + xrefSource + ": " + e.getMessage());
            return null;
        }
    }

    /** Logs the run against the file's load_file, so the load's history shows what was read and when. */
    private void recordRun(Session session, Input input, EnsemblTranscriptFastaFile fasta) throws IOException {
        if (dryRun) {
            return;
        }
        LoadFile loadFile = LoadFileRegistry.getOrCreate(session, LOAD_NAME, input.source(), null);
        Load load = loadFile.getLoad();
        LoadFileLog fileLog = new LoadFileLog();
        fileLog.setLoadFile(loadFile);
        fileLog.setLoadName(load.getName());
        fileLog.setSource(input.source());
        fileLog.setFilename(input.file().getFileName().toString());
        fileLog.setPath(input.file().toAbsolutePath().toString());
        fileLog.setDate(new Date());
        fileLog.setDownloadDate(input.uploaded() ? null : new Date());
        fileLog.setSize(Files.size(input.file()));
        fileLog.setMd5(md5(input.file()));
        Matcher release = RELEASE.matcher(input.source());
        fileLog.setReleaseNumber(release.find() ? release.group(1) : null);
        fileLog.setNotes(fasta.assembly() + ", " + fasta.entries().size() + " transcripts, attributed to " + pubId);
        fileLog.setProcessedDate(new Date());
        session.persist(fileLog);
    }

    private static String md5(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try (var in = Files.newInputStream(file)) {
                byte[] buffer = new byte[1 << 16];
                for (int n; (n = in.read(buffer)) > 0; ) {
                    digest.update(buffer, 0, n);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---- the load itself -------------------------------------------------------------------------------------

    private static List<String[]> query(Connection c, String sql, Object... params) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (PreparedStatement st = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                st.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = st.executeQuery()) {
                int columns = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    String[] row = new String[columns];
                    for (int i = 0; i < columns; i++) {
                        row[i] = rs.getString(i + 1);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private static String single(Connection c, String what, String sql, Object... params) throws SQLException {
        List<String[]> rows = query(c, sql, params);
        if (rows.size() != 1) {
            throw new IllegalStateException("Expected exactly one " + what + " but found " + rows.size());
        }
        return rows.get(0)[0];
    }

    private void load(Connection c, EnsemblTranscriptFastaFile fasta, EnsemblXrefFile xref, String assembly, Map<String, Long> biotypeTypeIds,
                      List<ReportLine> report, List<XrefLine> xrefReport, Map<String, Integer> unmappedBiotypes) throws SQLException {
        long assemblyId = Long.parseLong(single(c, "assembly named " + assembly,
                "select a_pk_id from assembly where a_name = ?", assembly));
        String geneFdbcont = single(c, "Zebrafish reference database Ensembl(" + assembly + ")", """
                select fdbcont_zdb_id from foreign_db_contains join foreign_db on fdbcont_fdb_db_id = fdb_db_pk_id
                 where fdb_db_name = ? and fdbcont_organism_common_name = 'Zebrafish'
                """, "Ensembl(" + assembly + ")");
        String transcriptFdbcont = single(c, "Zebrafish Ensembl_Trans RNA reference database", """
                select fdbcont_zdb_id from foreign_db_contains
                  join foreign_db on fdbcont_fdb_db_id = fdb_db_pk_id
                  join foreign_db_data_type on fdbcont_fdbdt_id = fdbdt_pk_id
                 where fdb_db_name = 'Ensembl_Trans' and fdbdt_data_type = 'RNA' and fdbcont_organism_common_name = 'Zebrafish'
                """);
        String genotype = assembly.endsWith("tu") ? TU_GENOTYPE : assembly.endsWith("ab") ? AB_GENOTYPE : null;
        if (genotype == null) {
            throw new IllegalStateException("Cannot tell the strain of assembly " + assembly + ": its name ends in neither tu nor ab");
        }
        String vocabTerm = single(c, "transcript annotation method '" + ANNOTATION_METHOD + "'", """
                select vt_id from vocabulary_term join vocabulary on vt_v_id = v_id
                 where v_name = 'transcript annotation method' and vt_name = ?
                """, ANNOTATION_METHOD);

        // ENSDARG -> the genes carrying it for this assembly
        Map<String, Set<String>> genesByEnsdarg = new HashMap<>();
        for (String[] row : query(c, "select dblink_acc_num, dblink_linked_recid from db_link where dblink_fdbcont_zdb_id = ?", geneFdbcont)) {
            genesByEnsdarg.computeIfAbsent(row[0], k -> new TreeSet<>()).add(row[1]);
        }
        // ENSDART -> transcript, and its db_link of the transcript reference database
        Map<String, String> transcriptByEnsdart = new HashMap<>();
        for (String[] row : query(c, "select tscript_ensdart_id, tscript_mrkr_zdb_id from transcript where tscript_ensdart_id is not null")) {
            transcriptByEnsdart.put(row[0], row[1]);
        }
        Map<String, String> dblinkByTranscript = new HashMap<>();
        for (String[] row : query(c, "select dblink_linked_recid, dblink_zdb_id from db_link where dblink_fdbcont_zdb_id = ?", transcriptFdbcont)) {
            dblinkByTranscript.putIfAbsent(row[0], row[1]);
        }
        Set<String> assemblyLinks = new HashSet<>();
        for (String[] row : query(c, "select dbla_dblink_zdb_id from db_link_assembly where dbla_a_pk_id = ?", assemblyId)) {
            assemblyLinks.add(row[0]);
        }
        Set<String> attributed = new HashSet<>();
        for (String[] row : query(c, "select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id = ? and recattrib_source_type = 'standard'", pubId)) {
            attributed.add(row[0]);
        }
        // ZFIN transcripts already on this assembly, to notice any the file no longer has
        Map<String, String> onAssembly = new TreeMap<>();
        for (String[] row : query(c, """
                select tscript_ensdart_id, tscript_mrkr_zdb_id
                  from transcript
                  join db_link on dblink_linked_recid = tscript_mrkr_zdb_id and dblink_fdbcont_zdb_id = ?
                  join db_link_assembly on dbla_dblink_zdb_id = dblink_zdb_id and dbla_a_pk_id = ?
                 where tscript_ensdart_id is not null
                """, transcriptFdbcont, assemblyId)) {
            onAssembly.put(row[0], row[1]);
        }
        Map<String, Set<String>> ensdargsByGene = new HashMap<>();
        genesByEnsdarg.forEach((ensdarg, genes) -> genes.forEach(g -> ensdargsByGene.computeIfAbsent(g, k -> new TreeSet<>()).add(ensdarg)));
        Map<String, String> zfinGenes = new HashMap<>();
        if (xref != null) {
            for (String[] row : query(c, "select mrkr_zdb_id, mrkr_abbrev from marker where mrkr_zdb_id like 'ZDB-GENE%'")) {
                zfinGenes.put(row[0], row[1]);
            }
        }
        Set<String> takenNames = new HashSet<>();
        for (String[] row : query(c, "select mrkr_name from marker where mrkr_name like 'ensdart%'")) {
            takenNames.add(row[0]);
        }

        try (PreparedStatement newId = c.prepareStatement("select get_id_and_insert_active_data(?)");
             PreparedStatement insMarker = c.prepareStatement("""
                     insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mrkr_owner)
                     values (?, ?, ?, 'TSCRIPT', ?)""");
             PreparedStatement insTranscript = c.prepareStatement("""
                     insert into transcript (tscript_mrkr_zdb_id, tscript_type_id, tscript_load_id, tscript_ensdart_id,
                                             tscript_genotype_zdb_id, tscript_vocab_id)
                     values (?, ?, ?, ?, ?, ?)""");
             PreparedStatement insRelationship = c.prepareStatement("""
                     insert into marker_relationship (mrel_zdb_id, mrel_type, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id)
                     values (?, 'gene produces transcript', ?, ?)""");
             PreparedStatement insDbLink = c.prepareStatement("""
                     insert into db_link (dblink_linked_recid, dblink_acc_num, dblink_zdb_id, dblink_fdbcont_zdb_id,
                                          dblink_length, dblink_info)
                     values (?, ?, ?, ?, ?, ?)""");
             PreparedStatement insAssembly = c.prepareStatement(
                     "insert into db_link_assembly (dbla_dblink_zdb_id, dbla_a_pk_id) values (?, ?)");
             PreparedStatement insAttribution = c.prepareStatement(
                     "insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) values (?, ?)")) {

            for (EnsemblTranscriptFastaFile.Entry e : fasta.entries()) {
                String where = "line " + e.lineNumber() + ": ";
                String name = e.ensdart().toLowerCase();
                Long typeId = biotypeTypeIds.get(e.biotype());
                if (typeId == null) {
                    unmappedBiotypes.merge(assembly + " " + e.biotype(), 1, Integer::sum);
                    report.add(new ReportLine(Action.UNMAPPED_BIOTYPE, e.ensdart(), e.ensdarg(), "", assembly,
                            where + "biotype " + e.biotype() + " has no ZFIN transcript type"));
                    if (xref != null && genesByEnsdarg.getOrDefault(e.ensdarg(), Set.of()).isEmpty()) {
                        xrefReport.add(xrefLine(xref, e, assembly, false, zfinGenes, ensdargsByGene));
                    }
                    continue;
                }
                Set<String> genes = genesByEnsdarg.getOrDefault(e.ensdarg(), Set.of());
                if (genes.isEmpty()) {
                    report.add(new ReportLine(Action.NO_GENE, e.ensdart(), e.ensdarg(), "", assembly,
                            where + "no gene carries " + e.ensdarg() + " for " + assembly));
                    if (xref != null) {
                        xrefReport.add(xrefLine(xref, e, assembly, typeId != null, zfinGenes, ensdargsByGene));
                    }
                    continue;
                }
                if (genes.size() > 1) {
                    report.add(new ReportLine(Action.AMBIGUOUS_GENE, e.ensdart(), e.ensdarg(), String.join(" ", genes), assembly,
                            where + e.ensdarg() + " is on " + genes.size() + " genes"));
                    continue;
                }
                String gene = genes.iterator().next();

                String transcript = transcriptByEnsdart.get(e.ensdart());
                boolean created = transcript == null;
                boolean changed = false;
                if (created) {
                    if (takenNames.contains(name)) {
                        report.add(new ReportLine(Action.NAME_TAKEN, e.ensdart(), e.ensdarg(), gene, assembly,
                                where + "a marker named " + name + " exists that is not this ENSDART's transcript"));
                        continue;
                    }
                    transcript = newId(newId, "TSCRIPT");
                    String relationship = newId(newId, "MREL");
                    insMarker.setString(1, transcript);
                    insMarker.setString(2, name);
                    insMarker.setString(3, name);
                    insMarker.setString(4, OWNER_ZDB_ID);
                    insMarker.executeUpdate();
                    insTranscript.setString(1, transcript);
                    insTranscript.setLong(2, typeId);
                    insTranscript.setString(3, transcript);
                    insTranscript.setString(4, e.ensdart());
                    insTranscript.setString(5, genotype);
                    insTranscript.setInt(6, Integer.parseInt(vocabTerm));
                    insTranscript.executeUpdate();
                    insRelationship.setString(1, relationship);
                    insRelationship.setString(2, gene);
                    insRelationship.setString(3, transcript);
                    insRelationship.executeUpdate();
                    attribute(insAttribution, attributed, relationship);
                    takenNames.add(name);
                    transcriptByEnsdart.put(e.ensdart(), transcript);
                }
                String dblink = dblinkByTranscript.get(transcript);
                if (dblink == null) {
                    dblink = newId(newId, "DBLINK");
                    insDbLink.setString(1, transcript);
                    insDbLink.setString(2, e.ensdart());
                    insDbLink.setString(3, dblink);
                    insDbLink.setString(4, transcriptFdbcont);
                    insDbLink.setInt(5, e.length());
                    insDbLink.setString(6, "Ensembl Load, " + assembly);
                    insDbLink.executeUpdate();
                    dblinkByTranscript.put(transcript, dblink);
                    changed = true;
                }
                if (assemblyLinks.add(dblink)) {
                    insAssembly.setString(1, dblink);
                    insAssembly.setLong(2, assemblyId);
                    insAssembly.executeUpdate();
                    changed = true;
                }
                if (attribute(insAttribution, attributed, transcript)) {
                    changed = true;
                }
                if (created) {
                    report.add(new ReportLine(Action.ADDED, e.ensdart(), e.ensdarg(), gene, assembly, transcript));
                } else if (changed) {
                    report.add(new ReportLine(Action.ASSEMBLY_OR_ATTRIBUTION_ADDED, e.ensdart(), e.ensdarg(), gene, assembly, transcript));
                } else {
                    report.add(new ReportLine(Action.ALREADY_LOADED, e.ensdart(), e.ensdarg(), gene, assembly, transcript));
                }
            }
        }

        Set<String> inFile = new HashSet<>();
        fasta.entries().forEach(e -> inFile.add(e.ensdart()));
        onAssembly.forEach((ensdart, transcript) -> {
            if (!inFile.contains(ensdart)) {
                report.add(new ReportLine(Action.NOT_IN_FILE, ensdart, "", "", assembly,
                        transcript + " is on " + assembly + " in ZFIN but this file has no " + ensdart));
            }
        });
    }

    /**
     * What the xref file offers for a transcript whose ENSDARG no gene carries. Adding it would mean putting the
     * ENSDARG on the gene (as {@link LoadEnsemblMappingTask} does) as well as loading the transcript.
     */
    private XrefLine xrefLine(EnsemblXrefFile xref, EnsemblTranscriptFastaFile.Entry e, String assembly, boolean biotypeMapped,
                              Map<String, String> zfinGenes, Map<String, Set<String>> ensdargsByGene) {
        Set<EnsemblXrefFile.ZfinGene> candidates = xref.zfinGenes(e.ensdarg());
        if (candidates.isEmpty()) {
            return new XrefLine(XrefOutcome.NO_XREF, e.ensdart(), e.ensdarg(), e.biotype(), assembly, "", "", "the xref file has no ZFIN gene for this ENSDARG");
        }
        if (candidates.size() > 1) {
            return new XrefLine(XrefOutcome.AMBIGUOUS_XREF, e.ensdart(), e.ensdarg(), e.biotype(), assembly,
                    candidates.stream().map(EnsemblXrefFile.ZfinGene::zdbId).reduce((a, b) -> a + " " + b).orElse(""),
                    candidates.stream().map(EnsemblXrefFile.ZfinGene::symbol).reduce((a, b) -> a + " " + b).orElse(""),
                    "the xref file gives this ENSDARG to " + candidates.size() + " ZFIN genes");
        }
        EnsemblXrefFile.ZfinGene gene = candidates.iterator().next();
        if (!zfinGenes.containsKey(gene.zdbId())) {
            return new XrefLine(XrefOutcome.GENE_NOT_IN_ZFIN, e.ensdart(), e.ensdarg(), e.biotype(), assembly, gene.zdbId(), gene.symbol(),
                    "no gene with this ZDB ID in ZFIN");
        }
        Set<String> others = ensdargsByGene.getOrDefault(gene.zdbId(), Set.of());
        if (!others.isEmpty()) {
            return new XrefLine(XrefOutcome.GENE_HAS_OTHER_ENSDARG, e.ensdart(), e.ensdarg(), e.biotype(), assembly, gene.zdbId(), zfinGenes.get(gene.zdbId()),
                    "the gene already carries " + String.join(" ", others) + " for " + assembly);
        }
        return new XrefLine(biotypeMapped ? XrefOutcome.WOULD_ADD : XrefOutcome.WOULD_ADD_BIOTYPE_UNMAPPED, e.ensdart(), e.ensdarg(),
                e.biotype(), assembly, gene.zdbId(), zfinGenes.get(gene.zdbId()),
                biotypeMapped ? "" : "biotype " + e.biotype() + " has no ZFIN transcript type");
    }

    private static String newId(PreparedStatement st, String type) throws SQLException {
        st.setString(1, type);
        try (ResultSet rs = st.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    private boolean attribute(PreparedStatement insert, Set<String> attributed, String dataId) throws SQLException {
        if (!attributed.add(dataId)) {
            return false;
        }
        insert.setString(1, dataId);
        insert.setString(2, pubId);
        insert.executeUpdate();
        return true;
    }

    // ---- reporting -------------------------------------------------------------------------------------------

    private void writeReport(List<ReportLine> report) throws IOException {
        try (Writer writer = Files.newBufferedWriter(outputDirectory.resolve(REPORT_FILE));
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord("action", "ensdart", "ensdarg", "gene", "assembly", "detail");
            for (ReportLine line : report) {
                printer.printRecord(line.action(), line.ensdart(), line.ensdarg(), line.gene(), line.assembly(), line.detail());
            }
        }
    }

    private void writeXrefReport(List<XrefLine> xrefReport) throws IOException {
        if (xrefReport.isEmpty()) {
            return;
        }
        try (Writer writer = Files.newBufferedWriter(outputDirectory.resolve(XREF_REPORT_FILE));
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord("outcome", "ensdart", "ensdarg", "biotype", "assembly", "zfin_gene", "symbol", "detail");
            for (XrefLine line : xrefReport) {
                printer.printRecord(line.outcome(), line.ensdart(), line.ensdarg(), line.biotype(), line.assembly(), line.gene(), line.symbol(), line.detail());
            }
        }
    }

    private void writeSummary(List<ReportLine> report, List<XrefLine> xrefReport, String files, Map<String, Integer> unmappedBiotypes) throws IOException {
        Map<String, Map<Action, Integer>> counts = new TreeMap<>();
        for (ReportLine line : report) {
            counts.computeIfAbsent(line.assembly(), k -> new LinkedHashMap<>()).merge(line.action(), 1, Integer::sum);
        }
        StringBuilder text = new StringBuilder();
        text.append(dryRun ? "DRY RUN: nothing was written to the database.\n\n" : "LOADED into the database.\n\n");
        text.append("Attributed to:       ").append(pubId).append('\n');
        text.append("Biotype overrides:   ").append(biotypeOverrides.isEmpty() ? "(none)" : biotypeOverrides).append('\n');
        text.append("Files:\n").append(files);
        long omitted = report.stream().filter(l -> l.action().omitted()).count();
        text.append("OMITTED:             ").append(omitted).append(" transcripts were not loaded (see the counts below and ")
                .append(REPORT_FILE).append(")\n");
        long notInFile = report.stream().filter(l -> l.action() == Action.NOT_IN_FILE).count();
        text.append("NOT IN FILE:         ").append(notInFile).append(" ZFIN transcripts are on a loaded assembly but no longer in its file. Nothing was changed.\n");
        for (Map.Entry<String, Map<Action, Integer>> byAssembly : counts.entrySet()) {
            text.append('\n').append(byAssembly.getKey()).append('\n');
            for (Action action : Action.values()) {
                Integer count = byAssembly.getValue().get(action);
                if (count != null) {
                    text.append(String.format("  %-32s %d%n", action, count));
                }
            }
        }
        if (!xrefReport.isEmpty()) {
            text.append("\nXREF REPORT (nothing here was loaded). Of the transcripts left out because no gene carries their ENSDARG,\n")
                    .append("what the Ensembl xref file offers; using it would also mean adding the ENSDARG to the gene:\n");
            Map<String, Map<XrefOutcome, Integer>> byAssembly = new TreeMap<>();
            Map<String, Set<String>> genesToLink = new TreeMap<>();
            for (XrefLine line : xrefReport) {
                byAssembly.computeIfAbsent(line.assembly(), k -> new LinkedHashMap<>()).merge(line.outcome(), 1, Integer::sum);
                if (line.outcome() == XrefOutcome.WOULD_ADD) {
                    genesToLink.computeIfAbsent(line.assembly(), k -> new TreeSet<>()).add(line.gene());
                }
            }
            byAssembly.forEach((assembly, outcomes) -> {
                text.append("  ").append(assembly).append('\n');
                for (XrefOutcome outcome : XrefOutcome.values()) {
                    Integer count = outcomes.get(outcome);
                    if (count != null) {
                        text.append(String.format("    %-32s %d%n", outcome, count));
                    }
                }
                text.append(String.format("    %-32s %d%n", "(genes that would get an ENSDARG)", genesToLink.getOrDefault(assembly, Set.of()).size()));
            });
            text.append("  Per-transcript detail: ").append(XREF_REPORT_FILE).append('\n');
        }
        if (!unmappedBiotypes.isEmpty()) {
            text.append("\nBiotypes with no ZFIN transcript type, skipped (set BIOTYPE_OVERRIDES to load them):\n");
            unmappedBiotypes.forEach((biotype, n) -> text.append(String.format("  %-40s %d%n", biotype, n)));
        }
        text.append("\nPer-transcript detail: ").append(REPORT_FILE).append('\n');
        Files.writeString(outputDirectory.resolve(SUMMARY_FILE), text.toString());
        System.out.println(text);
    }
}
