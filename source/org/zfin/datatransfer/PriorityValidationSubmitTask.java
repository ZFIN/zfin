package org.zfin.datatransfer;

import lombok.extern.log4j.Log4j2;
import org.zfin.alliancegenome.PriorityRESTAllianceService;
import org.zfin.alliancegenome.PriorityRESTAllianceService.CuratorPriorityResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ZFIN-10365: sends ZFIN's curator-validated indexing priorities back to the ABC so the
 * indexing priority classifier can be validated. Every row of the validation report is sent,
 * whether the curator confirmed the predicted priority or corrected it.
 * <p>
 * Usage: gradle submitPriorityValidation --args="&lt;report.tsv&gt; [--apply]"
 * with -Dpriority.baseUrl=https://stage-literature-rest.alliancegenome.org to target stage.
 * Without --apply it only validates the report and prints what it would send.
 * <p>
 * Input columns used: reference_curie, validated_priority (1|2|3), outcome.
 * Writes &lt;report&gt;.submission-result.tsv next to the input.
 */
@Log4j2
public class PriorityValidationSubmitTask {

    // ATP terms for the ABC indexing priority tags, as listed by the production
    // get_priority_tag endpoint's all_priority_tags (ABC stage returns an empty list).
    static final Map<String, String> PRIORITY_TO_ATP = Map.of(
            "1", "ATP:0000211",
            "2", "ATP:0000212",
            "3", "ATP:0000213"
    );

    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);
        boolean apply = argList.contains("--apply");
        List<String> files = argList.stream().filter(a -> !a.startsWith("--")).toList();
        if (files.size() != 1) {
            System.err.println("Usage: PriorityValidationSubmitTask <report.tsv> [--apply]");
            System.exit(1);
        }
        int failures = new PriorityValidationSubmitTask().run(Path.of(files.get(0)), apply);
        System.exit(failures == 0 ? 0 : 2);
    }

    record Row(String referenceCurie, String validatedPriority, String outcome) {
    }

    int run(Path report, boolean apply) throws IOException {
        List<Row> rows = readReport(report);
        log.info("Read " + rows.size() + " rows from " + report);

        PriorityRESTAllianceService service = apply ? new PriorityRESTAllianceService(true) : null;
        String target = apply ? service.getBaseUrl() : System.getProperty("priority.baseUrl", "(default)");
        log.info((apply ? "APPLYING" : "DRY RUN") + " against " + target);

        Path resultFile = report.resolveSibling(report.getFileName() + ".submission-result.tsv");
        Map<String, Integer> statusCounts = new TreeMap<>();
        int failures = 0;
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(resultFile, StandardCharsets.UTF_8))) {
            out.println("reference_curie\tvalidated_priority\tatp_id\toutcome\thttp_status\tresponse");
            int i = 0;
            for (Row row : rows) {
                i++;
                String atp = PRIORITY_TO_ATP.get(row.validatedPriority());
                String status;
                String body;
                if (atp == null) {
                    status = "INVALID_PRIORITY";
                    body = "validated_priority '" + row.validatedPriority() + "' is not 1, 2 or 3";
                } else if (!apply) {
                    status = "DRY_RUN";
                    body = "";
                } else {
                    CuratorPriorityResponse response = service.setCuratorPriority(row.referenceCurie(), atp);
                    status = String.valueOf(response.status());
                    body = response.isSuccess() ? "" : response.body();
                    if (response.status() == 401) {
                        out.println(String.join("\t", row.referenceCurie(), row.validatedPriority(), atp, row.outcome(), status, oneLine(body)));
                        statusCounts.merge(status, 1, Integer::sum);
                        failures++;
                        log.error("401 from ABC (token missing or expired) at row " + i + " of " + rows.size() + "; stopping. " + oneLine(body));
                        break;
                    }
                }
                if (!(status.equals("DRY_RUN") || status.startsWith("2"))) {
                    failures++;
                    log.warn(row.referenceCurie() + " -> " + status + " " + oneLine(body));
                }
                statusCounts.merge(status, 1, Integer::sum);
                out.println(String.join("\t", row.referenceCurie(), row.validatedPriority(), atp == null ? "" : atp, row.outcome(), status, oneLine(body)));
                if (i % 100 == 0) {
                    log.info("Processed " + i + " / " + rows.size());
                }
            }
        }
        log.info("Status counts: " + statusCounts);
        log.info("Failures: " + failures + "; results written to " + resultFile);
        return failures;
    }

    static List<Row> readReport(Path report) throws IOException {
        List<String> lines = Files.readAllLines(report, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IOException("Empty report " + report);
        }
        List<String> header = Arrays.asList(lines.get(0).split("\t", -1));
        int curieIdx = requireColumn(header, "reference_curie");
        int priorityIdx = requireColumn(header, "validated_priority");
        int outcomeIdx = requireColumn(header, "outcome");
        List<Row> rows = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            String[] cols = line.split("\t", -1);
            rows.add(new Row(cols[curieIdx].trim(), cols[priorityIdx].trim(), cols[outcomeIdx].trim()));
        }
        return rows;
    }

    private static int requireColumn(List<String> header, String name) throws IOException {
        int idx = header.indexOf(name);
        if (idx < 0) {
            throw new IOException("Report is missing column '" + name + "'; header: " + header);
        }
        return idx;
    }

    private static String oneLine(String s) {
        return s == null ? "" : s.replaceAll("[\\t\\r\\n]+", " ");
    }
}
