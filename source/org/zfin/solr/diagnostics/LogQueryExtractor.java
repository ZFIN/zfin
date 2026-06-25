package org.zfin.solr.diagnostics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Extract {@code q=...} values from an Apache access log, dedupe, count
 * by frequency, and emit the top N as a queries-file ready for
 * {@link QueriesTool}. Replaces {@code solr-queries-from-log.sh}.
 *
 * <p>Filtering matches the popularity-pipeline's: internal IPs
 * (128.223.56/57 = ZFIN office), monitoring probes, and known bot UAs
 * are dropped. The value stays URL-encoded as the user submitted it —
 * {@link QueriesTool} detects the leading {@code q=} and passes through
 * to Solr without re-encoding.
 */
public class LogQueryExtractor implements DiagTool {

    private static final Logger logger = LogManager.getLogger(LogQueryExtractor.class);

    private static final int DEFAULT_LIMIT = 200;

    // Drop internal subnets, monitoring tools, and bot UAs.
    // The set mirrors getIdListFromApacheLog.sh so the same hosts are
    // excluded for "what users searched" as for "what users clicked".
    private static final Pattern DROP_LINE = Pattern.compile(
        "128\\.223\\.5[67]|nagios|Googlebot|bingbot|Exabot|check_http"
    );

    // Extract a `?q=...` or `&q=...` substring up to the next & or whitespace
    // or quote. Group 1 is the value (without the leading separator/key).
    private static final Pattern Q_PARAM = Pattern.compile("[?&](q=[^& \"]+)");

    @Override public String name()  { return "queries-from-log"; }
    @Override public String usage() { return "queries-from-log [logfile]   (default: /var/log/httpd/zfin_access; LIMIT env: 200)"; }

    @Override
    public void run(DiagContext ctx, String[] args) throws Exception {
        Path log = ctx.resolvePath(args.length >= 1 ? args[0] : "/var/log/httpd/zfin_access");
        if (!Files.isReadable(log)) {
            throw new IllegalStateException("cannot read " + log);
        }
        int limit = Integer.parseInt(System.getenv().getOrDefault("LIMIT", String.valueOf(DEFAULT_LIMIT)));

        // Insertion-ordered map: first occurrence sets position, ties
        // break by first-seen so stable output across reruns where two
        // queries hit the same count.
        Map<String, Long> counts = new LinkedHashMap<>();
        try (var lines = Files.lines(log, StandardCharsets.UTF_8)) {
            lines.filter(line -> !DROP_LINE.matcher(line).find())
                 .forEach(line -> {
                     Matcher m = Q_PARAM.matcher(line);
                     while (m.find()) {
                         String value = m.group(1);
                         if ("q=".equals(value)) continue; // skip empty
                         counts.merge(value, 1L, Long::sum);
                     }
                 });
        }

        List<String> top = counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (args.length >= 2) {
            // Second arg = output file. Common case for the orchestrator;
            // standalone CLI use can redirect stdout instead.
            Path out = ctx.resolvePath(args[1]);
            if (out.getParent() != null) Files.createDirectories(out.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(out)) {
                for (String q : top) {
                    w.write(q);
                    w.newLine();
                }
            }
            logger.info("wrote {} queries to {}", top.size(), out);
        } else {
            PrintStream out = System.out;
            for (String q : top) out.println(q);
            logger.info("wrote {} queries to stdout", top.size());
        }
    }
}
