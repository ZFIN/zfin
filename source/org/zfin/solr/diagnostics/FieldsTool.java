package org.zfin.solr.diagnostics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.LukeResponse.FieldInfo;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Emit the names of every indexed field in a Solr core, one per line,
 * sorted. Replaces {@code solr-fields.sh}.
 *
 * <p>Pulls live schema info via {@code /admin/luke} instead of parsing
 * {@code schema.xml} — this catches dynamic fields, copyField-created
 * fields, and any divergence between the source schema and what the
 * running core actually has.
 *
 * <p>"Indexed" here means Luke's schema flag string starts with {@code I}
 * (the schema flags are a positional bitmap: position 0 = I(ndexed),
 * 1 = T(okenized), 2 = S(tored), …). System fields like {@code _version_}
 * pass the filter; that's intentional — characterization should cover
 * them too, and {@link DumpTool}/{@link TermsTool} strip per-doc
 * volatility separately.
 */
public class FieldsTool implements DiagTool {

    private static final Logger logger = LogManager.getLogger(FieldsTool.class);

    @Override public String name()  { return "fields"; }
    @Override public String usage() { return "fields [outfile]   (default: stdout)"; }

    @Override
    public void run(DiagContext ctx, String[] args) throws Exception {
        List<String> indexedFields = collectIndexedFields(ctx);

        if (args.length >= 1) {
            Path out = ctx.resolvePath(args[0]);
            if (out.getParent() != null) Files.createDirectories(out.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(out)) {
                for (String name : indexedFields) {
                    w.write(name);
                    w.newLine();
                }
            }
            logger.info("wrote {} indexed field names to {}", indexedFields.size(), out);
        } else {
            // No outfile arg → stdout. solr-characterize redirects the
            // output to a file via shell; the standalone use case (a
            // dev typing `solrDiag fields`) wants the list inline.
            PrintStream out = System.out;
            for (String name : indexedFields) out.println(name);
        }
    }

    List<String> collectIndexedFields(DiagContext ctx) throws Exception {
        // numTerms=0: we only need schema info, not term enumerations.
        // Cuts Luke's response from ~10 MB to ~50 KB on the site_index core.
        LukeRequest req = new LukeRequest();
        req.setNumTerms(0);
        LukeResponse resp = req.process(ctx.coreClient());

        List<String> out = new ArrayList<>();
        Map<String, FieldInfo> fields = resp.getFieldInfo();
        if (fields != null) {
            for (Map.Entry<String, FieldInfo> e : fields.entrySet()) {
                String schemaFlags = e.getValue().getSchema();
                if (schemaFlags != null && schemaFlags.startsWith("I")) {
                    out.add(e.getKey());
                }
            }
        }
        out.sort(String::compareTo);
        return out;
    }
}
