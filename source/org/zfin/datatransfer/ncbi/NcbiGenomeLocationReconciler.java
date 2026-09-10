package org.zfin.datatransfer.ncbi;

import jakarta.persistence.Tuple;
import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportSummaryTable;
import org.zfin.mapping.GenomeLocation;

import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.zfin.datatransfer.ncbi.NCBIDirectPort.FDCONT_NCBI_GENE_ID;
import static org.zfin.datatransfer.ncbi.port.PortHelper.print;
import static org.zfin.framework.HibernateUtil.createTransaction;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.framework.HibernateUtil.flushAndCommitCurrentSession;
import static org.zfin.framework.HibernateUtil.rollbackTransaction;

/**
 * Re-aligns the NCBI genome locations with the gene mappings a load run has just written.
 *
 * <p>Applies the decisions {@link GenomeLocationDrift} defines. Construct one per run and
 * call {@link #reconcile()}; it works inside the caller's Hibernate session and returns the
 * report actions rather than publishing them anywhere itself.
 */
class NcbiGenomeLocationReconciler {

    /** The load's own log; this class only ever appends to it. */
    private final BufferedWriter log;

    NcbiGenomeLocationReconciler(BufferedWriter log) {
        this.log = log;
    }

    /**
     * Re-align the NCBI genome locations with the gene mappings this run just wrote
     * (ZFIN-10461).
     *
     * <p>Rows in sequence_feature_chromosome_location_generated sourced from
     * {@code NCBILoader} pair an NCBI Gene ID ({@code sfclg_acc_num}) with a ZFIN gene
     * ({@code sfclg_data_zdb_id}). Load-NCBI-GFF3-File resolves that pairing once, out of
     * db_link, and caches it. On a later run its upsert keys off the accession alone and
     * only ever compares start/end/chromosome, so it never revisits the gene — a pairing
     * that this load invalidates is never corrected, and a gene page keeps showing
     * coordinates that now belong to a different gene.
     *
     * <p>Kinds of drift, handled differently:
     * <ul>
     *   <li>the accession no longer has any NCBI Gene db_link — the row asserts a location
     *       for a gene NCBI no longer associates with it, so it is deleted;</li>
     *   <li>the accession now maps to exactly one <em>different</em> gene — the coordinates
     *       belong to the accession, so the row is re-pointed at the new gene;</li>
     *   <li>re-pointing collides with a row the target gene already holds at the
     *       <em>same</em> location — the row is a leftover duplicate of a correct row the
     *       GFF3 load already created, so it is deleted; no coordinates are lost and the
     *       wrong location stops being shown on the old gene;</li>
     *   <li>re-pointing collides at a <em>different</em> location, or the accession maps to
     *       several genes — there is no unambiguous correction, so the row is left exactly
     *       as it was and reported for a curator rather than guessed at or dropped.</li>
     * </ul>
     *
     * <p>Scoped to {@code NCBILoader}. The sibling {@code NCBIStartEndLoader} rows are
     * rebuilt from current db_link on every run of that load (loadNCBIStartEnd.sql deletes
     * the whole source before re-inserting), so they self-correct and need no reconciling.
     */
    /** Every NCBILoader location whose (accession, gene) pair db_link no longer carries. */
    private static final String DRIFT_SQL = """
            with drifted as (
              select l.*,
                     (select string_agg(distinct d.dblink_linked_recid, ',' order by d.dblink_linked_recid)
                        from db_link d
                       where d.dblink_acc_num = l.sfclg_acc_num
                         and d.dblink_fdbcont_zdb_id = :fdbcont) as current_genes
                from sequence_feature_chromosome_location_generated l
               where l.sfclg_location_source = :source
                 and not exists (select 1 from db_link d
                                  where d.dblink_acc_num = l.sfclg_acc_num
                                    and d.dblink_fdbcont_zdb_id = :fdbcont
                                    and d.dblink_linked_recid = l.sfclg_data_zdb_id)
            )
            select d.sfclg_pk_id, d.sfclg_data_zdb_id, d.sfclg_acc_num,
                   d.sfclg_chromosome, d.sfclg_start, d.sfclg_end, d.current_genes,
                   -- Would moving this row onto d.current_genes be refused? Mirrors
                   -- uq_sfclg_unique_location, the constraint that binds for these rows:
                   -- every one of its columns, with IS NOT DISTINCT FROM for its
                   -- NULLS NOT DISTINCT semantics. Only meaningful when current_genes names
                   -- exactly one gene; for the orphaned and ambiguous rows it reads false and
                   -- is not consulted.
                   exists (select 1
                             from sequence_feature_chromosome_location_generated t
                            where t.sfclg_data_zdb_id = d.current_genes
                              and t.sfclg_pk_id <> d.sfclg_pk_id
                              and t.sfclg_acc_num           is not distinct from d.sfclg_acc_num
                              and t.sfclg_chromosome        is not distinct from d.sfclg_chromosome
                              and t.sfclg_start             is not distinct from d.sfclg_start
                              and t.sfclg_end               is not distinct from d.sfclg_end
                              and t.sfclg_location_source   is not distinct from d.sfclg_location_source
                              and t.sfclg_location_subsource is not distinct from d.sfclg_location_subsource
                              and t.sfclg_fdb_db_id         is not distinct from d.sfclg_fdb_db_id
                              and t.sfclg_pub_zdb_id        is not distinct from d.sfclg_pub_zdb_id
                              and t.sfclg_assembly          is not distinct from d.sfclg_assembly
                              and t.sfclg_gbrowse_track     is not distinct from d.sfclg_gbrowse_track
                              and t.sfclg_evidence_code     is not distinct from d.sfclg_evidence_code
                              and t.sfclg_strand            is not distinct from d.sfclg_strand) as would_collide
              from drifted d
             order by d.sfclg_data_zdb_id, d.sfclg_acc_num
            """;

