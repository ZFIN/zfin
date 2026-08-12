package org.zfin.uniprot;

import org.zfin.report.Report;
import org.zfin.report.ReportNode;
import org.zfin.report.ReportTable;
import org.zfin.uniprot.diff.UniProtDiffSetSummary;
import org.zfin.uniprot.dto.RichSequenceDTO;
import org.zfin.uniprot.dto.RichSequenceDiffDTO;
import org.zfin.uniprot.dto.UniProtCrossReferenceDTO;
import org.zfin.uniprot.dto.UniProtDiffSetDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a standard {@link Report} from a UniProt compare diff
 * ({@link UniProtDiffSetDTO}).
 *
 * <p>Replaces the former bespoke report, which spliced the raw diff JSON into a
 * hand-written HTML viewer under {@code home/uniprot} via a {@code JSON_GOES_HERE}
 * string replace. The report now renders through
 * {@link org.zfin.report.ReportWriter} like the other ZFIN load reports, so no
 * template needs to live outside the classpath.
 */
public class UniProtDiffReportBuilder {

    private static final String SCHEMA_SUMMARY = "summary";
    private static final String SCHEMA_ACCESSIONS = "accessions";
    private static final String SCHEMA_CHANGED = "changed";

    public Report build(String title, UniProtDiffSetDTO diff) {
        Report report = new Report()
                .meta(new Report.Meta()
                        .title(title)
                        .createdAt(System.currentTimeMillis())
                        .schemaVersion("1"))
                .definitions(defs());

        ReportNode root = new ReportNode()
                .id("_root")
                .title(title)
                .body(Report.Body.text(rootText(diff.summary)));

        root.addChild(summaryNode(diff.summary));
        root.addChild(accessionsNode("added", "Added sequences", diff.addedSequences));
        root.addChild(accessionsNode("removed", "Removed sequences", diff.removedSequences));
        root.addChild(changedNode(diff.changedSequences));

        return report.root(root);
    }

    private String rootText(UniProtDiffSetSummary s) {
        if (s == null) {
            return "UniProt comparison report.";
        }
        return "UniProt comparison of two load files: " + s.added() + " added, "
                + s.removed() + " removed, " + s.changed() + " changed (of " + s.total()
                + " compared).";
    }

    private ReportNode summaryNode(UniProtDiffSetSummary s) {
        ReportNode node = new ReportNode().id("summary").title("Summary");
        ReportTable table = new ReportTable().schemaRef(SCHEMA_SUMMARY).title("Counts");
        if (s != null) {
            addMetric(table, "Added", s.added());
            addMetric(table, "Removed", s.removed());
            addMetric(table, "Changed", s.changed());
            addMetric(table, "Total compared", s.total());
            addMetric(table, "Changed RefSeq", s.changedRefSeq());
            addMetric(table, "Changed ZFIN", s.changedZFIN());
            addMetric(table, "Changed GeneID", s.changedGeneID());
            addMetric(table, "Changed InterPro", s.changedInterPro());
            addMetric(table, "Changed EC", s.changedEC());
            addMetric(table, "Changed Pfam", s.changedPfam());
            addMetric(table, "Changed PROSITE", s.changedPROSITE());
            addMetric(table, "Latest update in set 1", s.latestUpdateFromSequence1());
            addMetric(table, "Latest update in set 2", s.latestUpdateFromSequence2());
        }
        return node.addTable(table);
    }

    private void addMetric(ReportTable table, String metric, Object value) {
        table.addRow("metric", metric, "value", value);
    }

    private ReportNode accessionsNode(String id, String title, List<RichSequenceDTO> seqs) {
        ReportNode node = new ReportNode().id(id).title(title).count(seqs == null ? 0 : seqs.size());
        ReportTable table = new ReportTable().schemaRef(SCHEMA_ACCESSIONS).title(title);
        if (seqs != null) {
            for (RichSequenceDTO seq : seqs) {
                table.addRow("accession", seq.accession);
            }
        }
        return node.addTable(table);
    }

    private ReportNode changedNode(List<RichSequenceDiffDTO> changed) {
        ReportNode node = new ReportNode().id("changed").title("Changed sequences")
                .count(changed == null ? 0 : changed.size());
        ReportTable table = new ReportTable().schemaRef(SCHEMA_CHANGED).title("Changed sequences");
        if (changed != null) {
            for (RichSequenceDiffDTO diff : changed) {
                table.addRow(
                        "accession", diff.accession,
                        "addedXrefs", joinXrefs(diff.addedCrossRefs),
                        "removedXrefs", joinXrefs(diff.removedCrossRefs),
                        "addedKeywords", join(diff.addedKeywords),
                        "removedKeywords", join(diff.removedKeywords));
            }
        }
        return node.addTable(table);
    }

    private String joinXrefs(List<UniProtCrossReferenceDTO> xrefs) {
        if (xrefs == null || xrefs.isEmpty()) {
            return "";
        }
        return xrefs.stream()
                .map(x -> x.dbName + ":" + x.accession)
                .collect(Collectors.joining(", "));
    }

    private String join(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return String.join(", ", items);
    }

    private Report.Definitions defs() {
        return new Report.Definitions()
                .tableSchema(SCHEMA_SUMMARY, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("metric", "Metric"))
                        .addColumn(ReportTable.Column.of("value", "Value")))
                .tableSchema(SCHEMA_ACCESSIONS, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("accession", "Accession")))
                .tableSchema(SCHEMA_CHANGED, new Report.TableSchema()
                        .addColumn(ReportTable.Column.of("accession", "Accession"))
                        .addColumn(ReportTable.Column.of("addedXrefs", "Added cross-refs"))
                        .addColumn(ReportTable.Column.of("removedXrefs", "Removed cross-refs"))
                        .addColumn(ReportTable.Column.of("addedKeywords", "Added keywords"))
                        .addColumn(ReportTable.Column.of("removedKeywords", "Removed keywords")));
    }
}
