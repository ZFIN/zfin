package org.zfin.solr.diagnostics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.client.solrj.response.AnalysisResponseBase.AnalysisPhase;
import org.apache.solr.client.solrj.response.AnalysisResponseBase.TokenInfo;
import org.apache.solr.client.solrj.response.FieldAnalysisResponse;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Run Solr's {@code /analysis/field} handler for each (fieldType, input)
 * pair so two schema variants can be diffed without a reindex — after
 * rebuilding the solr image with a new schema and restarting the
 * container (or {@code ant reload-solr-core} after a config edit), the
 * analyzers reflect the new config immediately. Only token output
 * changes here; stored docs are untouched. Replaces {@code solr-analyze.sh}.
 *
 * <p>Writes two outputs:
 * <ul>
 *   <li>{@code <out>.json} — full per-pair record with index- and
 *       query-side analyzer phases.</li>
 *   <li>{@code <out>.tokens.txt} — companion human-readable summary,
 *       just the final token list per (fieldtype, input) row.</li>
 * </ul>
 */
public class AnalyzeTool implements DiagTool {

    private static final Logger logger = LogManager.getLogger(AnalyzeTool.class);

    /** Field types exercised when {@code TYPES} env var isn't set — matches the bash default. */
    private static final List<String> DEFAULT_TYPES = List.of("text", "edgytext", "simplified-international-text");

    private final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    @Override public String name()  { return "analyze"; }
    @Override public String usage() { return "analyze <inputs-file> <out.json>"; }

    @Override
    public void run(DiagContext ctx, String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("usage: " + usage());
        Path inputsFile = ctx.resolvePath(args[0]);
        Path out        = ctx.resolvePath(args[1]);
        if (out.getParent() != null) Files.createDirectories(out.getParent());

        List<String> types = pickTypes();
        List<String> inputs = readNonComment(inputsFile);

        List<Map<String, Object>> records = new ArrayList<>();
        for (String input : inputs) {
            for (String fieldtype : types) {
                records.add(runOne(ctx, fieldtype, input));
            }
        }

        // Full JSON dump — sorted top-level keys via ObjectMapper feature
        // means each record's {analysis, fieldtype, input} surface in
        // alphabetic order regardless of insertion order.
        try (BufferedWriter w = Files.newBufferedWriter(out)) {
            mapper.writeValue(w, records);
        }
        logger.info("wrote: full json:  {}", out);

        // Companion token-only flat file. Path swap: <name>.json → <name>.tokens.txt
        Path flat = out.resolveSibling(stripJsonExtension(out.getFileName().toString()) + ".tokens.txt");
        try (BufferedWriter w = Files.newBufferedWriter(flat)) {
            for (Map<String, Object> rec : records) {
                w.write("fieldtype=" + rec.get("fieldtype") + "  input=" + rec.get("input"));
                w.newLine();
                w.write("  index: " + tokensFromLastPhase(rec, "index"));
                w.newLine();
                w.write("  query: " + tokensFromLastPhase(rec, "query"));
                w.newLine();
                w.newLine();
            }
        }
        logger.info("wrote: token-only flat: {}", flat);
    }

    private List<String> pickTypes() {
        String env = System.getenv("TYPES");
        if (env == null || env.isBlank()) return DEFAULT_TYPES;
        return List.of(env.trim().split("\\s+"));
    }

    private static List<String> readNonComment(Path p) throws Exception {
        List<String> out = new ArrayList<>();
        for (String line : Files.readAllLines(p)) {
            String s = line.strip();
            if (s.isEmpty() || s.startsWith("#")) continue;
            out.add(line); // preserve leading/trailing spaces in payload
        }
        return out;
    }

    private Map<String, Object> runOne(DiagContext ctx, String fieldtype, String input) throws Exception {
        FieldAnalysisRequest req = new FieldAnalysisRequest("/analysis/field");
        req.addFieldType(fieldtype);
        req.setFieldValue(input);
        req.setQuery(input);

        FieldAnalysisResponse resp = req.process(ctx.coreClient());
        Map<String, Object> rec = new LinkedHashMap<>();
        rec.put("fieldtype", fieldtype);
        rec.put("input",     input);
        rec.put("analysis",  fieldTypeAnalysis(resp, fieldtype));
        return rec;
    }

    /**
     * Reshape SolrJ's analysis response into the {@code analysis} block the
     * bash version produced via {@code jq '{analysis: .analysis}'} — a
     * map of fieldtype → {index: [phases], query: [phases]}, where each
     * phase is itself a list of {text, ...} maps.
     */
    private Map<String, Object> fieldTypeAnalysis(FieldAnalysisResponse resp, String fieldtype) {
        Map<String, Object> wrapper = new TreeMap<>();
        Map<String, Object> typeBlock = new TreeMap<>();

        var analysis = resp.getFieldTypeAnalysis(fieldtype);
        if (analysis != null) {
            typeBlock.put("index", phasesAsList(analysis.getIndexPhases()));
            typeBlock.put("query", phasesAsList(analysis.getQueryPhases()));
        }
        wrapper.put("field_types", Map.of(fieldtype, typeBlock));
        return wrapper;
    }

    private List<Object> phasesAsList(Iterable<AnalysisPhase> phases) {
        List<Object> out = new ArrayList<>();
        if (phases == null) return out;
        for (AnalysisPhase phase : phases) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("class", phase.getClassName());
            List<Map<String, Object>> tokens = new ArrayList<>();
            for (TokenInfo t : phase.getTokens()) {
                Map<String, Object> tok = new LinkedHashMap<>();
                tok.put("text",  t.getText());
                tok.put("type",  t.getType());
                tok.put("start", t.getStart());
                tok.put("end",   t.getEnd());
                tokens.add(tok);
            }
            p.put("tokens", tokens);
            out.add(p);
        }
        return out;
    }

    /**
     * Pull just the final phase's token-text list for the human-readable
     * summary. Mirrors the bash {@code .[].[-1]? | .[]?.text} jq path.
     */
    @SuppressWarnings("unchecked")
    private String tokensFromLastPhase(Map<String, Object> rec, String side) {
        Map<String, Object> analysis  = (Map<String, Object>) rec.get("analysis");
        Map<String, Object> fieldTypes = analysis == null ? null : (Map<String, Object>) analysis.get("field_types");
        if (fieldTypes == null || fieldTypes.isEmpty()) return "";
        Map<String, Object> typeBlock = (Map<String, Object>) fieldTypes.values().iterator().next();
        List<Object> phases = typeBlock == null ? null : (List<Object>) typeBlock.get(side);
        if (phases == null || phases.isEmpty()) return "";
        Map<String, Object> last = (Map<String, Object>) phases.get(phases.size() - 1);
        List<Map<String, Object>> tokens = (List<Map<String, Object>>) last.get("tokens");
        if (tokens == null) return "";
        return tokens.stream()
            .map(t -> String.valueOf(t.get("text")))
            .reduce((a, b) -> a + " | " + b)
            .orElse("");
    }

    private static String stripJsonExtension(String name) {
        return name.endsWith(".json") ? name.substring(0, name.length() - ".json".length()) : name;
    }
}
