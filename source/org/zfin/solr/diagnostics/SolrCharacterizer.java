package org.zfin.solr.diagnostics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SimpleSolrResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main entrypoint for the Solr diagnostics suite — a Java port of the
 * legacy {@code solr-*.sh} script cluster. Invoked via two gradle tasks:
 *
 * <pre>
 *   gradle solrCharacterize -PdiagArgs="&lt;outdir&gt; [queries-file]"
 *   gradle solrDiag         -PdiagArgs="&lt;subcommand&gt; [args...]"
 * </pre>
 *
 * <p>Subcommands (each maps to one {@link DiagTool}):
 * <ul>
 *   <li>{@code all} — full characterization pipeline (meta + fields +
 *       docs + terms + queries). The default for {@code solrCharacterize}.</li>
 *   <li>{@code dump}, {@code fields}, {@code terms}, {@code queries},
 *       {@code analyze}, {@code queries-from-log} — individual tools.</li>
 * </ul>
 *
 * <p>Endpoint defaults: {@code SOLR}/{@code CORE} env vars or
 * {@link org.zfin.properties.ZfinPropertiesEnum}. See {@link DiagContext}.
 *
 * <p>Extends {@link AbstractValidateDataReportTask} to share the
 * properties-load / logger-setup harness with the other Solr jobs
 * ({@code SolrIndexerJob}, {@code SolrReindexOrchestrator}), even though
 * this tool doesn't write report artifacts in the same way.
 */
