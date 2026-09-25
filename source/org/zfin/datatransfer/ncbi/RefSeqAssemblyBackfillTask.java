package org.zfin.datatransfer.ncbi;

import jakarta.persistence.Tuple;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.zfin.datatransfer.ncbi.dto.Gene2AccessionDTO;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zfin.datatransfer.ncbi.NCBIDirectPort.FDCONT_REFPEPT;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.FDCONT_REFSEQ_DNA;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.FDCONT_REFSEQ_RNA;
import static org.zfin.datatransfer.ncbi.port.PortHelper.stringStartsWithLetter;
import static org.zfin.util.DateUtil.nowToString;

/**
 * One-off backfill (ZFIN-10510): links RefSeq {@code db_link} rows already loaded by prior
 * {@link NCBIDirectPort} runs to the NCBI genome assembly(ies) they were annotated against.
 *
 * <p>{@link NCBIDirectPort} only captures this for accessions it loads from here on (see
 * {@code NCBIDirectPort.refSeqAccessionAssemblyIds}) — its incremental design never revisits an
 * accession that's already in {@code db_link}. This task exists to catch up the RefSeqs already
 * loaded before that change. Run it once, manually; it does not need to be scheduled, since
 * {@link NCBIDirectPort} keeps new data correct going forward.
 */
@Log4j2
public class RefSeqAssemblyBackfillTask {

    public static void main(String[] args) throws IOException {
        log.info("Starting RefSeq Assembly Backfill Task (ZFIN-10510)");

        File downloadDirectory = getDownloadDirectory();
        NCBIReleaseFetcher fetcher = new NCBIReleaseFetcher();
        NCBIReleaseFileReader reader = fetcher.downloadLatestReleaseFileSetReader(downloadDirectory);
        List<Gene2AccessionDTO> gene2AccessionDTOs = reader.readGene2AccessionFile();

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        int linksInserted;
        try {
            linksInserted = backfill(gene2AccessionDTOs, session);
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }

        log.info("Finished RefSeq Assembly Backfill Task. Links inserted: " + linksInserted);
    }

    private static File getDownloadDirectory() {
        File downloadDirectory = new File(NCBILoadTask.NCBI_DOWNLOAD_DIRECTORY_BASE, nowToString("yyyy-MM-dd"));
        if (System.getenv("NCBI_DOWNLOAD_DIRECTORY") != null) {
            downloadDirectory = new File(System.getenv("NCBI_DOWNLOAD_DIRECTORY"));
        }
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }
        return downloadDirectory;
    }

    /**
     * Links every existing RefSeq RNA/protein/genomic {@code db_link} row whose accession is
     * named in {@code gene2AccessionDTOs} to its resolved assembly(ies), inserting into
     * {@code db_link_assembly} (skipping ones already linked). Returns the number of new links
     * inserted. Package-visible/static so it can be exercised directly in tests without file
     * download.
     */
    static int backfill(List<Gene2AccessionDTO> gene2AccessionDTOs, Session session) {
        Map<String, Set<Long>> accessionToAssemblyIds = buildAccessionAssemblyMap(gene2AccessionDTOs);
        if (accessionToAssemblyIds.isEmpty()) {
            return 0;
        }

        NativeQuery<Tuple> query = session.createNativeQuery("""
                select dblink_zdb_id, dblink_acc_num
                  from db_link
                 where dblink_fdbcont_zdb_id in (:refseqRna, :refPept, :refseqDna)
                """, Tuple.class);
        query.setParameter("refseqRna", FDCONT_REFSEQ_RNA);
        query.setParameter("refPept", FDCONT_REFPEPT);
        query.setParameter("refseqDna", FDCONT_REFSEQ_DNA);
        List<Tuple> existingRefSeqDbLinks = query.list();

        int linksInserted = 0;
        for (Tuple row : existingRefSeqDbLinks) {
            String dblinkZdbId = row.get(0, String.class);
            String accNum = row.get(1, String.class);
            Set<Long> assemblyIds = accessionToAssemblyIds.get(accNum);
            if (assemblyIds == null) {
                continue;
            }
            for (Long assemblyId : assemblyIds) {
                int inserted = session.createNativeQuery("""
                        insert into db_link_assembly (dbla_dblink_zdb_id, dbla_a_pk_id)
                        values (:dblinkZdbId, :assemblyId)
                        on conflict (dbla_dblink_zdb_id, dbla_a_pk_id) do nothing
                        """)
                        .setParameter("dblinkZdbId", dblinkZdbId)
                        .setParameter("assemblyId", assemblyId)
                        .executeUpdate();
                linksInserted += inserted;
            }
        }
        return linksInserted;
    }

    static Map<String, Set<Long>> buildAccessionAssemblyMap(List<Gene2AccessionDTO> gene2AccessionDTOs) {
        Map<String, Set<Long>> accessionToAssemblyIds = new HashMap<>();
        for (Gene2AccessionDTO dto : gene2AccessionDTOs) {
            if (!dto.includeThisRecord() || "SUPPRESSED".equals(dto.status())) {
                continue;
            }
            Long assemblyId = NcbiAssemblyResolver.resolveAssemblyId(dto.assembly());
            if (assemblyId == null) {
                continue;
            }
            addAccession(accessionToAssemblyIds, dto.rnaNucleotideAccessionVersion(), assemblyId);
            addAccession(accessionToAssemblyIds, dto.proteinAccessionVersion(), assemblyId);
            addAccession(accessionToAssemblyIds, dto.genomicNucleotideAccessionVersion(), assemblyId);
        }
        return accessionToAssemblyIds;
    }

    private static void addAccession(Map<String, Set<Long>> accessionToAssemblyIds, String accessionVersion, Long assemblyId) {
        if (!stringStartsWithLetter(accessionVersion)) {
            return;
        }
        String accession = accessionVersion.replaceFirst("\\.\\d+$", "");
        accessionToAssemblyIds.computeIfAbsent(accession, k -> new LinkedHashSet<>()).add(assemblyId);
    }
}