    /** Where each reconciled row lands. The report is built from these three buckets. */
    private static final class Outcomes {
        private final List<GenomeLocationDrift.ReportRow> repointed = new ArrayList<>();
        private final List<GenomeLocationDrift.ReportRow> deleted = new ArrayList<>();
        private final List<GenomeLocationDrift.ReportRow> failed = new ArrayList<>();
    }

    List<LoadReportAction> reconcile() {
        String source = GenomeLocation.Source.NCBI_LOADER.getName();
        Outcomes outcomes = new Outcomes();

        try {
            createTransaction();
            List<Tuple> drift = findDriftedLocations(source);
            print(log, "Found " + drift.size() + " " + source + " genome locations out of step with the new gene mappings.\n");
            currentSession().doWork(connection -> applyDecisions(connection, drift, outcomes));
            flushAndCommitCurrentSession();
        } catch (RuntimeException e) {
            rollbackTransaction();
            print(log, "ERROR: Could not reconcile NCBI genome locations: " + e.getMessage() + "\n");
            outcomes.failed.add(new GenomeLocationDrift.ReportRow("N/A", "N/A",
                    "Reconciliation aborted: " + e.getMessage(), "N/A", "N/A"));
        }

        print(log, "Genome location reconciliation: " + outcomes.repointed.size() + " re-pointed, "
                + outcomes.deleted.size() + " deleted, " + outcomes.failed.size() + " could not be reconciled.\n");
        return reportActions(outcomes);
    }

    /** Read the drifted rows, and what db_link maps each accession to now. */
    private List<Tuple> findDriftedLocations(String source) {
        return currentSession().createNativeQuery(DRIFT_SQL, Tuple.class)
                .setParameter("fdbcont", FDCONT_NCBI_GENE_ID)
                .setParameter("source", source)
                .list();
    }

    /**
     * Work through the drifted rows, applying each decision and recording what happened.
     *
     * <p>Raw JDBC with a savepoint per row: the failure we have to survive is a
     * unique-constraint violation, and two overlapping unique constraints cover this table
     * with different NULL semantics. Letting Postgres decide and rolling back just that row is
     * more trustworthy than re-deriving both keys here, and it keeps a violation from
     * poisoning the Hibernate session.
     */
    private void applyDecisions(Connection connection, List<Tuple> drift,
                                Outcomes outcomes) throws SQLException {
        try (PreparedStatement repoint = connection.prepareStatement(
                     "update sequence_feature_chromosome_location_generated set sfclg_data_zdb_id = ? where sfclg_pk_id = ?");
             PreparedStatement remove = connection.prepareStatement(
                     "delete from sequence_feature_chromosome_location_generated where sfclg_pk_id = ?")) {
            for (Tuple tuple : drift) {
                apply(DriftedRow.from(tuple), repoint, remove, outcomes);
            }
        }
    }

    /** One drifted row, in the shape the decision and the report both want. */
    private record DriftedRow(long pkId, String gene, String accession,
                              String currentGenes, String location, boolean wouldCollide) {

        static DriftedRow from(Tuple tuple) {
            return new DriftedRow(
                    ((Number) tuple.get("sfclg_pk_id")).longValue(),
                    tuple.get("sfclg_data_zdb_id", String.class),
                    tuple.get("sfclg_acc_num", String.class),
                    tuple.get("current_genes", String.class),
                    tuple.get("sfclg_chromosome", String.class) + ":"
                            + tuple.get("sfclg_start", Integer.class) + "-"
                            + tuple.get("sfclg_end", Integer.class),
                    Boolean.TRUE.equals(tuple.get("would_collide", Boolean.class)));
        }

        GenomeLocationDrift.ReportRow reported(String outcome, String nowMapsTo) {
            return new GenomeLocationDrift.ReportRow(gene, accession, outcome, nowMapsTo, location);
        }
    }