public class SolrCharacterizer extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(SolrCharacterizer.class);

    private static final Map<String, DiagTool> TOOLS = new LinkedHashMap<>();
    static {
        register(new DumpTool());
        register(new FieldsTool());
        register(new TermsTool());
        register(new QueriesTool());
        register(new AnalyzeTool());
        register(new LogQueryExtractor());
    }
    private static void register(DiagTool t) { TOOLS.put(t.name(), t); }

    /** Diagnostic CLI args (subcommand + positional). null until parsed from args[]. */
    private String[] diagArgs;

    public SolrCharacterizer(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        // The gradle task passes the standard AbstractValidateDataReportTask
        // triple first, then the diag CLI as a 4th packed string.
        var job = new SolrCharacterizer(args[2], args[0], args[1]);
        job.diagArgs = args.length > 3 ? splitArgs(args[3]) : new String[0];
        job.initDatabase(false);   // Solr-only tool — no Hibernate session needed
        System.exit(job.execute());
    }

    /**
     * Split the gradle-packed args string into tokens, respecting quotes.
     * Examples:
     * <pre>
     *   "all ./baseline-solr8"           → ["all", "./baseline-solr8"]
     *   "queries \"my queries.txt\" ./q" → ["queries", "my queries.txt", "./q"]
     * </pre>
     */
    static String[] splitArgs(String packed) {
        if (packed == null || packed.isBlank()) return new String[0];
        // Reasonably simple tokenizer — gradle args are dev-typed,
        // not adversarial input, so a regex-light pass is enough.
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < packed.length(); i++) {
            char c = packed.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (cur.length() > 0) { tokens.add(cur.toString()); cur.setLength(0); }
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) tokens.add(cur.toString());
        return tokens.toArray(new String[0]);
    }

    @Override
    public int execute() {
        setLoggerFile();
        clearReportDirectory();

        if (diagArgs.length == 0) {
            usage();
            return 2;
        }

        String sub = diagArgs[0];
        String[] toolArgs = Arrays.copyOfRange(diagArgs, 1, diagArgs.length);

        try (DiagContext ctx = DiagContext.fromEnvOrProperties()) {
            if ("all".equals(sub) || "characterize".equals(sub)) {
                return runAll(ctx, toolArgs);
            }
            DiagTool tool = TOOLS.get(sub);
            if (tool == null) {
                logger.error("unknown subcommand: {}", sub);
                usage();
                return 2;
            }
            tool.run(ctx, toolArgs);
            return 0;
        } catch (Exception e) {
            logger.error("diagnostic failed", e);
            return 1;
        }
    }

    private void usage() {
        logger.info("Usage:");
        logger.info("  solrCharacterize:  all <outdir> [queries-file]");
        for (DiagTool t : TOOLS.values()) {
            logger.info("  solrDiag:          {}", t.usage());
        }
    }

    // ---------- the "all" pipeline -----------------------------------------

    /**
     * Run the full characterization pipeline — the orchestrator the bash
     * {@code solr-characterize.sh} script implemented. Output layout
     * (unchanged from the legacy version, so existing comparison scripts
     * keep working):
     *
     * <pre>
     *   &lt;outdir&gt;/
     *       meta.txt        solr endpoint, version, doc count, snapshot timestamp
     *       fields.txt      indexed field names, one per line
     *       docs.ndjson     full doc dump (one per line, canonical, id-sorted)
     *       terms/&lt;f&gt;.tsv per-field term -&gt; frequency
     *       queries/*.json  one canonical JSON file per query input
     * </pre>
     */
    private int runAll(DiagContext ctx, String[] args) throws Exception {
        if (args.length < 1) {
            logger.error("usage: all <outdir> [queries-file]");
            return 2;
        }
        Path outDir = ctx.resolvePath(args[0]);
        Path queriesFile = args.length >= 2
            ? ctx.resolvePath(args[1])
            : ctx.resolvePath("server_apps/solr/diagnostics/solr-queries.txt");

        Files.createDirectories(outDir);
        Files.createDirectories(outDir.resolve("terms"));
        Files.createDirectories(outDir.resolve("queries"));

        // --- meta --------------------------------------------------------
        // Capture identity info so a stale checkout doesn't get mistaken
        // for a fresh characterization later.
        Path meta = outDir.resolve("meta.txt");
        try (BufferedWriter w = Files.newBufferedWriter(meta)) {
            w.write("solr_endpoint=" + ctx.solrBase()); w.newLine();
            w.write("core=" + ctx.core()); w.newLine();
            w.write("snapshot_at=" + DateTimeFormatter.ISO_INSTANT
                .withZone(ZoneOffset.UTC).format(Instant.now())); w.newLine();
            w.write("solr_version=" + solrVersion(ctx)); w.newLine();
            w.write("doc_count=" + docCount(ctx)); w.newLine();
        }
        logger.info("[characterize] meta:");
        for (String line : Files.readAllLines(meta)) logger.info("  {}", line);

        // --- fields list ---------------------------------------------------
        logger.info("[characterize] enumerating indexed fields");
        Path fieldsFile = outDir.resolve("fields.txt");
        TOOLS.get("fields").run(ctx, new String[]{ fieldsFile.toString() });
        logger.info("  {} fields", Files.readAllLines(fieldsFile).size());

        // --- doc dump ------------------------------------------------------
        logger.info("[characterize] dumping all docs (ndjson)");
        Path docsFile = outDir.resolve("docs.ndjson");
        TOOLS.get("dump").run(ctx, new String[]{ docsFile.toString() });
        logger.info("  {} docs", Files.readAllLines(docsFile).size());

        // --- per-field terms ----------------------------------------------
        logger.info("[characterize] dumping per-field terms");
        TOOLS.get("terms").run(ctx, new String[]{
            outDir.resolve("terms").toString(),
            fieldsFile.toString()
        });

        // --- query suite ---------------------------------------------------
        if (Files.isReadable(queriesFile)) {
            logger.info("[characterize] running query suite from {}", queriesFile);
            TOOLS.get("queries").run(ctx, new String[]{
                queriesFile.toString(),
                outDir.resolve("queries").toString()
            });
        } else {
            logger.warn("[characterize] skipping query suite (no {})", queriesFile);
            logger.warn("  build one with: solrDiag -PdiagArgs=\"queries-from-log /var/log/httpd/zfin_access {}\"",
                queriesFile);
        }

        logger.info("[characterize] done -> {}", outDir);
        return 0;
    }

    // ---------- meta helpers -----------------------------------------------

    private String solrVersion(DiagContext ctx) {
        // /admin/info/system is server-scoped (no core path segment).
        // Build a GenericSolrRequest against the admin client.
        try {
            ModifiableSolrParams p = new ModifiableSolrParams();
            p.set("wt", "json");
            GenericSolrRequest req = new GenericSolrRequest(SolrRequest.METHOD.GET, "/admin/info/system", p);
            SimpleSolrResponse resp = req.process(ctx.adminClient());
            // Solr returns lucene.solr-spec-version under .lucene. Dig in.
            NamedList<Object> root = resp.getResponse();
            Object lucene = root == null ? null : root.get("lucene");
            if (lucene instanceof NamedList<?> nl) {
                Object v = nl.get("solr-spec-version");
                if (v != null) return v.toString();
            }
        } catch (Exception e) {
            logger.warn("could not read solr version: {}", e.getMessage());
        }
        return "unknown";
    }

    private long docCount(DiagContext ctx) {
        try {
            QueryResponse resp = ctx.coreClient().query(new SolrQuery("*:*").setRows(0));
            return resp.getResults() == null ? 0 : resp.getResults().getNumFound();
        } catch (Exception e) {
            logger.warn("could not read doc count: {}", e.getMessage());
            return -1;
        }
    }
}
