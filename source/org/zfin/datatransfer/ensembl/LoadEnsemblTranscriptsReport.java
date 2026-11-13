package org.zfin.datatransfer.ensembl;

import org.zfin.datatransfer.ensembl.LoadEnsemblTranscriptsTask.Action;
import org.zfin.datatransfer.ensembl.LoadEnsemblTranscriptsTask.ReportLine;
import org.zfin.datatransfer.ensembl.LoadEnsemblTranscriptsTask.XrefLine;
import org.zfin.datatransfer.ensembl.LoadEnsemblTranscriptsTask.XrefOutcome;
import org.zfin.report.Report;
import org.zfin.report.ReportNode;
import org.zfin.report.ReportTable;
import org.zfin.report.ReportWriter;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The Ensembl transcript load's HTML report, built directly as a {@link Report}: a node per kind of outcome, each
 * with a note on what it means and what to do about it, and a table of the transcripts.
 *
 * <p>A load reads 175,000 transcripts, so a table shows at most {@value #MAX_ROWS_PER_TABLE} rows. Counts are always
 * complete, and the CSV reports list every transcript.
 */
class LoadEnsemblTranscriptsReport {

    static final int MAX_ROWS_PER_TABLE = 10_000;

    private static final String SCHEMA_LOADED = "loaded";
    private static final String SCHEMA_TRANSCRIPTS = "transcripts";
    private static final String SCHEMA_XREF = "xref";
    private static final String SCHEMA_COUNTS = "counts";
    private static final String SCHEMA_BIOTYPES = "biotypes";
    private static final String SCHEMA_XREF_COUNTS = "xrefCounts";

    /** What each action means, and what to do about it. */
    private static final Map<Action, String> NOTES = new EnumMap<>(Action.class);

    static {
        NOTES.put(Action.ADDED, "New ZDB-TSCRIPTs: a marker named by the lower-case ENSDART, the gene-produces-transcript "
                + "relationship, the Ensembl_Trans db_link with its length and assembly, and the attribution.");
        NOTES.put(Action.ASSEMBLY_OR_ATTRIBUTION_ADDED, "The transcript was already in ZFIN. It only gained the assembly or "
                + "attribution it lacked.");
        NOTES.put(Action.ALREADY_LOADED, "Already in ZFIN with its assembly and attribution. Nothing was done.");
        NOTES.put(Action.NO_GENE, "No gene in ZFIN carries this transcript's ENSDARG for the assembly, so it has no gene to "
                + "hang from. Run Load-Ensembl-Mapping (it adds the ENSDARG to the gene), then run this load again. "
                + "The xref report shows what Ensembl's own xref file would offer for these.");
        NOTES.put(Action.AMBIGUOUS_GENE, "More than one gene carries this ENSDARG for the assembly, so the gene is not "
                + "certain. A curator has to fix the db_links.");
        NOTES.put(Action.UNMAPPED_BIOTYPE, "The Ensembl biotype has no ZFIN transcript type. Once curators decide one, give it "
                + "in BIOTYPE_OVERRIDES (for example lncRNA=lincRNA) and run again.");
        NOTES.put(Action.NAME_TAKEN, "A marker already has this transcript's name (the lower-case ENSDART) but is not its "
                + "transcript. A curator has to resolve the clash.");
        NOTES.put(Action.REJECTED, "A FASTA header line could not be used (no ENSDART, assembly, gene or biotype, or an "
                + "empty sequence).");
        NOTES.put(Action.NOT_IN_FILE, "In ZFIN on this assembly, but not in the file any more: Ensembl has probably retired it. "
                + "The load never withdraws or deletes a transcript, so a curator decides what to do with these. Only "
                + "meaningful when the file is the complete set for the assembly.");
    }

    private static final Map<XrefOutcome, String> XREF_NOTES = new EnumMap<>(XrefOutcome.class);

    static {
        XREF_NOTES.put(XrefOutcome.WOULD_ADD, "Ensembl names one ZFIN gene for the ENSDARG, the gene exists and has no other "
                + "ENSDARG for the assembly. Adding the ENSDARG to the gene would let the transcript load.");
        XREF_NOTES.put(XrefOutcome.WOULD_ADD_BIOTYPE_UNMAPPED, "As above, but the biotype has no ZFIN transcript type, so "
                + "the transcript would still be skipped.");
        XREF_NOTES.put(XrefOutcome.GENE_HAS_OTHER_ENSDARG, "The gene Ensembl names already carries a different ENSDARG for "
                + "the assembly.");
        XREF_NOTES.put(XrefOutcome.GENE_NOT_IN_ZFIN, "The ZDB ID Ensembl gives is not a gene in ZFIN.");
        XREF_NOTES.put(XrefOutcome.AMBIGUOUS_XREF, "Ensembl gives the ENSDARG to more than one ZFIN gene.");
        XREF_NOTES.put(XrefOutcome.NO_XREF, "The xref file has no ZFIN gene for this ENSDARG.");
    }

    void write(File output, boolean dryRun, String pubId, Map<String, String> biotypeOverrides, String files,
               List<ReportLine> report, List<XrefLine> xrefReport, Map<String, Integer> unmappedBiotypes) throws IOException {
        Report html = new Report()
                .meta(new Report.Meta()
                        .title("Ensembl Transcript Load" + (dryRun ? " (dry run)" : ""))
                        .createdAt(System.currentTimeMillis())
                        .schemaVersion("1"))
                .definitions(definitions());

        ReportNode root = new ReportNode()
                .id("_root")
                .title("Ensembl Transcript Load")
                .body(Report.Body.text((dryRun ? "DRY RUN: nothing was written to the database.\n" : "Loaded into the database.\n")
                        + "Attributed to " + pubId + ". Biotype overrides: "
                        + (biotypeOverrides.isEmpty() ? "(none)" : biotypeOverrides) + ".\n\nFiles read:\n" + files
                        + "\nTables show at most " + MAX_ROWS_PER_TABLE + " rows; the CSV reports have every transcript."));
        root.addTable(countsTable(report));
        if (!unmappedBiotypes.isEmpty()) {
            root.addTable(biotypesTable(unmappedBiotypes));
        }
        if (!xrefReport.isEmpty()) {
            root.addTable(xrefCountsTable(xrefReport));
        }

        for (Action action : Action.values()) {
            List<ReportLine> lines = report.stream().filter(l -> l.action() == action).toList();
            if (!lines.isEmpty()) {
                root.addChild(actionNode(action, lines));
            }
        }
        if (!xrefReport.isEmpty()) {
            root.addChild(xrefNode(xrefReport));
        }
        html.root(root);
        new ReportWriter().write(html, output);
    }

    private static String id(Action action) {
        return action.name().toLowerCase().replace('_', '-');
    }

    private ReportNode actionNode(Action action, List<ReportLine> lines) {
        boolean loaded = action == Action.ADDED || action == Action.ASSEMBLY_OR_ATTRIBUTION_ADDED || action == Action.ALREADY_LOADED;
        ReportTable table = new ReportTable()
                .schemaRef(loaded ? SCHEMA_LOADED : SCHEMA_TRANSCRIPTS)
                .title(tableTitle(action.name(), lines.size()));
        for (ReportLine line : lines.subList(0, Math.min(lines.size(), MAX_ROWS_PER_TABLE))) {
            // an ambiguous ENSDARG has several genes; they go in the detail so the gene column stays a single link
            boolean oneGene = !line.gene().contains(" ");
            if (loaded) {
                table.addRow("ensdart", line.ensdart(), "ensdarg", line.ensdarg(), "gene", line.gene(),
                        "assembly", line.assembly(), "transcript", line.detail());
            } else {
                table.addRow("ensdart", line.ensdart(), "ensdarg", line.ensdarg(), "gene", oneGene ? line.gene() : "",
                        "assembly", line.assembly(),
                        "detail", oneGene ? line.detail() : line.detail() + ": " + line.gene());
            }
        }
        return new ReportNode()
                .id(id(action))
                .title(action.name())
                .count(lines.size())
                .body(Report.Body.text(NOTES.get(action)))
                .addTable(table);
    }

    private ReportNode xrefNode(List<XrefLine> xrefReport) {
        ReportNode node = new ReportNode()
                .id("xref")
                .title("Xref report")
                .count(xrefReport.size())
                .body(Report.Body.text("Nothing here was loaded. For transcripts left out because no gene carries their "
                        + "ENSDARG, what the xref file Ensembl publishes beside the FASTA says about the ZFIN gene. "
                        + "Using it would also mean adding the ENSDARG to the gene, as Load-Ensembl-Mapping does."));
        for (XrefOutcome outcome : XrefOutcome.values()) {
            List<XrefLine> lines = xrefReport.stream().filter(l -> l.outcome() == outcome).toList();
            if (lines.isEmpty()) {
                continue;
            }
            ReportTable table = new ReportTable()
                    .schemaRef(SCHEMA_XREF)
                    .title(tableTitle(outcome.name(), lines.size()));
            for (XrefLine line : lines.subList(0, Math.min(lines.size(), MAX_ROWS_PER_TABLE))) {
                boolean oneGene = !line.gene().contains(" ");
                table.addRow("ensdart", line.ensdart(), "ensdarg", line.ensdarg(), "biotype", line.biotype(),
                        "assembly", line.assembly(), "gene", oneGene ? line.gene() : "",
                        "symbol", line.symbol(), "detail", oneGene ? line.detail() : line.detail() + ": " + line.gene());
            }
            node.addChild(new ReportNode()
                    .id("xref-" + outcome.name().toLowerCase().replace('_', '-'))
                    .title(outcome.name())
                    .count(lines.size())
                    .body(Report.Body.text(XREF_NOTES.get(outcome)))
                    .addTable(table));
        }
        return node;
    }

    private ReportTable countsTable(List<ReportLine> report) {
        Map<String, Map<Action, Integer>> counts = new TreeMap<>();
        for (ReportLine line : report) {
            counts.computeIfAbsent(line.assembly(), k -> new LinkedHashMap<>()).merge(line.action(), 1, Integer::sum);
        }
        ReportTable table = new ReportTable().schemaRef(SCHEMA_COUNTS).title("Transcripts by assembly and action");
        counts.forEach((assembly, byAction) -> {
            for (Action action : Action.values()) {
                if (byAction.containsKey(action)) {
                    table.addRow("assembly", assembly, "action", action.name(), "count", byAction.get(action));
                }
            }
        });
        return table;
    }

    private ReportTable biotypesTable(Map<String, Integer> unmappedBiotypes) {
        ReportTable table = new ReportTable().schemaRef(SCHEMA_BIOTYPES)
                .title("Biotypes with no ZFIN transcript type, skipped (set BIOTYPE_OVERRIDES to load them)");
        unmappedBiotypes.forEach((key, n) -> {
            int space = key.indexOf(' ');
            table.addRow("assembly", key.substring(0, space), "biotype", key.substring(space + 1), "count", n);
        });
        return table;
    }

    private ReportTable xrefCountsTable(List<XrefLine> xrefReport) {
        Map<String, Map<XrefOutcome, Integer>> byAssembly = new TreeMap<>();
        Map<String, Set<String>> genes = new TreeMap<>();
        for (XrefLine line : xrefReport) {
            byAssembly.computeIfAbsent(line.assembly(), k -> new LinkedHashMap<>()).merge(line.outcome(), 1, Integer::sum);
            if (line.outcome() == XrefOutcome.WOULD_ADD) {
                genes.computeIfAbsent(line.assembly(), k -> new TreeSet<>()).add(line.gene());
            }
        }
        ReportTable table = new ReportTable().schemaRef(SCHEMA_XREF_COUNTS).title("Xref report, by assembly and outcome");
        byAssembly.forEach((assembly, outcomes) -> {
            for (XrefOutcome outcome : XrefOutcome.values()) {
                if (outcomes.containsKey(outcome)) {
                    table.addRow("assembly", assembly, "outcome", outcome.name(), "count", outcomes.get(outcome),
                            "genes", outcome == XrefOutcome.WOULD_ADD ? genes.getOrDefault(assembly, Set.of()).size() : "");
                }
            }
        });
        return table;
    }

    private static String tableTitle(String name, int total) {
        return total > MAX_ROWS_PER_TABLE
                ? name + " (first " + MAX_ROWS_PER_TABLE + " of " + total + ")"
                : name + " (" + total + ")";
    }

    private Report.Definitions definitions() {
        return new Report.Definitions()
                .field("ensdart", Report.FieldDef.link("ENSDART", "https://www.ensembl.org/id/{value}"))
                .field("zdbID", Report.FieldDef.link("ZDB ID", "https://zfin.org/{value}"))
                .tableSchema(SCHEMA_LOADED, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("ensdart", "ENSDART", "ensdart"))
                        .addColumn(ReportTable.Column.of("ensdarg", "ENSDARG"))
                        .addColumn(ReportTable.Column.of("gene", "Gene", "zdbID"))
                        .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                        .addColumn(ReportTable.Column.of("transcript", "Transcript", "zdbID")))
                .tableSchema(SCHEMA_TRANSCRIPTS, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("ensdart", "ENSDART", "ensdart"))
                        .addColumn(ReportTable.Column.of("ensdarg", "ENSDARG"))
                        .addColumn(ReportTable.Column.of("gene", "Gene", "zdbID"))
                        .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                        .addColumn(ReportTable.Column.of("detail", "Detail")))
                .tableSchema(SCHEMA_XREF, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("ensdart", "ENSDART", "ensdart"))
                        .addColumn(ReportTable.Column.of("ensdarg", "ENSDARG"))
                        .addColumn(ReportTable.Column.of("biotype", "Biotype"))
                        .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                        .addColumn(ReportTable.Column.of("gene", "ZFIN gene (xref)", "zdbID"))
                        .addColumn(ReportTable.Column.of("symbol", "Symbol"))
                        .addColumn(ReportTable.Column.of("detail", "Detail")))
                .tableSchema(SCHEMA_COUNTS, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                        .addColumn(ReportTable.Column.of("action", "Action"))
                        .addColumn(ReportTable.Column.of("count", "Count")))
                .tableSchema(SCHEMA_BIOTYPES, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                        .addColumn(ReportTable.Column.of("biotype", "Biotype"))
                        .addColumn(ReportTable.Column.of("count", "Count")))
                .tableSchema(SCHEMA_XREF_COUNTS, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("assembly", "Assembly"))
                        .addColumn(ReportTable.Column.of("outcome", "Outcome"))
                        .addColumn(ReportTable.Column.of("count", "Transcripts"))
                        .addColumn(ReportTable.Column.of("genes", "Genes that would get the ENSDARG")));
    }
}
