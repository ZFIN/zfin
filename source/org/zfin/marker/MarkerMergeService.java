package org.zfin.marker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.framework.HibernateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Merges one marker (the "delete" record) into another (the "merge into" record): it reassigns every
 * foreign-key reference and applies the marker-specific business rules, then deletes the old record.
 * This is the single, verified merge implementation shared by the command-line tool
 * ({@link MergeMarkersCommandLine}, i.e. {@code zfin-util merge-markers}) and the webapp
 * ({@code MergeMarkerController}); it is a faithful port of the legacy {@code cgi-bin/merge_markers.pl},
 * proven byte-for-byte equivalent to the Perl by a differential harness. The Perl and that harness
 * have since been removed from the repo and archived to the retirement ticket (the
 * {@code merge-comparison-utility} tarball) now that this Java service is the sole implementation.
 * <p>
 * The heavy lifting is a <em>generic</em> recursive walk of PostgreSQL's {@code information_schema}:
 * it discovers every table with a foreign key onto the record being deleted and generates
 * {@code UPDATE}/{@code DELETE} statements (handling unique- and primary-key-constraint conflicts by
 * deleting the losing row and recursing). On top of that it ports the explicit business rules:
 * genotype/fish display-name renaming, antibody isotype/host merging, inference-group-member
 * reassignment (FB&nbsp;11133), unspecified-allele renaming (FB&nbsp;10333), public-note
 * concatenation (MRDL-121), STR fish/relationship de-duplication, GO root-term cleanup
 * (FB&nbsp;11048), duplicate Alliance link removal (ZFIN-6073), {@code regen_genox_marker}, and the
 * {@code data_alias} / {@code marker_history} / {@code zdb_replaced_data} bookkeeping.
 * <p>
 * <b>Transaction ownership:</b> {@link #merge()} runs against the current Hibernate session's JDBC
 * connection ({@code HibernateUtil.currentSession().doWork(...)}) but does <em>not</em> begin, commit,
 * or roll back the transaction. The caller owns that boundary (the CLI via {@code ToolBootstrap}; the
 * controller via {@code HibernateUtil.createTransaction()}/{@code flushAndCommitCurrentSession()}).
 * One instance performs one merge (it holds per-merge state), so create a fresh instance each time.
 */
public class MarkerMergeService {

    private static final Logger LOG = LogManager.getLogger(MarkerMergeService.class);

    private static final Pattern ZDB_ID = Pattern.compile("^ZDB-([A-Z]+)-\\d{6}-\\d+$");

    // GO ontology root terms (FB 11048).
    private static final String ROOT_BIOLOGICAL_PROCESS = "ZDB-TERM-091209-6070";
    private static final String ROOT_MOLECULAR_FUNCTION = "ZDB-TERM-091209-2432";
    private static final String ROOT_CELLULAR_COMPONENT = "ZDB-TERM-091209-4029";

    private static final String ALLIANCE_FDBCONT = "ZDB-FDBCONT-171018-1";

    /** Sentinel the Perl uses for "no comment"; only a literal "none" suppresses the note update. */
    private static final String NONE = "none";

    private final String deleted;
    private final String mergedInto;
    private final boolean skipRegen;

    private String type1;
    private String deletedAbbrev;
    private String mergedAbbrev;
    private String mergedName;
    private String daliasId;
    private String nomenId;

    /** SQL string -> ordering value (FK-walk depth, or the record_attribution cleanup order). */
    private final Map<String, Integer> mergeSQLs = new HashMap<>();
    /** Guard so the recursive walk visits each (childTable, fkColumn) only once. */
    private final Set<String> processed = new HashSet<>();
    /** Every statement executed, for the dry-run / audit log. */
    private final List<String> executedSql = new ArrayList<>();

    /**
     * @param deleted    ZDB id of the marker to delete (its references move to {@code mergedInto})
     * @param mergedInto ZDB id of the surviving marker
     * @param skipRegen  if true, skip the {@code regen_genox_marker} denorm recompute (used by bulk
     *                   merges that regenerate once at the end, and by the equivalence harness; see
     *                   the class doc and README). Interactive single merges should pass {@code false}.
     */
    public MarkerMergeService(String deleted, String mergedInto, boolean skipRegen) {
        this.deleted = deleted;
        this.mergedInto = mergedInto;
        this.skipRegen = skipRegen;
    }

    /**
     * Validate the ids and perform the merge on the current session's connection (no commit/rollback).
     *
     * @return the ordered list of SQL statements executed (useful for audit/dry-run logging)
     * @throws IllegalArgumentException if the ids are malformed, of different types, equal, or not
     *                                  found / not markers
     * @throws RuntimeException         wrapping any SQL failure (the caller should roll back)
     */
    public List<String> merge() {
        type1 = validateZdbIdFormat(deleted, "record to be deleted");
        String type2 = validateZdbIdFormat(mergedInto, "record to be merged into");
        if (!type1.equals(type2)) {
            throw new IllegalArgumentException(
                    "Cannot merge markers of different types: " + deleted + " (" + type1 + ") vs "
                            + mergedInto + " (" + type2 + ")");
        }
        if (deleted.equals(mergedInto)) {
            throw new IllegalArgumentException("Cannot merge a marker into itself: " + deleted);
        }

        LOG.info("Merging {} into {}", deleted, mergedInto);
        try {
            HibernateUtil.currentSession().doWork(this::doMerge);
        } catch (RuntimeException e) {
            throw new RuntimeException("Merge of " + deleted + " into " + mergedInto + " failed: "
                    + e.getMessage(), e);
        }
        LOG.info("Merge of {} into {} produced {} statements", deleted, mergedInto, executedSql.size());
        return executedSql;
    }

    /** The statements executed by the most recent {@link #merge()} call (for audit/dry-run logging). */
    public List<String> getExecutedSql() {
        return executedSql;
    }

    private static String validateZdbIdFormat(String zdbId, String label) {
        if (zdbId == null) {
            throw new IllegalArgumentException("Missing ZDB ID for " + label);
        }
        Matcher m = ZDB_ID.matcher(zdbId);
        if (!m.matches()) {
            throw new IllegalArgumentException("Not a valid ZDB ID for the " + label + ": " + zdbId);
        }
        return m.group(1);
    }

    private void doMerge(Connection conn) throws SQLException {
        assertExists(conn, deleted);
        assertExists(conn, mergedInto);

        // 1/2. Marker abbreviations + names, and the new DALIAS / NOMEN ids.
        loadMarkerInfo(conn);

        // 3. Genotype / fish display-name renaming (genes etc.; not antibodies).
        if (!"ATB".equals(type1)) {
            renameGenotypeDisplayNames(conn);
            renameFishNames(conn);
        }

        // 4. Seed the "already processed" guard, matching the Perl.
        processed.add("zdb_active_data" + "zactvd_zdb_id");
        processed.add("updates" + "rec_id");
        processed.add("zdb_replaced_data" + "zrepld_old_zdb_id");
        processed.add("zdb_replaced_data" + "zrepld_new_zdb_id");
        processed.add("record_attribution" + "recattrib_data_zdb_id");
        processed.add("record_attribution" + "recattrib_source_zdb_id");

        // 5. Pre-empt record_attribution unique-constraint conflicts (highest priority).
        cleanupRecordAttributionTable(conn, 999);

        // 6. Generic FK-graph walk: build all the UPDATE/DELETE statements.
        recursivelyGetSQLs(conn, deleted, mergedInto, "zdb_active_data", "zactvd_zdb_id", 0);

        // 7. Execute deepest-first; at equal depth DELETE sorts before UPDATE alphabetically.
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(mergeSQLs.entrySet());
        sorted.sort(Comparator
                .comparingInt((Map.Entry<String, Integer> e) -> e.getValue()).reversed()
                .thenComparing(Map.Entry::getKey));
        for (Map.Entry<String, Integer> entry : sorted) {
            exec(conn, entry.getKey());
        }

        // 8/9. Updating tables can trigger fresh record_attribution rows; clean up again (order 0).
        cleanupRecordAttributionTable(conn, 0);
        for (Map.Entry<String, Integer> entry : mergeSQLs.entrySet()) {
            if (entry.getValue() == 0) {
                exec(conn, entry.getKey());
            }
        }

        // 10. Reassign remaining record_attribution rows.
        execUpdate(conn,
                "update record_attribution set recattrib_data_zdb_id = ? where recattrib_data_zdb_id = ?",
                mergedInto, deleted);

        // 11. data_alias: add the deleted marker's symbol as an alias of the surviving marker.
        createAliasIfAbsent(conn);

        // 12. marker_history: record the merge event.
        createMarkerHistory(conn);

        // 13/14. Type-specific business rules.
        if ("ATB".equals(type1)) {
            mergeAntibodyFields(conn);
        } else {
            mergeInferenceGroupMembers(conn);   // FB 11133
            renameUnspecifiedAllele(conn);       // FB 10333
        }

        // 15. Public notes (MRDL-121).
        mergePublicNotes(conn);

        // 16. STR fish / marker_relationship de-duplication.
        if (deleted.contains("MRPHLNO") || deleted.contains("CRISP") || deleted.contains("TALEN")) {
            deleteConflictingStrUsages(conn);
        }

        // 17. GO root-term cleanup (FB 11048).
        cleanupRedundantRootGoTerms(conn);

        // 18. Duplicate Alliance link (ZFIN-6073).
        execUpdate(conn,
                "delete from zdb_active_data where exists("
                        + "select 1 from db_link where dblink_zdb_id = zactvd_zdb_id "
                        + "and dblink_linked_recid = ? and dblink_acc_num = ? "
                        + "and dblink_fdbcont_zdb_id = ?)",
                mergedInto, deleted, ALLIANCE_FDBCONT);

        // 19. Regenerate genotype/expression denormalizations for the surviving marker.
        // This recompute rebuilds derived fast-search tables; it is skipped when --skip-regen is given
        // (e.g. bulk merges that regenerate once at the end, or the Perl-vs-Java equivalence harness,
        // where regen_genox_marker's minute-granular temp-table name collides when many merges run
        // within the same minute).
        if (skipRegen) {
            LOG.info("skipping regen_genox_marker('{}') (--skip-regen)", mergedInto);
        } else {
            execUpdate(conn, "select regen_genox_marker(?)", mergedInto);
        }

        // 20-23. zdb_replaced_data bookkeeping + delete the old record.
        execUpdate(conn, "delete from zdb_replaced_data where zrepld_old_zdb_id = ?", deleted);
        execUpdate(conn, "update zdb_replaced_data set zrepld_new_zdb_id = ? where zrepld_new_zdb_id = ?",
                mergedInto, deleted);
        execUpdate(conn, "delete from zdb_active_data where zactvd_zdb_id = ?", deleted);
        execUpdate(conn,
                "insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values (?, ?)",
                deleted, mergedInto);
    }

    private void assertExists(Connection conn, String zdbId) throws SQLException {
        if (countRows(conn, "zdb_active_data", "zactvd_zdb_id", zdbId) == 0) {
            throw new IllegalArgumentException(zdbId + " is not found at ZFIN");
        }
    }

    private void loadMarkerInfo(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select mrkr_abbrev, get_id('DALIAS') from marker where mrkr_zdb_id = ?")) {
            ps.setString(1, deleted);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    deletedAbbrev = rs.getString(1);
                    daliasId = rs.getString(2);
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "select mrkr_abbrev, mrkr_name, get_id('NOMEN') from marker where mrkr_zdb_id = ?")) {
            ps.setString(1, mergedInto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mergedAbbrev = rs.getString(1);
                    mergedName = rs.getString(2);
                    nomenId = rs.getString(3);
                }
            }
        }
        if (deletedAbbrev == null || mergedAbbrev == null) {
            throw new IllegalArgumentException(
                    "Both ZDB IDs must be markers (found in the marker table): " + deleted + ", " + mergedInto);
        }
    }

    // ZFIN-6309: rename genotype display names that embed the deleted marker's symbol.
    private void renameGenotypeDisplayNames(Connection conn) throws SQLException {
        Map<String, String> genotypes = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select geno_zdb_id, geno_display_name from feature_marker_relationship, genotype_feature, genotype "
                        + "where fmrel_mrkr_zdb_id = ? and fmrel_ftr_zdb_id = genofeat_feature_zdb_id "
                        + "and genofeat_geno_zdb_id = geno_zdb_id")) {
            ps.setString(1, deleted);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    genotypes.put(rs.getString(1), rs.getString(2));
                }
            }
        }
        for (Map.Entry<String, String> e : genotypes.entrySet()) {
            String displayName = e.getValue();
            if (displayName != null && displayName.contains(deletedAbbrev)) {
                String newName = displayName.replace(deletedAbbrev, mergedAbbrev);
                execUpdate(conn, "update genotype set geno_display_name = ? where geno_zdb_id = ?",
                        newName, e.getKey());
            }
        }
    }

    private void renameFishNames(Connection conn) throws SQLException {
        Map<String, String> fish = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select fish_zdb_id, fish_name from feature_marker_relationship, genotype_feature, fish "
                        + "where fmrel_mrkr_zdb_id = ? and fmrel_ftr_zdb_id = genofeat_feature_zdb_id "
                        + "and genofeat_geno_zdb_id = fish_genotype_zdb_id")) {
            ps.setString(1, deleted);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fish.put(rs.getString(1), rs.getString(2));
                }
            }
        }
        for (Map.Entry<String, String> e : fish.entrySet()) {
            String fishName = e.getValue();
            if (fishName != null && fishName.contains(deletedAbbrev)) {
                String newName = fishName.replace(deletedAbbrev, mergedAbbrev);
                execUpdate(conn, "update fish set fish_name = ? where fish_zdb_id = ?",
                        newName, e.getKey());
            }
        }
    }

    /**
     * Generic FK-graph walk (port of the Perl {@code recursivelyGetSQLs}). Discovers every child
     * table referencing {@code parentTable.foreignKey} and records the UPDATE/DELETE needed to move
     * references from {@code toBeDeleted} to {@code toBeMergedInto}, recursing into composite-key /
     * unique-constraint conflicts so the losing rows are removed first.
     */
    private void recursivelyGetSQLs(Connection conn, String toBeDeleted, String toBeMergedInto,
                                    String parentTable, String foreignKey, int depth) throws SQLException {
        depth++;

        List<String[]> children = new ArrayList<>(); // {childTable, fkColumn, childSchema}
        try (PreparedStatement ps = conn.prepareStatement(
                "select distinct c.table_name, k1.column_name, k2.table_name, k2.column_name, "
                        + "k1.table_schema, k2.table_schema "
                        + "from information_schema.constraint_table_usage c, "
                        + "information_schema.key_column_usage k1, information_schema.table_constraints tc1, "
                        + "information_schema.key_column_usage k2, information_schema.table_constraints tc2 "
                        + "where k1.column_name = ? and k1.table_name = ? "
                        + "and tc1.table_name = k1.table_name and tc1.constraint_name = k1.constraint_name "
                        + "and c.table_name = k1.table_name and c.constraint_name = k2.constraint_name "
                        + "and c.table_name != k2.table_name "
                        + "and tc2.table_name = k2.table_name and tc2.constraint_name = k2.constraint_name "
                        + "and tc2.constraint_type = 'FOREIGN KEY' order by 1, 3, 4")) {
            ps.setString(1, foreignKey);
            ps.setString(2, parentTable);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    children.add(new String[]{rs.getString(3), rs.getString(4), rs.getString(6)});
                }
            }
        }

        for (String[] child : children) {
            String childTable = child[0];
            String fkColumn = child[1];
            String childSchema = child[2];
            if (processed.contains(childTable + fkColumn)) {
                continue;
            }
            String schemaTable = childSchema + "." + childTable;

            int rowsForDeleted = countRows(conn, schemaTable, fkColumn, toBeDeleted);
            if (rowsForDeleted > 0 && !"record_attribution".equals(childTable)) {
                // Non-FK columns of any UNIQUE constraint, and whether the FK is part of one.
                List<String> uniqueCols = new ArrayList<>();
                boolean fkInUnique = false;
                int numUniqueCols = 0;
                for (String col : constraintColumns(conn, childTable, "UNIQUE")) {
                    if (col.equals(fkColumn)) {
                        fkInUnique = true;
                    } else {
                        uniqueCols.add(col);
                    }
                    numUniqueCols++;
                }

                // Primary-key columns; track whether the FK is (part of) the PK.
                List<String> pkCols = new ArrayList<>();
                String soloPk = null;
                String pkOfChild = null;
                boolean fkInPk = false;
                int numPkCols = 0;
                for (String col : constraintColumns(conn, childTable, "PRIMARY KEY")) {
                    if (col.equals(fkColumn)) {
                        fkInPk = true;
                        soloPk = col;
                    } else {
                        pkCols.add(col);
                        pkOfChild = col;
                    }
                    numPkCols++;
                }

                // Scenario 1: FK is part of a multi-column UNIQUE constraint and the PK is a single
                // (non-FK) column. Delete the conflicting "deleted" rows and recurse on the PK.
                // The Perl only handles unique constraints of exactly 2, 3, or 4 columns (explicit
                // if/elsif branches); for any other column count it does NO conflict resolution and
                // just emits the plain UPDATE. We cap to [2,4] to match that behavior exactly --
                // notably, wide unique constraints like expression_experiment2's (6-7 columns) get
                // only the UPDATE, never conflict deletes.
                if (fkInUnique && numUniqueCols >= 2 && numUniqueCols <= 4 && numPkCols == 1) {
                    String selectList = String.join(", ", uniqueCols);
                    Map<String, String> mergedCombos =
                            comboToPk(conn, schemaTable, pkOfChild, selectList, fkColumn, toBeMergedInto);
                    if (!mergedCombos.isEmpty()) {
                        Map<String, String> deletedCombos =
                                comboToPk(conn, schemaTable, pkOfChild, selectList, fkColumn, toBeDeleted);
                        for (Map.Entry<String, String> e : deletedCombos.entrySet()) {
                            if (mergedCombos.containsKey(e.getKey())) {
                                String deletedPk = e.getValue();
                                mergeSQLs.put("delete from " + schemaTable + " where " + pkOfChild
                                        + " = '" + deletedPk + "'", depth);
                                recursivelyGetSQLs(conn, deletedPk, mergedCombos.get(e.getKey()),
                                        childTable, pkOfChild, depth);
                            }
                        }
                    }
                }

                // Scenario 2: FK is part of a composite PRIMARY KEY. Delete the conflicting rows.
                // As in scenario 1, the Perl only handles 2-, 3-, or 4-column primary keys; cap to
                // [2,4] to match (other counts get no conflict deletes, only the UPDATE/recurse).
                if (fkInPk && numPkCols >= 2 && numPkCols <= 4) {
                    String selectListP = String.join(", ", pkCols);
                    Set<String> mergedCombos =
                            combos(conn, schemaTable, selectListP, fkColumn, toBeMergedInto);
                    if (!mergedCombos.isEmpty()) {
                        List<Map<String, String>> deletedRows =
                                rowsByColumn(conn, schemaTable, selectListP, pkCols, fkColumn, toBeDeleted);
                        for (Map<String, String> row : deletedRows) {
                            StringBuilder combo = new StringBuilder();
                            for (String col : pkCols) {
                                combo.append(nz(row.get(col))); // NULL -> "" (Perl concatenates undef as "")
                            }
                            if (mergedCombos.contains(combo.toString())) {
                                StringBuilder sql = new StringBuilder("delete from " + schemaTable
                                        + " where " + fkColumn + " = '" + toBeDeleted + "'");
                                for (String col : pkCols) {
                                    sql.append(" and ").append(col).append(" = '").append(nz(row.get(col))).append("'");
                                }
                                mergeSQLs.put(sql.toString(), depth);
                            }
                        }
                    }
                }

                if (fkInPk && numPkCols == 1) {
                    // FK is the sole PK: recurse one level deeper without an UPDATE here.
                    recursivelyGetSQLs(conn, toBeDeleted, toBeMergedInto, childTable, soloPk, depth);
                } else {
                    mergeSQLs.put("update " + schemaTable + " set " + fkColumn + " = '" + toBeMergedInto
                            + "' where " + fkColumn + " = '" + toBeDeleted + "'", depth);
                }
            }

            processed.add(childTable + fkColumn);
        }
    }

    /**
     * Build delete-from-record_attribution statements for rows whose (source, type) pair already
     * exists for the surviving record (a unique-constraint conflict). Port of the Perl
     * {@code cleanupRecordAttributionTable}; {@code order} is the value stored in {@link #mergeSQLs}.
     */
    private void cleanupRecordAttributionTable(Connection conn, int order) throws SQLException {
        Set<String> mergedKeys = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select recattrib_source_zdb_id, recattrib_source_type from record_attribution "
                        + "where recattrib_data_zdb_id = ?")) {
            ps.setString(1, mergedInto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mergedKeys.add(rs.getString(1) + rs.getString(2));
                }
            }
        }
        if (mergedKeys.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "select recattrib_pk_id, recattrib_source_zdb_id, recattrib_source_type "
                        + "from record_attribution where recattrib_data_zdb_id = ?")) {
            ps.setString(1, deleted);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (mergedKeys.contains(rs.getString(2) + rs.getString(3))) {
                        mergeSQLs.put("delete from record_attribution where recattrib_pk_id = '"
                                + rs.getString(1) + "'", order);
                    }
                }
            }
        }
    }

    private void createAliasIfAbsent(Connection conn) throws SQLException {
        String existingAliasId = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "select dalias_zdb_id from data_alias where dalias_data_zdb_id = ? and dalias_alias = ? "
                        + "and dalias_group_id = 1")) {
            ps.setString(1, mergedInto);
            ps.setString(2, deletedAbbrev);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    existingAliasId = rs.getString(1);
                }
            }
        }
        if (existingAliasId != null) {
            daliasId = existingAliasId;
            return;
        }
        execUpdate(conn, "insert into zdb_active_data values (?)", daliasId);
        execUpdate(conn,
                "insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id) "
                        + "values (?, ?, ?, '1')",
                daliasId, mergedInto, deletedAbbrev);
    }

    private void createMarkerHistory(Connection conn) throws SQLException {
        execUpdate(conn, "insert into zdb_active_data values (?)", nomenId);
        execUpdate(conn,
                "insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, "
                        + "mhist_date, mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, "
                        + "mhist_comments, mhist_dalias_zdb_id) "
                        + "values (?, ?, 'merged', 'same marker', now(), ?, ?, 'none', ?)",
                nomenId, mergedInto, mergedName, mergedAbbrev, daliasId);
    }

    private void mergeAntibodyFields(Connection conn) throws SQLException {
        for (String column : new String[]{"atb_type", "atb_hviso_name", "atb_ltiso_name",
                "atb_host_organism", "atb_immun_organism"}) {
            String fromValue = scalar(conn,
                    "select " + column + " from antibody where atb_zdb_id = ?", deleted);
            if (isPresent(fromValue)) {
                String intoValue = scalar(conn,
                        "select " + column + " from antibody where atb_zdb_id = ?", mergedInto);
                if (!isPresent(intoValue)) {
                    execUpdate(conn, "update antibody set " + column + " = ? where atb_zdb_id = ?",
                            fromValue, mergedInto);
                }
            }
        }
    }

    // FB 11133: move inference_group_member rows referencing the deleted gene, skipping duplicates.
    private void mergeInferenceGroupMembers(Connection conn) throws SQLException {
        String goaway = "ZFIN:" + deleted;
        String into = "ZFIN:" + mergedInto;

        Set<String> existing = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select distinct infgrmem_mrkrgoev_zdb_id, infgrmem_inferred_from "
                        + "from inference_group_member where infgrmem_inferred_from = ?")) {
            ps.setString(1, into);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existing.add(rs.getString(1) + rs.getString(2));
                }
            }
        }

        List<String> mrkrgoevIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select distinct infgrmem_mrkrgoev_zdb_id from inference_group_member "
                        + "where infgrmem_inferred_from = ?")) {
            ps.setString(1, goaway);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mrkrgoevIds.add(rs.getString(1));
                }
            }
        }

        for (String mrkrgoevId : mrkrgoevIds) {
            String key = mrkrgoevId + into;
            if (!existing.contains(key)) {
                execUpdate(conn,
                        "update inference_group_member set infgrmem_inferred_from = ? "
                                + "where infgrmem_mrkrgoev_zdb_id = ? and infgrmem_inferred_from = ?",
                        into, mrkrgoevId, goaway);
            }
            existing.add(key);
        }
    }

    // FB 10333: if only the deleted gene has an unspecified allele, rename it onto the surviving gene.
    private void renameUnspecifiedAllele(Connection conn) throws SQLException {
        List<String> deletedAlleles = unspecifiedAlleles(conn, deleted);
        List<String> retainedAlleles = unspecifiedAlleles(conn, mergedInto);
        if (!retainedAlleles.isEmpty() || deletedAlleles.isEmpty()) {
            return;
        }
        // Match the Perl, which renames the last-fetched unspecified allele feature.
        String featureId = deletedAlleles.get(deletedAlleles.size() - 1);
        String newAlleleName = mergedAbbrev + "_unspecified";
        execUpdate(conn,
                "update feature set (feature_name, feature_abbrev) = (?, ?) where feature_zdb_id = ?",
                newAlleleName, newAlleleName, featureId);

        Map<String, String[]> genotypes = new HashMap<>(); // genoId -> {displayName, handle}
        try (PreparedStatement ps = conn.prepareStatement(
                "select geno_zdb_id, geno_display_name, geno_handle from genotype, genotype_feature "
                        + "where genofeat_feature_zdb_id = ? and genofeat_geno_zdb_id = geno_zdb_id")) {
            ps.setString(1, featureId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    genotypes.put(rs.getString(1), new String[]{rs.getString(2), rs.getString(3)});
                }
            }
        }
        for (Map.Entry<String, String[]> e : genotypes.entrySet()) {
            // The Perl uses s/// without /g here: first occurrence only.
            String displayName = replaceFirstLiteral(e.getValue()[0], deletedAbbrev, mergedAbbrev);
            String handle = replaceFirstLiteral(e.getValue()[1], deletedAbbrev, mergedAbbrev);
            execUpdate(conn,
                    "update genotype set (geno_display_name, geno_handle) = (?, ?) where geno_zdb_id = ?",
                    displayName, handle, e.getKey());
        }
    }

    private List<String> unspecifiedAlleles(Connection conn, String marker) throws SQLException {
        List<String> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select fmrel_ftr_zdb_id from feature_marker_relationship, feature "
                        + "where fmrel_ftr_zdb_id = feature_zdb_id and fmrel_mrkr_zdb_id = ? "
                        + "and feature_unspecified = 't'")) {
            ps.setString(1, marker);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
            }
        }
        return ids;
    }

    // MRDL-121: concatenate public comments onto the surviving marker.
    //
    // Faithful to the Perl original, INCLUDING its quirk: the Perl uses loose `ne 'none'` checks, and
    // in Perl `undef ne 'none'` is true, so a NULL comment on the deleted marker still "passes" and the
    // surviving marker's comment gets set to second + "\n\n" + first (== "\n\n" when both are NULL).
    // We replicate that exactly so the merge is behavior-identical to merge_markers.pl. The "\n\n"
    // pollution is a known legacy bug to be fixed separately (see follow-up ticket); do not "clean it
    // up" here or the equivalence test against the Perl will (correctly) fail.
    private void mergePublicNotes(Connection conn) throws SQLException {
        boolean[] found = new boolean[1];
        String firstNote = fetchComment(conn, deleted, found);
        if (!found[0] || NONE.equals(firstNote)) {
            return;
        }
        String combined;
        String secondNote = fetchComment(conn, mergedInto, found);
        if (found[0] && !NONE.equals(secondNote)) {
            combined = nz(secondNote) + "\n\n" + nz(firstNote);
        } else {
            combined = firstNote; // may be null -> stored as SQL NULL, matching the Perl
        }
        if (!NONE.equals(combined)) {
            execUpdate(conn, "update marker set mrkr_comments = ? where mrkr_zdb_id = ?", combined, mergedInto);
        }
    }

    /** Fetch mrkr_comments; sets found[0] to whether the marker row existed. Value may be null. */
    private String fetchComment(Connection conn, String marker, boolean[] found) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select mrkr_comments from marker where mrkr_zdb_id = ?")) {
            ps.setString(1, marker);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    found[0] = true;
                    return rs.getString(1);
                }
                found[0] = false;
                return NONE;
            }
        }
    }

    // For STRs, drop fish / marker_relationship rows that would collide on a unique constraint.
    private void deleteConflictingStrUsages(Connection conn) throws SQLException {
        List<String> fishIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select distinct fstr1.fishstr_fish_zdb_id from fish_str fstr1 "
                        + "where fstr1.fishstr_str_zdb_id = ? and exists("
                        + "select 'x' from fish_str fstr2 where fstr2.fishstr_str_zdb_id = ? "
                        + "and fstr2.fishstr_fish_zdb_id = fstr1.fishstr_fish_zdb_id)")) {
            ps.setString(1, deleted);
            ps.setString(2, mergedInto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fishIds.add(rs.getString(1));
                }
            }
        }
        List<String> relIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select distinct mrkrrel1.mrel_zdb_id from marker_relationship mrkrrel1 "
                        + "where mrkrrel1.mrel_mrkr_1_zdb_id = ? and exists("
                        + "select 'x' from marker_relationship mrkrrel2 where mrkrrel2.mrel_mrkr_1_zdb_id = ? "
                        + "and mrkrrel2.mrel_mrkr_2_zdb_id = mrkrrel1.mrel_mrkr_2_zdb_id "
                        + "and mrkrrel2.mrel_type = mrkrrel1.mrel_type)")) {
            ps.setString(1, deleted);
            ps.setString(2, mergedInto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    relIds.add(rs.getString(1));
                }
            }
        }
        for (String fishId : fishIds) {
            execUpdate(conn, "delete from zdb_active_data where zactvd_zdb_id = ?", fishId);
        }
        for (String relId : relIds) {
            execUpdate(conn, "delete from zdb_active_data where zactvd_zdb_id = ?", relId);
        }
    }

    // FB 11048: when one marker has a real GO term and the other only the ontology root, drop the root.
    private void cleanupRedundantRootGoTerms(Connection conn) throws SQLException {
        for (String[] ontology : new String[][]{
                {"biological_process", ROOT_BIOLOGICAL_PROCESS},
                {"molecular_function", ROOT_MOLECULAR_FUNCTION},
                {"cellular_component", ROOT_CELLULAR_COMPONENT}}) {
            deleteRedundantRootGo(conn, deleted, mergedInto, ontology[1], ontology[0]);
            deleteRedundantRootGo(conn, mergedInto, deleted, ontology[1], ontology[0]);
        }
    }

    private void deleteRedundantRootGo(Connection conn, String markerWithNonRoot, String markerWithRoot,
                                       String rootTermId, String ontology) throws SQLException {
        boolean hasNonRoot;
        try (PreparedStatement ps = conn.prepareStatement(
                "select 1 from marker_go_term_evidence, term where mrkrgoev_mrkr_zdb_id = ? "
                        + "and mrkrgoev_term_zdb_id != ? and mrkrgoev_term_zdb_id = term_zdb_id "
                        + "and term_ontology = ?")) {
            ps.setString(1, markerWithNonRoot);
            ps.setString(2, rootTermId);
            ps.setString(3, ontology);
            try (ResultSet rs = ps.executeQuery()) {
                hasNonRoot = rs.next();
            }
        }
        if (!hasNonRoot) {
            return;
        }
        List<String> rootEvidenceIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select mrkrgoev_zdb_id from marker_go_term_evidence "
                        + "where mrkrgoev_mrkr_zdb_id = ? and mrkrgoev_term_zdb_id = ?")) {
            ps.setString(1, markerWithRoot);
            ps.setString(2, rootTermId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rootEvidenceIds.add(rs.getString(1));
                }
            }
        }
        for (String evidenceId : rootEvidenceIds) {
            execUpdate(conn, "delete from zdb_active_data where zactvd_zdb_id = ?", evidenceId);
        }
    }

    // ---- low-level helpers -------------------------------------------------------------------

    /** Column names participating in a constraint of the given type on the given table. */
    private List<String> constraintColumns(Connection conn, String table, String constraintType)
            throws SQLException {
        List<String> cols = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select kc.column_name from information_schema.key_column_usage kc, "
                        + "information_schema.table_constraints tc where kc.table_name = ? "
                        + "and tc.table_name = kc.table_name and tc.constraint_name = kc.constraint_name "
                        + "and tc.constraint_type = ?")) {
            ps.setString(1, table);
            ps.setString(2, constraintType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cols.add(rs.getString(1));
                }
            }
        }
        return cols;
    }

    /** Map of concatenated unique-column values -> primary-key value, for rows where FK = id. */
    private Map<String, String> comboToPk(Connection conn, String schemaTable, String pkColumn,
                                          String selectList, String fkColumn, String id) throws SQLException {
        Map<String, String> result = new HashMap<>();
        String sql = "select " + pkColumn + ", " + selectList + " from " + schemaTable
                + " where " + fkColumn + " = " + lit(id);
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int colCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder combo = new StringBuilder();
                for (int i = 2; i <= colCount; i++) {
                    combo.append(nz(rs.getString(i))); // NULL -> "" (Perl concatenates undef as "")
                }
                result.put(combo.toString(), rs.getString(1));
            }
        }
        return result;
    }

    /** Set of concatenated column values from {@code selectList} for rows where FK = id. */
    private Set<String> combos(Connection conn, String schemaTable, String selectList,
                               String fkColumn, String id) throws SQLException {
        Set<String> result = new HashSet<>();
        String sql = "select " + selectList + " from " + schemaTable + " where " + fkColumn + " = " + lit(id);
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int colCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder combo = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    combo.append(nz(rs.getString(i))); // NULL -> "" (Perl concatenates undef as "")
                }
                result.add(combo.toString());
            }
        }
        return result;
    }

    /** Rows as column->value maps (only the columns named in {@code columns}), for rows where FK = id. */
    private List<Map<String, String>> rowsByColumn(Connection conn, String schemaTable, String selectList,
                                                    List<String> columns, String fkColumn, String id)
            throws SQLException {
        List<Map<String, String>> rows = new ArrayList<>();
        String sql = "select " + selectList + " from " + schemaTable + " where " + fkColumn + " = " + lit(id);
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    row.put(columns.get(i - 1), rs.getString(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private int countRows(Connection conn, String schemaTable, String column, String id) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "select count(*) from " + schemaTable + " where " + column + " = " + lit(id))) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String scalar(Connection conn, String sql, String param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Execute a statement built from trusted identifiers and validated ids (no bind parameters). */
    private void exec(Connection conn, String sql) throws SQLException {
        executedSql.add(sql);
        LOG.info("SQL: {}", sql);
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    /** Execute a parameterized statement (used whenever fetched string values are embedded). */
    private void execUpdate(Connection conn, String sql, String... params) throws SQLException {
        StringBuilder rendered = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                rendered.append(", ");
            }
            rendered.append(params[i]); // StringBuilder renders null as "null" (null-safe logging)
        }
        executedSql.add(sql + "  -- params: " + rendered);
        LOG.info("SQL: {} | params: {}", sql, rendered);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            ps.execute();
        }
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isEmpty() && !"none".equals(value);
    }

    /** Null-coalesce to empty string, mirroring Perl's undef-to-"" coercion in string concatenation. */
    private static String nz(String value) {
        return value == null ? "" : value;
    }

    /**
     * Render a value as a quoted SQL literal, the way the Perl interpolates ids/keys into its
     * queries. Crucial for the generic FK walk: a literal is "unknown"-typed and Postgres coerces it
     * to the column's type, so it works against numeric (bigint) key columns reached during recursion.
     * Binding the same value with setString would type it as varchar and fail with
     * "operator does not exist: bigint = character varying".
     */
    private static String lit(String value) {
        return value == null ? "NULL" : "'" + value.replace("'", "''") + "'";
    }

    private static String replaceFirstLiteral(String input, String target, String replacement) {
        if (input == null) {
            return null;
        }
        int idx = input.indexOf(target);
        return idx < 0 ? input : input.substring(0, idx) + replacement + input.substring(idx + target.length());
    }
}
