package org.zfin.datatransfer.ensembl;

import jakarta.persistence.Tuple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.Session;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Loads Ensembl gene IDs (ENSDARGs) for the newer genome assemblies into {@code db_link} from a z11 -> z12
 * mapping file (see {@link EnsemblMappingFile}).
 *
 * <p>For every gene / ENSDARG / assembly in the file it makes sure there is
 * <ul>
 *   <li>a {@code db_link} from the gene to the ENSDARG, under the {@code Ensembl(<assembly>)} reference
 *   database (e.g. {@code Ensembl(GRCz12tu)}),</li>
 *   <li>a {@code db_link_assembly} row tying it to that assembly, and</li>
 *   <li>a {@code record_attribution} to the publication given for this load.</li>
 * </ul>
 * Only unambiguous accessions are loaded: an ENSDARG the file gives to several genes, or that is already linked to
 * a different gene, is skipped, as are rows the file flags as conflicts, unknown genes and malformed cells. Rows
 * whose z11 ID is known only from NCBI ({@code z11_source=ncbi}) and that Ensembl does not corroborate
 * ({@code is_match_to_xref} of {@code no_xref} or {@code mismatch}) are skipped too, as are genes whose NCBI Gene ID in
 * the file differs from the one ZFIN has, and genes with neither an NCBI Gene ID nor a GRCz11 Ensembl ID in ZFIN. Each
 * kind of skip has its own report category and a warning in the summary.
 * Every omission is listed in the report with its reason.
 *
 * <p>Nothing is ever deleted or changed, so a rerun of the same file adds nothing, and an existing link that lacks
 * its assembly or attribution gets just that. The z11 column is not loaded: z11 links already exist.
 *
 * <p>Run from the {@code Load-Ensembl-Mapping} Jenkins job. Inputs, as environment variables:
 * <ul>
 *   <li>{@code MAPPING_FILE_UPLOAD}: the Jenkins file parameter; the file is {@code $WORKSPACE/MAPPING_FILE_UPLOAD}.
 *   Or {@code MAPPING_FILE}: a path (plain or .gz), for running by hand.</li>
 *   <li>{@code ATTRIBUTION_PUB_ID}: required, the ZDB-PUB ID the new links are attributed to.</li>
 *   <li>{@code DRY_RUN}: anything but {@code false} only reports what would be done (the default).</li>
 * </ul>
 * Writes {@code ensembl_mapping_load_report.csv} (one line per gene / ENSDARG / assembly and what happened to it)
 * and {@code ensembl_mapping_load_summary.txt} to the working directory.
 *
 * <p>By hand: {@code MAPPING_FILE=/path/map.csv ATTRIBUTION_PUB_ID=ZDB-PUB-... DRY_RUN=false gradle loadEnsemblMapping}
 */
@Log4j2
public class LoadEnsemblMappingTask extends AbstractScriptWrapper {

    public static final String REPORT_FILE = "ensembl_mapping_load_report.csv";
    public static final String SUMMARY_FILE = "ensembl_mapping_load_summary.txt";
    public static final String UPLOAD_PARAMETER = "MAPPING_FILE_UPLOAD";

    private static final Pattern PUB_ZDB_ID = Pattern.compile("ZDB-PUB-\\d{6}-\\d+");

    enum Action {
        ADDED,
        ALREADY_LOADED,
        ASSEMBLY_OR_ATTRIBUTION_ADDED,
        NO_SUCH_GENE,
        REJECTED,
        HELD_CONFLICT,
        HELD_NCBI_ONLY_UNCORROBORATED,
        HELD_NCBI_ID_DISAGREES,
        HELD_NO_NCBI_OR_Z11_IN_DB,
        AMBIGUOUS_IN_FILE,
        AMBIGUOUS_IN_DB;

        boolean omitted() {
            return this != ADDED && this != ALREADY_LOADED && this != ASSEMBLY_OR_ATTRIBUTION_ADDED;
        }
    }

    record ReportLine(Action action, String zdbId, String ensdarg, String assembly, String detail) {
    }

    private final boolean dryRun;
    private final String pubId;
    private final Path mappingFile;
    private final Path outputDirectory;

    LoadEnsemblMappingTask(Path mappingFile, String pubId, boolean dryRun, Path outputDirectory) {
        this.mappingFile = mappingFile;
        this.pubId = pubId;
        this.dryRun = dryRun;
        this.outputDirectory = outputDirectory;
    }

