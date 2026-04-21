package org.zfin.datatransfer.go;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generates a categorized error summary from GAF load errors.
 * Takes the error list directly from GafJobData rather than parsing the details file.
 */
public class GafErrorSummary {

    private static final Pattern ID_PATTERN = Pattern.compile("ID\\[(.+?)]");
    private static final Pattern CREATED_BY_PATTERN = Pattern.compile("createdBy='([^']*)'");
    private static final Pattern TERM_PATTERN = Pattern.compile("termName='([^']+)'.*oboID='([^']+)'");

    private final Map<String, Integer> categoryCounts = new LinkedHashMap<>();
    private final Map<String, List<String>> categoryExamples = new LinkedHashMap<>();
    private final Map<String, Integer> geneNotFoundIds = new LinkedHashMap<>();
    private final Map<String, Integer> geneNotFoundSources = new LinkedHashMap<>();
    private Map<String, Integer> parserRejections = new LinkedHashMap<>();

    private static String categorize(String message) {
        if (message.startsWith("Annotations with IEA evidence must")) {
            Matcher m = Pattern.compile("\\[(.+?)]").matcher(message);
            String type = m.find() ? m.group(1) : "unknown";
            return "IEA inference field validation (" + type + ")";
        }
        if (message.startsWith("Go term in column")) {
            return "Obsolete GO term referenced";
        }
        if (message.startsWith("Unable to find genes associated with ID")) {
            return "Gene not found for ID";
        }
        if (message.startsWith("A duplicate entry is being added:")) {
            return "Duplicate annotation entry";
        }
        if (message.startsWith("Can not use term in a \"Do Not Annotate\"")) {
            return "Term in \"Do Not Annotate\" subset";
        }
        if (message.startsWith("Protein binding")) {
            return "Protein binding (GO:0005515) requires IPI evidence";
        }
        if (message.startsWith("No pub found for pmid:")) {
            return "Publication not found for PMID";
        }
        if (message.startsWith("MarkerGoTermEvidence{")) {
            return "Annotation insertion failed";
        }
        // Strip trailing colon if present
        if (message.endsWith(":")) {
            return message.substring(0, message.length() - 1);
        }
        return message;
    }

    private static String classifyId(String entryId) {
        if (entryId.startsWith("URS")) {
            return "RNAcentral";
        }
        if (entryId.startsWith("A0A")) {
            return "TrEMBL (unreviewed)";
        }
        return "Swiss-Prot / other";
    }

    private void addExample(String category, String example) {
        categoryExamples.computeIfAbsent(category, k -> new ArrayList<>());
        if (categoryExamples.get(category).size() < 3) {
            categoryExamples.get(category).add(example);
        }
    }

    /**
     * Set the parser rejection counts (entries filtered before validation).
     */
    public void setParserRejections(Map<String, Integer> rejections) {
        this.parserRejections = rejections;
    }

    /**
     * Build the summary directly from the error list produced by the GAF load.
     */
    public void processErrors(List<GafValidationError> errors) {
        for (GafValidationError error : errors) {
            String fullMessage = error.getMessage();
            if (fullMessage == null || fullMessage.isBlank()) {
                continue;
            }

            // The message format is: "error text:\nGafEntry{...}" or "error text:\nobsolete term detail\nGafEntry{...}"
            String[] lines = fullMessage.split("\n", 2);
            String firstLine = lines[0].strip();
            String detail = lines.length > 1 ? lines[1].strip() : "";

            String category = categorize(firstLine);
            if (category == null) {
                continue;
            }
            categoryCounts.merge(category, 1, Integer::sum);

            // Add examples
            if (!detail.isEmpty()) {
                // For obsolete terms, extract the term name
                Matcher termMatcher = TERM_PATTERN.matcher(detail);
                if (termMatcher.find()) {
                    addExample(category, termMatcher.group(2) + ": " + termMatcher.group(1));
                } else {
                    addExample(category, detail.split("\n")[0]);
                }
            }

            // Track gene-not-found details
            if ("Gene not found for ID".equals(category)) {
                Matcher idMatcher = ID_PATTERN.matcher(firstLine);
                if (idMatcher.find()) {
                    geneNotFoundIds.merge(idMatcher.group(1), 1, Integer::sum);
                }
                Matcher createdByMatcher = CREATED_BY_PATTERN.matcher(detail);
                if (createdByMatcher.find()) {
                    geneNotFoundSources.merge(createdByMatcher.group(1), 1, Integer::sum);
                }
            }
        }
    }

