package org.zfin.datatransfer.ncbi;

import jakarta.persistence.Tuple;
import org.hibernate.Session;
import org.zfin.datatransfer.ncbi.dto.Gene2AccessionDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zfin.datatransfer.ncbi.NCBIDirectPort.FDCONT_REFPEPT;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.FDCONT_REFSEQ_DNA;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.FDCONT_REFSEQ_RNA;
import static org.zfin.datatransfer.ncbi.port.PortHelper.stringStartsWithLetter;
import static org.zfin.mapping.GenomeLocation.GRCZ12TU;

/**
 * Makes {@code db_link_assembly} match NCBI's gene2accession for RefSeq RNA, protein and genomic
 * {@code db_link} rows (ZFIN-10510): each such row is linked to exactly the assemblies NCBI
 * annotates its accession against. Links are added and removed, so an accession NCBI drops from
 * an assembly loses that link. Accessions absent from gene2accession are left untouched.
 *
 * <p>{@link #reconcileMarkerAssemblies} then carries those links up to the marker
 * ({@code marker_assembly}, ZFIN-10531) for every current assembly except GRCz12tu.
 *
 * <p>Run by {@link NCBIDirectPort} after every load, and on demand by
 * {@link RefSeqAssemblyBackfillTask}. Works inside the caller's Hibernate session and transaction.
 */
class RefSeqAssemblyReconciler {

    record Link(String dblinkZdbId, long assemblyId) {
    }

    record Changes(List<Link> toAdd, List<Link> toRemove) {
    }

    record MarkerAssemblyChanges(int added, int removed) {
    }

    // The RefSeq db_link rows whose assemblies a marker inherits, as a SQL condition on db_link.
    private static final String REFSEQ_DBLINK = "dblink_fdbcont_zdb_id in (:refseqRna, :refPept, :refseqDna)";

    // The assemblies whose marker_assembly rows come from the RefSeq links: every current assembly
    // but GRCz12tu, whose rows markerAssemblyUpdate.sql and NCBIGff3Processor write from the gene's
    // NCBI genome location instead.
    private static final String REFSEQ_DERIVED_ASSEMBLY = "a_is_current and a_name <> :grcz12tu";

    /**
     * Maps each RefSeq accession (version stripped) named in non-suppressed zebrafish rows to the
     * assemblies it is annotated against. An accession whose rows name no recognised assembly
     * maps to an empty set, so reconciling removes any links it had.
     */
    static Map<String, Set<Long>> buildAccessionAssemblyMap(List<Gene2AccessionDTO> gene2AccessionDTOs,
                                                             NcbiAssemblyResolver assemblyResolver) {
        Map<String, Set<Long>> accessionToAssemblyIds = new HashMap<>();
        for (Gene2AccessionDTO dto : gene2AccessionDTOs) {
            if (!dto.includeThisRecord() || "SUPPRESSED".equals(dto.status()) || "-".equals(dto.status())) {
                continue;
            }
            Long assemblyId = assemblyResolver.resolveAssemblyId(dto.assembly());
            addAccession(accessionToAssemblyIds, dto.rnaNucleotideAccessionVersion(), assemblyId);
            addAccession(accessionToAssemblyIds, dto.proteinAccessionVersion(), assemblyId);
            addAccession(accessionToAssemblyIds, dto.genomicNucleotideAccessionVersion(), assemblyId);
        }
        return accessionToAssemblyIds;
    }

    /**
     * Records {@code accessionVersion} (version stripped) in {@code accessionToAssemblyIds}, with
     * {@code assemblyId} when it is non-null. Shared with {@link NCBIDirectPort}'s own
     * gene2accession parse so both build the map the same way.
     */
    static void addAccession(Map<String, Set<Long>> accessionToAssemblyIds, String accessionVersion, Long assemblyId) {
        if (!stringStartsWithLetter(accessionVersion)) {
            return;
        }
        String accession = accessionVersion.replaceFirst("\\.\\d+$", "");
        Set<Long> assemblyIds = accessionToAssemblyIds.computeIfAbsent(accession, k -> new LinkedHashSet<>());
        if (assemblyId != null) {
            assemblyIds.add(assemblyId);
        }
    }

