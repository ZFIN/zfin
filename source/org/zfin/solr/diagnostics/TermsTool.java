package org.zfin.solr.diagnostics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Dump the indexed term dictionary for each requested field, one TSV per
 * field (term &lt;TAB&gt; freq), term-sorted. Diffable across two reindexed
 * cores to surface what an analyzer change actually produced. Replaces
 * {@code solr-terms.sh}.
 *
 * <p>Reindex required on each side before running this — terms only
 * reflect what's already committed to the index, not the live schema.
 *
 * <p>Two compatibility knobs ride on each /terms call, matching the bash
 * version's hard-won notes:
 * <ul>
 *   <li>{@code terms=true} — Solr 8's {@code /terms} request handler
 *       does not auto-activate the TermsComponent; without this flag
 *       it returns just a responseHeader and an empty terms block.
 *       Harmless on Solr 9, mandatory on Solr 8.</li>
 *   <li>{@code json.nl=flat} — forces the terms list to come back as a
 *       flat {@code ["term", count, "term", count, …]} array on every
 *       Solr version. Without it, Solr 8 may return an object/map per
 *       field which diverges from Solr 9's array.</li>
 * </ul>
 */
public class TermsTool implements DiagTool {

    private static final Logger logger = LogManager.getLogger(TermsTool.class);

    private static final int DEFAULT_LIMIT = -1; // -1 = unlimited (Solr semantics)

    @Override public String name()  { return "terms"; }
    @Override public String usage() { return "terms <outdir> <field-list-file>"; }

    @Override
    public void run(DiagContext ctx, String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("usage: " + usage());
        Path outDir     = ctx.resolvePath(args[0]);
        Path fieldsFile = ctx.resolvePath(args[1]);
        Files.createDirectories(outDir);

        int limit = Integer.parseInt(System.getenv().getOrDefault("LIMIT", String.valueOf(DEFAULT_LIMIT)));

        for (String field : readFieldList(fieldsFile)) {
            int count = dumpField(ctx, field, limit, outDir.resolve(field + ".tsv"));
            logger.info("  {}: {} terms", field, count);
        }
        logger.info("done: per-field term lists in {}", outDir);
    }

    private static List<String> readFieldList(Path file) throws Exception {
        List<String> out = new ArrayList<>();
        for (String line : Files.readAllLines(file)) {
            String trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            out.add(trimmed);
        }
        return out;
    }

    private int dumpField(DiagContext ctx, String field, int limit, Path outFile) throws Exception {
        SolrQuery q = new SolrQuery();
        q.setRequestHandler("/terms");
        q.set("terms", true);
        q.set("terms.fl", field);
        q.set("terms.limit", String.valueOf(limit));
        q.set("terms.sort", "index");
        q.set("json.nl", "flat");

        QueryResponse resp = ctx.coreClient().query(q);
        NamedList<Object> root  = resp.getResponse();
        NamedList<?> terms      = (NamedList<?>) root.get("terms");
        // With json.nl=flat the per-field value is a flat ["term", count, ...]
        // array; SolrJ surfaces it as a List<Object>. Defensively allow
        // NamedList (the json.nl=arr default shape) too in case a future
        // Solr config changes the response writer hint.
        Object payload = terms == null ? null : terms.get(field);

        try (BufferedWriter w = Files.newBufferedWriter(outFile)) {
            if (payload instanceof List<?> list) {
                int written = 0;
                // entries come in (term, count, term, count, ...) order
                for (int i = 0; i + 1 < list.size(); i += 2) {
                    w.write(String.valueOf(list.get(i)));
                    w.write('\t');
                    w.write(String.valueOf(list.get(i + 1)));
                    w.newLine();
                    written++;
                }
                return written;
            }
            if (payload instanceof NamedList<?> nl) {
                int written = 0;
                for (int i = 0; i < nl.size(); i++) {
                    w.write(nl.getName(i));
                    w.write('\t');
                    w.write(String.valueOf(nl.getVal(i)));
                    w.newLine();
                    written++;
                }
                return written;
            }
            return 0;
        }
    }
}