    public String format() {
        int totalErrors = categoryCounts.values().stream().mapToInt(Integer::intValue).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("GAF Load Error Summary\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("Total errors: %,d%n", totalErrors));
        sb.append(String.format("Unique error categories: %d%n", categoryCounts.size()));

        // Parser rejections (entries filtered before validation)
        if (!parserRejections.isEmpty()) {
            int totalRejected = parserRejections.values().stream().mapToInt(Integer::intValue).sum();
            sb.append(String.format("Entries filtered during parsing: %,d%n", totalRejected));
        }
        sb.append("\n");

        if (!parserRejections.isEmpty()) {
            sb.append("Entries Filtered During Parsing\n");
            sb.append("-".repeat(60)).append("\n");
            int totalRejected = 0;
            for (Map.Entry<String, Integer> e : parserRejections.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList())) {
                sb.append(String.format("  %,6d  %s%n", e.getValue(), e.getKey()));
                totalRejected += e.getValue();
            }
            sb.append(String.format("%n  %,6d  TOTAL filtered%n%n", totalRejected));
        }

        List<Map.Entry<String, Integer>> sorted = categoryCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());

        sb.append("Error Counts by Category\n");
        sb.append("-".repeat(60)).append("\n");
        for (Map.Entry<String, Integer> e : sorted) {
            sb.append(String.format("  %,6d  %s%n", e.getValue(), e.getKey()));
        }
        sb.append(String.format("%n  %,6d  TOTAL%n", totalErrors));

        // Gene not found deep dive
        if (!geneNotFoundIds.isEmpty()) {
            int uniqueIds = geneNotFoundIds.size();
            int totalOccurrences = geneNotFoundIds.values().stream().mapToInt(Integer::intValue).sum();

            Map<String, Integer> idTypeUnique = new LinkedHashMap<>();
            for (String id : geneNotFoundIds.keySet()) {
                idTypeUnique.merge(classifyId(id), 1, Integer::sum);
            }

            sb.append("\n\nGene Not Found — Deep Dive\n");
            sb.append("=".repeat(60)).append("\n");
            sb.append(String.format("%,d unique IDs, %,d total occurrences%n", uniqueIds, totalOccurrences));
            sb.append("(Each ID may appear multiple times — once per GO annotation)\n\n");

            sb.append("By ID Type (unique IDs)\n");
            sb.append("-".repeat(60)).append("\n");
            idTypeUnique.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> sb.append(String.format("  %,6d  (%4.1f%%)  %s%n",
                    e.getValue(), e.getValue() * 100.0 / uniqueIds, e.getKey())));

            sb.append("\n  RNAcentral:          URS* IDs — non-coding RNA, not mapped in ZFIN\n");
            sb.append("  TrEMBL (unreviewed): A0A* IDs — computationally predicted proteins\n");
            sb.append("  Swiss-Prot / other:  Older accessions not in ZFIN db_link\n");

            sb.append("\nBy Annotation Source (occurrences)\n");
            sb.append("-".repeat(60)).append("\n");
            geneNotFoundSources.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> sb.append(String.format("  %,6d  (%4.1f%%)  %s%n",
                    e.getValue(), e.getValue() * 100.0 / totalOccurrences, e.getKey())));

            sb.append("\nTop 20 IDs by Occurrence\n");
            sb.append("-".repeat(60)).append("\n");
            geneNotFoundIds.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .forEach(e -> sb.append(String.format("  %,4dx  %s  (%s)%n",
                    e.getValue(), e.getKey(), classifyId(e.getKey()))));
            if (uniqueIds > 20) {
                sb.append(String.format("  ... and %,d more%n", uniqueIds - 20));
            }
        }

        // Examples for other categories
        sb.append("\n\nExamples by Category\n");
        sb.append("=".repeat(60)).append("\n");
        for (Map.Entry<String, Integer> e : sorted) {
            if ("Gene not found for ID".equals(e.getKey())) {
                continue;
            }
            List<String> examples = categoryExamples.getOrDefault(e.getKey(), Collections.emptyList());
            if (!examples.isEmpty()) {
                sb.append(String.format("%n  %s (%,d):%n", e.getKey(), e.getValue()));
                for (String ex : examples) {
                    sb.append("    - ").append(ex).append("\n");
                }
            }
        }

        return sb.toString();
    }

    public void writeToFile(File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(format());
        }
    }
}
