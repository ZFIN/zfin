package org.zfin.solr.diagnostics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CursorMarkParams;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Dump every doc from a Solr core to one ndjson file, id-sorted, with
 * volatile per-doc fields removed and keys sorted, so two runs can be
 * diffed directly. Replaces {@code solr-dump.sh}.
 *
 * <p>Behavioural note: the bash version used {@code curl + jq} and
 * occasionally produced ~0.1% malformed lines on multi-GB dumps (a
 * concurrent-write artifact between curl pagination and jq's appends).
 * This implementation writes sequentially from a single process, so
 * that class of corruption can't happen.
 */
public class DumpTool implements DiagTool {

    private static final Logger logger = LogManager.getLogger(DumpTool.class);

    /** Per-doc fields stripped before write — they vary across reindexes. */
    private static final Set<String> VOLATILE_FIELDS = Set.of("_version_", "timestamp", "_root_");

    private static final int DEFAULT_ROWS = 1000;

    @Override public String name()  { return "dump"; }
    @Override public String usage() { return "dump <outfile.ndjson>"; }

    @Override
    public void run(DiagContext ctx, String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException("usage: " + usage());
        Path out = ctx.resolvePath(args[0]);
        Files.createDirectories(out.getParent());

        ObjectMapper mapper = new ObjectMapper();
        int rows = Integer.parseInt(System.getenv().getOrDefault("ROWS", String.valueOf(DEFAULT_ROWS)));

        try (BufferedWriter writer = Files.newBufferedWriter(out)) {
            SolrQuery query = new SolrQuery("*:*")
                .setRows(rows)
                .setSort("id", SolrQuery.ORDER.asc);
            String cursor = CursorMarkParams.CURSOR_MARK_START;
            int page = 0, total = 0;

            while (true) {
                query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursor);
                QueryResponse resp = ctx.coreClient().query(query);
                for (SolrDocument doc : resp.getResults()) {
                    writer.write(mapper.writeValueAsString(canonicalize(doc)));
                    writer.newLine();
                    total++;
                }
                page++;
                String next = resp.getNextCursorMark();
                if (page % 25 == 0) {
                    logger.info("  page {}  total={}  cursor={}", page, total, next);
                }
                if (next.equals(cursor)) break;
                cursor = next;
            }
            logger.info("done: {} docs written to {}", total, out);
        }
    }

    /**
     * Drop volatile fields and return a TreeMap so Jackson serializes
     * keys in lexical order — gives byte-stable output across runs,
     * matching the jq {@code --sort-keys} flag the bash version used.
     */
    private static Map<String, Object> canonicalize(SolrDocument doc) {
        Map<String, Object> ordered = new TreeMap<>();
        for (Map.Entry<String, Object> entry : doc.entrySet()) {
            if (VOLATILE_FIELDS.contains(entry.getKey())) continue;
            ordered.put(entry.getKey(), entry.getValue());
        }
        // LinkedHashMap copy preserves the TreeMap ordering for Jackson,
        // while keeping a stable iteration order even if a downstream
        // consumer happens to reflect on the map type.
        return new LinkedHashMap<>(ordered);
    }
}