    public static void main(String[] args) throws IOException {
        String pubId = env("ATTRIBUTION_PUB_ID");
        if (!PUB_ZDB_ID.matcher(pubId).matches()) {
            fail("ATTRIBUTION_PUB_ID must be a publication ZDB ID like ZDB-PUB-240101-1, got '" + pubId + "'");
        }
        Path file = determineInputFile();
        boolean dryRun = !"false".equalsIgnoreCase(env("DRY_RUN"));

        LoadEnsemblMappingTask task = new LoadEnsemblMappingTask(file, pubId, dryRun, Paths.get("."));
        task.initAll();
        task.run();
        System.exit(0);
    }

    private static Path determineInputFile() {
        String workspace = env("WORKSPACE");
        if (!workspace.isEmpty() && Files.isRegularFile(Paths.get(workspace, UPLOAD_PARAMETER))) {
            return Paths.get(workspace, UPLOAD_PARAMETER);
        }
        String path = env("MAPPING_FILE");
        if (!path.isEmpty() && Files.isRegularFile(Paths.get(path))) {
            return Paths.get(path);
        }
        System.err.println("Provide the mapping file through the Jenkins upload parameter (" + UPLOAD_PARAMETER
                + ") or the environment variable MAPPING_FILE.");
        System.err.println("Example: MAPPING_FILE=/tmp/z11_z12_mapping.csv ATTRIBUTION_PUB_ID=ZDB-PUB-240101-1 gradle loadEnsemblMapping");
        System.exit(1);
        return null;
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value.trim();
    }

    private static void fail(String message) {
        System.err.println(message);
        System.exit(1);
    }

    void run() throws IOException {
        log.info("Ensembl mapping load: file=" + mappingFile + " pub=" + pubId + " dryRun=" + dryRun
               );
        EnsemblMappingFile mapping = EnsemblMappingFile.read(mappingFile, false);

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        List<ReportLine> report = new ArrayList<>();
        try {
            requirePublication(session);
            Map<String, Long> assemblyIds = new LinkedHashMap<>();
            Map<String, String> fdbcontIds = new LinkedHashMap<>();
            for (String column : mapping.assemblyColumnsPresent()) {
                String assembly = EnsemblMappingFile.ASSEMBLY_COLUMNS.get(column);
                assemblyIds.put(assembly, findAssemblyId(session, assembly));
                fdbcontIds.put(assembly, findFdbcontId(session, assembly));
            }
            load(session, mapping, assemblyIds, fdbcontIds, report);
            if (dryRun) {
                session.getTransaction().rollback();
            } else {
                session.getTransaction().commit();
            }
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }
        writeReport(report);
        writeSummary(mapping, report);
    }

    private void requirePublication(Session session) {
        Number found = (Number) session.createNativeQuery("select count(*) from publication where zdb_id = :pub")
                .setParameter("pub", pubId).getSingleResult();
        if (found.intValue() == 0) {
            throw new IllegalArgumentException("Publication " + pubId + " does not exist");
        }
    }

    private Long findAssemblyId(Session session, String assembly) {
        List<?> ids = session.createNativeQuery("select a_pk_id from assembly where a_name = :name")
                .setParameter("name", assembly).list();
        if (ids.isEmpty()) {
            throw new IllegalStateException("No assembly named " + assembly + " in the assembly table");
        }
        return ((Number) ids.get(0)).longValue();
    }

    private String findFdbcontId(Session session, String assembly) {
        String dbName = "Ensembl(" + assembly + ")";
        List<?> ids = session.createNativeQuery("""
                        select fdbcont_zdb_id
                          from foreign_db_contains
                          join foreign_db on fdbcont_fdb_db_id = fdb_db_pk_id
                         where fdb_db_name = :name and fdbcont_organism_common_name = 'Zebrafish'
                        """)
                .setParameter("name", dbName).list();
        if (ids.size() != 1) {
            throw new IllegalStateException("Expected exactly one Zebrafish reference database named " + dbName
                    + " but found " + ids.size() + ". It is added by the Ensembl(GRCz12tu/GRCz12ab/GRCz13ab) migration.");
        }
        return (String) ids.get(0);
    }