    /**
     * Decide what becomes of one drifted row, do it, and record it. Every outcome the
     * reconciliation can produce is here, in one switch.
     *
     * <p>Nothing is attempted speculatively: the drift query has already worked out whether a
     * re-point would collide, so the category is known before any write.
     */
    private void apply(DriftedRow row, PreparedStatement repoint, PreparedStatement remove,
                       Outcomes outcomes) throws SQLException {
        switch (GenomeLocationDrift.categorize(row.currentGenes(), row.wouldCollide())) {
            case ORPHANED -> {
                delete(remove, row);
                outcomes.deleted.add(row.reported("Deleted - accession has no NCBI Gene ID link", "(none)"));
            }
            case REMAPPED -> {
                rePoint(repoint, row);
                outcomes.repointed.add(row.reported("Re-pointed to " + row.currentGenes(), row.currentGenes()));
            }
            case REMAPPED_DUPLICATE -> {
                delete(remove, row);
                outcomes.deleted.add(row.reported(
                        "Deleted - duplicate of the identical location already on " + row.currentGenes(),
                        row.currentGenes()));
            }
            case AMBIGUOUS -> outcomes.failed.add(
                    row.reported("Left unchanged - accession maps to several genes", row.currentGenes()));
        }
    }

    private void delete(PreparedStatement remove, DriftedRow row) throws SQLException {
        remove.setLong(1, row.pkId());
        remove.executeUpdate();
    }

    /**
     * Move the row to the gene its accession now belongs to.
     *
     * <p>No savepoint: the drift query predicted this write would be accepted, so a refusal
     * means the prediction is wrong rather than that this row is special. That can only happen
     * if the unique constraints on the table no longer match the predicate in
     * {@link #DRIFT_SQL}, so it aborts the whole reconciliation with that said plainly, rather
     * than carrying on against a model of the schema that has stopped being true.
     */
    private void rePoint(PreparedStatement repoint, DriftedRow row) throws SQLException {
        repoint.setString(1, row.currentGenes());
        repoint.setLong(2, row.pkId());
        try {
            repoint.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Re-pointing genome location " + row.pkId() + " (" + row.accession() + ") to "
                    + row.currentGenes() + " was refused, but the drift query predicted it would"
                    + " be accepted. The unique constraints on"
                    + " sequence_feature_chromosome_location_generated no longer match the"
                    + " would_collide predicate in DRIFT_SQL - it mirrors"
                    + " uq_sfclg_unique_location, so check whether that constraint has changed."
                    + " Aborting rather than reconciling against a stale model of the schema.", e);
        }
    }

    /** Turn the three buckets into the report's UPDATE / DELETE / ERROR actions. */
    private List<LoadReportAction> reportActions(Outcomes outcomes) {
        List<LoadReportAction> actions = new ArrayList<>();
        addGenomeLocationAction(actions, LoadReportAction.Type.UPDATE,
                "Re-pointed NCBI Genome Location", outcomes.repointed,
                "NCBI genome locations moved to the gene their NCBI Gene ID now maps to.");
        addGenomeLocationAction(actions, LoadReportAction.Type.DELETE,
                "Deleted NCBI Genome Location", outcomes.deleted,
                "NCBI genome locations removed: either the NCBI Gene ID no longer links to any ZFIN "
                + "gene, or the row duplicated a location the gene it now maps to already holds.");
        addGenomeLocationAction(actions, LoadReportAction.Type.ERROR,
                "Unreconciled NCBI Genome Location", outcomes.failed,
                "These NCBI genome locations still point at the wrong gene and need a curator. Either "
                + "the accession maps to more than one gene, or the gene it now maps to already holds "
                + "different coordinates for it, so there is no unambiguous correction to make.");
        return actions;
    }

    private void addGenomeLocationAction(List<LoadReportAction> actions, LoadReportAction.Type type,
                                         String subType, List<GenomeLocationDrift.ReportRow> rows,
                                         String description) {
        if (rows.isEmpty()) {
            return;
        }
        LoadReportAction action = new LoadReportAction();
        action.setType(type);
        action.setSubType(subType);
        action.setId(subType);
        action.setGeneZdbID("N/A");
        action.setAccession("N/A");
        action.setDetails(description);
        action.setRelatedEntityFields(Map.of("Report Title", subType));

        LoadReportSummaryTable table = new LoadReportSummaryTable();
        table.setDescription(rows.size() + " row(s) in sequence_feature_chromosome_location_generated");
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Gene ZDB ID", "Gene ZDB ID");
        headers.put("NCBI Gene ID", "NCBI Gene ID");
        headers.put("Outcome", "Outcome");
        headers.put("Accession now maps to", "Accession now maps to");
        headers.put("Location", "Location");
        table.setTableHeadersByMap(headers);
        table.setRows(rows.stream().map(GenomeLocationDrift.ReportRow::toMap).toList());
        action.setTables(List.of(table));
        actions.add(action);
    }

}