    /**
     * Brings {@code db_link_assembly} in line with {@code accessionToAssemblyIds} and returns what
     * changed.
     */
    static Changes reconcile(Map<String, Set<Long>> accessionToAssemblyIds, Session session) {
        List<Tuple> rows = session.createNativeQuery("""
                select dblink_zdb_id, dblink_acc_num, dbla_a_pk_id
                  from db_link
                  left join db_link_assembly on dbla_dblink_zdb_id = dblink_zdb_id
                 where dblink_fdbcont_zdb_id in (:refseqRna, :refPept, :refseqDna)
                """, Tuple.class)
                .setParameter("refseqRna", FDCONT_REFSEQ_RNA)
                .setParameter("refPept", FDCONT_REFPEPT)
                .setParameter("refseqDna", FDCONT_REFSEQ_DNA)
                .list();

        Map<String, String> accessionByDblink = new HashMap<>();
        Map<String, Set<Long>> currentByDblink = new HashMap<>();
        for (Tuple row : rows) {
            String dblinkZdbId = row.get(0, String.class);
            accessionByDblink.put(dblinkZdbId, row.get(1, String.class));
            Set<Long> current = currentByDblink.computeIfAbsent(dblinkZdbId, k -> new LinkedHashSet<>());
            Number assemblyId = row.get(2, Number.class);
            if (assemblyId != null) {
                current.add(assemblyId.longValue());
            }
        }

        Changes changes = plan(accessionToAssemblyIds, accessionByDblink, currentByDblink);
        for (Link link : changes.toRemove()) {
            session.createNativeMutationQuery("""
                    delete from db_link_assembly
                     where dbla_dblink_zdb_id = :dblinkZdbId and dbla_a_pk_id = :assemblyId
                    """)
                    .setParameter("dblinkZdbId", link.dblinkZdbId())
                    .setParameter("assemblyId", link.assemblyId())
                    .executeUpdate();
        }
        for (Link link : changes.toAdd()) {
            session.createNativeMutationQuery("""
                    insert into db_link_assembly (dbla_dblink_zdb_id, dbla_a_pk_id)
                    values (:dblinkZdbId, :assemblyId)
                    """)
                    .setParameter("dblinkZdbId", link.dblinkZdbId())
                    .setParameter("assemblyId", link.assemblyId())
                    .executeUpdate();
        }
        return changes;
    }

    /**
     * Makes {@code marker_assembly} agree with {@code db_link_assembly} for the RefSeq-derived
     * assemblies (GRCz13ab, GRCz12ab, ...; see {@link #REFSEQ_DERIVED_ASSEMBLY}): a marker is on
     * such an assembly exactly when one of its RefSeq accessions is. Run after {@link #reconcile},
     * so the marker rows follow this run's links. Without it the gene page's Genome Assembly field,
     * which reads marker_assembly, never shows those assemblies (ZFIN-10531).
     */
    static MarkerAssemblyChanges reconcileMarkerAssemblies(Session session) {
        int removed = session.createNativeMutationQuery("""
                delete from marker_assembly ma
                 using assembly a
                 where a.a_pk_id = ma.ma_a_pk_id
                   and %s
                   and not exists (select 1
                                     from db_link
                                     join db_link_assembly on dbla_dblink_zdb_id = dblink_zdb_id
                                    where dblink_linked_recid = ma.ma_mrkr_zdb_id
                                      and dbla_a_pk_id = ma.ma_a_pk_id
                                      and %s)
                """.formatted(REFSEQ_DERIVED_ASSEMBLY, REFSEQ_DBLINK))
                .setParameter("grcz12tu", GRCZ12TU)
                .setParameter("refseqRna", FDCONT_REFSEQ_RNA)
                .setParameter("refPept", FDCONT_REFPEPT)
                .setParameter("refseqDna", FDCONT_REFSEQ_DNA)
                .executeUpdate();
        int added = session.createNativeMutationQuery("""
                insert into marker_assembly (ma_mrkr_zdb_id, ma_a_pk_id)
                select distinct dblink_linked_recid, dbla_a_pk_id
                  from db_link
                  join db_link_assembly on dbla_dblink_zdb_id = dblink_zdb_id
                  join assembly on a_pk_id = dbla_a_pk_id
                  join marker on mrkr_zdb_id = dblink_linked_recid
                 where %s
                   and %s
                on conflict (ma_a_pk_id, ma_mrkr_zdb_id) do nothing
                """.formatted(REFSEQ_DBLINK, REFSEQ_DERIVED_ASSEMBLY))
                .setParameter("grcz12tu", GRCZ12TU)
                .setParameter("refseqRna", FDCONT_REFSEQ_RNA)
                .setParameter("refPept", FDCONT_REFPEPT)
                .setParameter("refseqDna", FDCONT_REFSEQ_DNA)
                .executeUpdate();
        return new MarkerAssemblyChanges(added, removed);
    }

    /**
     * Works out the links to add and remove so that every RefSeq {@code db_link} whose accession is
     * in {@code accessionToAssemblyIds} ends up linked to exactly that accession's assemblies.
     * Pure, so the decision can be tested without a database.
     *
     * @param accessionByDblink every RefSeq db_link's accession, keyed by db_link ZDB ID
     * @param currentByDblink   each of those db_links' current assembly IDs (absent or empty if none)
     */
    static Changes plan(Map<String, Set<Long>> accessionToAssemblyIds,
                        Map<String, String> accessionByDblink,
                        Map<String, Set<Long>> currentByDblink) {
        List<Link> toAdd = new ArrayList<>();
        List<Link> toRemove = new ArrayList<>();
        accessionByDblink.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Set<Long> wanted = accessionToAssemblyIds.get(entry.getValue());
                    if (wanted == null) {
                        return;
                    }
                    String dblinkZdbId = entry.getKey();
                    Set<Long> current = currentByDblink.getOrDefault(dblinkZdbId, Set.of());
                    current.stream().filter(id -> !wanted.contains(id)).sorted()
                            .forEach(id -> toRemove.add(new Link(dblinkZdbId, id)));
                    wanted.stream().filter(id -> !current.contains(id)).sorted()
                            .forEach(id -> toAdd.add(new Link(dblinkZdbId, id)));
                });
        return new Changes(toAdd, toRemove);
    }
}
