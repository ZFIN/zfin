package org.zfin.solr.diagnostics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Run each query from {@code <queries-file>} against a Solr core,
 * capturing the bits that matter for "did the cutover change behaviour":
 * numFound, the first N document ids, and the facet block. One canonical
 * JSON file is written per query input. Running this against two Solr
 * versions with the same query list produces directories that
 * {@code diff -r} cleanly. Replaces {@code solr-queries.sh}.
 *
 * <p>Queries file format: one query per line.
 * <ul>
 *   <li>A bare value, e.g. {@code tp53} — wrapped as {@code q=tp53}.</li>
 *   <li>A full URL-encoded query string, e.g.
 *       {@code q=*:*&facet=true&facet.field=category} — passed through
 *       parameter-by-parameter (we re-decode and re-encode via SolrJ,
 *       so the wire format is consistent regardless of source).</li>
 * </ul>
 * Blank lines and lines starting with {@code #} are ignored.
 *
 * <p>Solr-side errors are recorded in the per-query JSON rather than
 * aborting the run; the goal is to capture the SET of erroring queries
 * so it diffs cleanly across versions.
 */
public class QueriesTool implements DiagTool {

    private static final Logger logger = LogManager.getLogger(QueriesTool.class);

    private static final int DEFAULT_TOPN = 5;

    private final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    @Override public String name()  { return "queries"; }
    @Override public String usage() { return "queries <queries-file> <outdir>"; }

    @Override
    public void run(DiagContext ctx, String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("usage: " + usage());
        Path qFile  = ctx.resolvePath(args[0]);
        Path outDir = ctx.resolvePath(args[1]);
        Files.createDirectories(outDir);

        int topN = Integer.parseInt(System.getenv().getOrDefault("TOPN", String.valueOf(DEFAULT_TOPN)));

        int n = 0;
        for (String line : Files.readAllLines(qFile)) {
            String trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            n++;

            Path outFile = outDir.resolve(filenameFor(n, trimmed));
            Map<String, Object> canonical = runQuery(ctx, trimmed, topN);
            writeJson(outFile, canonical);

            Object nf = canonical.getOrDefault("numFound",
                canonical.getOrDefault("error", "?"));
            logger.info("  q[{}]: {} -> {}", n, trimmed, nf);
        }
        logger.info("done: {} queries -> {}", n, outDir);
    }

    /**
     * Run one query and reshape the response into the canonical record
     * the bash version produced. Catches SolrException + everything
     * else so a single bad query doesn't kill the whole run.
     */
    private Map<String, Object> runQuery(DiagContext ctx, String line, int topN) {
        ModifiableSolrParams params = buildParams(line);
        params.set("rows", String.valueOf(topN));
        params.set("fl", "id");
        params.set("wt", "json");

        // Records are written in insertion order; using LinkedHashMap keeps
        // the JSON shape consistent regardless of where SolrJ returned the
        // facet block in its NamedList.
        Map<String, Object> rec = new LinkedHashMap<>();
        rec.put("query", line);
        try {
            QueryResponse resp = ctx.coreClient().query(params);
            long numFound = resp.getResults() == null ? 0L : resp.getResults().getNumFound();
            List<String> ids = new ArrayList<>();
            if (resp.getResults() != null) {
                for (SolrDocument doc : resp.getResults()) {
                    Object id = doc.getFieldValue("id");
                    ids.add(id == null ? null : id.toString());
                }
            }
            rec.put("numFound", numFound);
            rec.put("topIds",   ids);
            rec.put("facets",   facetsAsMap(resp));
            rec.put("error",    null);
        } catch (SolrException e) {
            rec.put("numFound", null);
            rec.put("topIds",   List.of());
            rec.put("facets",   Map.of());
            rec.put("error",    e.getMessage());
        } catch (Exception e) {
            rec.put("numFound", null);
            rec.put("topIds",   List.of());
            rec.put("facets",   Map.of());
            rec.put("error",    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return rec;
    }

    /**
     * Parse a queries-file line into SolrJ params.
     *
     * <p>If the line contains {@code =}, treat it as URL-encoded params
     * (e.g. {@code q=*:*&facet=true&facet.field=category}) and decode
     * each pair so SolrJ can re-encode consistently. Otherwise, treat
     * the whole line as a bare {@code q=} value.
     */
    private ModifiableSolrParams buildParams(String line) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        if (!line.contains("=")) {
            params.set("q", line);
            return params;
        }
        for (String pair : line.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) {
                params.add(decode(pair), "");
            } else {
                params.add(decode(pair.substring(0, eq)), decode(pair.substring(eq + 1)));
            }
        }
        return params;
    }

    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return s; // UTF-8 always present; this can't happen
        }
    }

    /** Convert SolrJ's facet object graph back into plain maps for JSON output. */
    private Map<String, Object> facetsAsMap(QueryResponse resp) {
        Map<String, Object> out = new TreeMap<>();
        if (resp.getFacetFields() != null) {
            Map<String, Object> ff = new TreeMap<>();
            resp.getFacetFields().forEach(f -> {
                Map<String, Long> counts = new LinkedHashMap<>();
                f.getValues().forEach(v -> counts.put(v.getName(), v.getCount()));
                ff.put(f.getName(), counts);
            });
            out.put("facet_fields", ff);
        }
        if (resp.getFacetQuery() != null && !resp.getFacetQuery().isEmpty()) {
            out.put("facet_queries", new TreeMap<>(resp.getFacetQuery()));
        }
        if (resp.getFacetRanges() != null && !resp.getFacetRanges().isEmpty()) {
            // RangeFacet has complex shape; emit its toString() rather than
            // re-implementing the model — the bash version's diff didn't
            // exercise range facets in practice.
            Map<String, String> rng = new TreeMap<>();
            resp.getFacetRanges().forEach(r -> rng.put(r.getName(), r.toString()));
            out.put("facet_ranges", rng);
        }
        return out;
    }

    private static String filenameFor(int index, String query) {
        // Slugify: any non-[A-Za-z0-9] → '_', first 40 chars.
        StringBuilder slug = new StringBuilder(40);
        int n = Math.min(query.length(), 40);
        for (int i = 0; i < n; i++) {
            char c = query.charAt(i);
            slug.append((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ? c : '_');
        }
        return String.format("q%04d-%s.json", index, slug.toString());
    }

    private void writeJson(Path file, Map<String, Object> rec) throws Exception {
        // TreeMap copy for the top-level ensures alphabetic key order
        // (so two snapshots diff cleanly), but preserves the nested
        // facet structures the canonical reshape built.
        Map<String, Object> sorted = new TreeMap<>(rec);
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            mapper.writeValue(w, sorted);
        }
    }

}