    private void load(Session session, EnsemblMappingFile mapping, Map<String, Long> assemblyIds,
                      Map<String, String> fdbcontIds, List<ReportLine> report) {
        for (EnsemblMappingFile.Rejected r : mapping.rejected()) {
            report.add(new ReportLine(Action.REJECTED, r.zdbId(), r.column().equals("zdb_id") ? "" : r.value(), "",
                    "line " + r.lineNumber() + ": " + r.reason()));
        }
        for (EnsemblMappingFile.Conflict c : mapping.conflicts()) {
            report.add(new ReportLine(Action.HELD_CONFLICT, c.zdbId(), c.ensdarg(), c.assemblyName(),
                    "line " + c.lineNumber() + ": " + c.conflict()));
        }

        for (EnsemblMappingFile.Unsupported u : mapping.unsupported()) {
            report.add(new ReportLine(Action.HELD_NCBI_ONLY_UNCORROBORATED, u.zdbId(), u.ensdarg(), u.assemblyName(),
                    "line " + u.lineNumber() + ": WARNING not loaded: z11_source=" + u.z11Source()
                            + " (only NCBI links this gene to the z11 ID) and is_match_to_xref=" + u.xref()
                            + " (Ensembl does not confirm it)"));
        }

        for (EnsemblMappingFile.Ambiguous a : mapping.ambiguous()) {
            report.add(new ReportLine(Action.AMBIGUOUS_IN_FILE, a.zdbId(), a.ensdarg(), a.assemblyName(),
                    "line " + a.lineNumber() + ": the file gives this ENSDARG to " + a.genes().size() + " genes: " + String.join(" ", a.genes())));
        }

        Set<String> markers = new HashSet<>();
        for (Object id : session.createNativeQuery("select mrkr_zdb_id from marker").list()) {
            markers.add((String) id);
        }

        // key: gene | ensdarg | fdbcont  ->  dblink id
        Map<String, String> existingLinks = new HashMap<>();
        // key: fdbcont | ensdarg  ->  genes it is already linked to
        Map<String, Set<String>> existingGenesByAccession = new HashMap<>();
        for (Tuple row : session.createNativeQuery("""
                select dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id, dblink_zdb_id
                  from db_link where dblink_fdbcont_zdb_id in (:fdbconts)
                """, Tuple.class).setParameterList("fdbconts", fdbcontIds.values()).list()) {
            existingLinks.put(linkKey(row.get(0, String.class), row.get(1, String.class), row.get(2, String.class)),
                    row.get(3, String.class));
            existingGenesByAccession.computeIfAbsent(row.get(2, String.class) + "|" + row.get(1, String.class), k -> new TreeSet<>())
                    .add(row.get(0, String.class));
        }
        Set<String> existingAssemblyLinks = new HashSet<>();
        for (Tuple row : session.createNativeQuery("""
                select dbla_dblink_zdb_id, dbla_a_pk_id from db_link_assembly
                  join db_link on dblink_zdb_id = dbla_dblink_zdb_id
                 where dblink_fdbcont_zdb_id in (:fdbconts)
                """, Tuple.class).setParameterList("fdbconts", fdbcontIds.values()).list()) {
            existingAssemblyLinks.add(row.get(0, String.class) + "|" + row.get(1, Number.class).longValue());
        }
        Set<String> existingAttributions = new HashSet<>();
        for (Object id : session.createNativeQuery("""
                select recattrib_data_zdb_id from record_attribution
                 where recattrib_source_zdb_id = :pub and recattrib_source_type = 'standard'
                """).setParameter("pub", pubId).list()) {
            existingAttributions.add((String) id);
        }

        // the gene's NCBI Gene IDs and whether it has a GRCz11 Ensembl ID, as ZFIN already records them
        Map<String, Set<String>> dbNcbiIds = new HashMap<>();
        Set<String> genesWithZ11 = new HashSet<>();
        for (Tuple row : session.createNativeQuery("""
                select dblink_linked_recid, dblink_acc_num, fdb_db_name
                  from db_link
                  join foreign_db_contains on fdbcont_zdb_id = dblink_fdbcont_zdb_id
                  join foreign_db on fdb_db_pk_id = fdbcont_fdb_db_id
                 where fdbcont_organism_common_name = 'Zebrafish'
                   and fdb_db_name in ('Gene', 'Ensembl(GRCz11)')
                """, Tuple.class).list()) {
            if ("Gene".equals(row.get(2, String.class))) {
                dbNcbiIds.computeIfAbsent(row.get(0, String.class), k -> new TreeSet<>()).add(row.get(1, String.class));
            } else {
                genesWithZ11.add(row.get(0, String.class));
            }
        }

        for (EnsemblMappingFile.Entry entry : mapping.entries()) {
            if (!markers.contains(entry.zdbId())) {
                report.add(new ReportLine(Action.NO_SUCH_GENE, entry.zdbId(), entry.ensdarg(), entry.assemblyName(),
                        "line " + entry.lineNumber() + ": no marker with this ZDB ID"));
                continue;
            }
            Set<String> dbNcbi = dbNcbiIds.getOrDefault(entry.zdbId(), Set.of());
            if (dbNcbi.isEmpty() && !genesWithZ11.contains(entry.zdbId())) {
                report.add(new ReportLine(Action.HELD_NO_NCBI_OR_Z11_IN_DB, entry.zdbId(), entry.ensdarg(), entry.assemblyName(),
                        "line " + entry.lineNumber() + ": WARNING not loaded: the gene has neither an NCBI Gene ID nor a GRCz11 Ensembl ID in ZFIN, so nothing in ZFIN corroborates this mapping"));
                continue;
            }
            if (!dbNcbi.isEmpty() && !entry.ncbiGeneIds().isEmpty() && Collections.disjoint(dbNcbi, entry.ncbiGeneIds())) {
                report.add(new ReportLine(Action.HELD_NCBI_ID_DISAGREES, entry.zdbId(), entry.ensdarg(), entry.assemblyName(),
                        "line " + entry.lineNumber() + ": WARNING not loaded: ZFIN has NCBI Gene ID " + String.join(" ", dbNcbi)
                                + " for this gene but the mapping file has " + String.join(" ", entry.ncbiGeneIds())));
                continue;
            }
            String fdbcontId = fdbcontIds.get(entry.assemblyName());
            long assemblyId = assemblyIds.get(entry.assemblyName());
            Set<String> linkedGenes = existingGenesByAccession.getOrDefault(fdbcontId + "|" + entry.ensdarg(), Set.of());
            if (linkedGenes.stream().anyMatch(g -> !g.equals(entry.zdbId()))) {
                report.add(new ReportLine(Action.AMBIGUOUS_IN_DB, entry.zdbId(), entry.ensdarg(), entry.assemblyName(),
                        "line " + entry.lineNumber() + ": already linked in the database to " + String.join(" ", linkedGenes)));
                continue;
            }
            String key = linkKey(entry.zdbId(), entry.ensdarg(), fdbcontId);

            String dblinkId = existingLinks.get(key);
            boolean created = dblinkId == null;
            if (created) {
                dblinkId = (String) session.createNativeQuery("select get_id_and_insert_active_data('DBLINK')").getSingleResult();
                session.createNativeMutationQuery("""
                                insert into db_link (dblink_linked_recid, dblink_acc_num, dblink_zdb_id, dblink_fdbcont_zdb_id)
                                values (:gene, :acc, :id, :fdbcont)
                                """)
                        .setParameter("gene", entry.zdbId())
                        .setParameter("acc", entry.ensdarg())
                        .setParameter("id", dblinkId)
                        .setParameter("fdbcont", fdbcontId)
                        .executeUpdate();
                existingLinks.put(key, dblinkId);
            }
            boolean assemblyAdded = existingAssemblyLinks.add(dblinkId + "|" + assemblyId);
            if (assemblyAdded) {
                session.createNativeMutationQuery("insert into db_link_assembly (dbla_dblink_zdb_id, dbla_a_pk_id) values (:id, :assembly)")
                        .setParameter("id", dblinkId)
                        .setParameter("assembly", assemblyId)
                        .executeUpdate();
            }
            boolean attributionAdded = existingAttributions.add(dblinkId);
            if (attributionAdded) {
                session.createNativeMutationQuery("""
                                insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
                                values (:id, :pub)
                                """)
                        .setParameter("id", dblinkId)
                        .setParameter("pub", pubId)
                        .executeUpdate();
            }

            if (created) {
                report.add(new ReportLine(Action.ADDED, entry.zdbId(), entry.ensdarg(), entry.assemblyName(), dblinkId));
            } else if (assemblyAdded || attributionAdded) {
                report.add(new ReportLine(Action.ASSEMBLY_OR_ATTRIBUTION_ADDED, entry.zdbId(), entry.ensdarg(), entry.assemblyName(),
                        dblinkId + (assemblyAdded ? " +assembly" : "") + (attributionAdded ? " +attribution" : "")));
            } else {
                report.add(new ReportLine(Action.ALREADY_LOADED, entry.zdbId(), entry.ensdarg(), entry.assemblyName(), dblinkId));
            }
        }
    }

    private static String linkKey(String gene, String ensdarg, String fdbcont) {
        return gene + "|" + ensdarg + "|" + fdbcont;
    }

    private void writeReport(List<ReportLine> report) throws IOException {
        try (Writer writer = Files.newBufferedWriter(outputDirectory.resolve(REPORT_FILE));
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord("action", "zdb_id", "ensdarg", "assembly", "detail");
            for (ReportLine line : report) {
                printer.printRecord(line.action(), line.zdbId(), line.ensdarg(), line.assembly(), line.detail());
            }
        }
    }

    private void writeSummary(EnsemblMappingFile mapping, List<ReportLine> report) throws IOException {
        Map<String, Map<Action, Integer>> counts = new TreeMap<>();
        for (ReportLine line : report) {
            counts.computeIfAbsent(line.assembly().isEmpty() ? "(none)" : line.assembly(), k -> new LinkedHashMap<>())
                    .merge(line.action(), 1, Integer::sum);
        }
        StringBuilder text = new StringBuilder();
        text.append(dryRun ? "DRY RUN: nothing was written to the database.\n\n" : "LOADED into the database.\n\n");
        text.append("Mapping file:        ").append(mappingFile.getFileName()).append('\n');
        text.append("Attributed to:       ").append(pubId).append('\n');
        text.append("Rows in file:        ").append(mapping.rowCount()).append('\n');
        text.append("Assembly columns:    ").append(String.join(", ", mapping.assemblyColumnsPresent())).append('\n');
        text.append("Repeated pairs:      ").append(mapping.duplicateCount()).append(" (same gene, ENSDARG and assembly seen earlier)\n");
                long omitted = report.stream().filter(l -> l.action().omitted()).count();
        text.append("OMITTED:             ").append(omitted).append(" associations were not loaded (see the counts below and ")
                .append(REPORT_FILE).append(")\n");
        for (Map.Entry<String, Map<Action, Integer>> byAssembly : counts.entrySet()) {
            text.append('\n').append(byAssembly.getKey()).append('\n');
            for (Action action : Action.values()) {
                Integer count = byAssembly.getValue().get(action);
                if (count != null) {
                    text.append(String.format("  %-32s %d%n", action, count));
                }
            }
        }
        Map<Action, String> warnings = new LinkedHashMap<>();
        warnings.put(Action.HELD_NCBI_ONLY_UNCORROBORATED,
                "only NCBI links the gene to its z11 ID (z11_source=ncbi) and Ensembl does not confirm it (is_match_to_xref=no_xref or mismatch)");
        warnings.put(Action.HELD_NCBI_ID_DISAGREES,
                "the NCBI Gene ID in the mapping file differs from the one ZFIN has for the gene");
        warnings.put(Action.HELD_NO_NCBI_OR_Z11_IN_DB,
                "the gene has neither an NCBI Gene ID nor a GRCz11 Ensembl ID in ZFIN, so nothing in ZFIN corroborates the mapping");
        for (Map.Entry<Action, String> warning : warnings.entrySet()) {
            List<ReportLine> held = report.stream().filter(l -> l.action() == warning.getKey()).toList();
            if (!held.isEmpty()) {
                long genes = held.stream().map(ReportLine::zdbId).distinct().count();
                text.append("\nWARNING: ").append(held.size()).append(" associations (").append(genes)
                        .append(" genes) were NOT loaded: ").append(warning.getValue()).append(".\n")
                        .append("They are listed as ").append(warning.getKey()).append(" in ").append(REPORT_FILE)
                        .append(" for curator review.\n");
            }
        }
        text.append("\nPer-row detail: ").append(REPORT_FILE).append('\n');
        Files.writeString(outputDirectory.resolve(SUMMARY_FILE), text.toString());
        System.out.println(text);
    }
}
